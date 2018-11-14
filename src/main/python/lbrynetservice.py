import sys
from twisted.internet import asyncioreactor
if 'twisted.internet.reactor' not in sys.modules:
    asyncioreactor.install()
else:
    from twisted.internet import reactor
    if not isinstance(reactor, asyncioreactor.AsyncioSelectorReactor) and getattr(sys, 'frozen', False):
        # pyinstaller hooks install the default reactor before
        # any of our code runs, see kivy for similar problem:
        #    https://github.com/kivy/kivy/issues/4182
        del sys.modules['twisted.internet.reactor']
        asyncioreactor.install()
        from twisted.internet import reactor

import keyring.backend
import platform
import ssl
from jnius import autoclass

# Fixes / patches / overrides
# platform.platform() in libc_ver: IOError: [Errno 21] Is a directory
lbrynet_utils = autoclass('io.lbry.browser.Utils')
service = autoclass('io.lbry.browser.LbrynetService').serviceInstance
platform.platform = lambda: 'Android %s (API %s)' % (lbrynet_utils.getAndroidRelease(), lbrynet_utils.getAndroidSdk())

import lbrynet.androidhelpers
# RPC authentication secret
# Retrieve the Android keystore
ks = lbrynet_utils.initKeyStore(service.getApplicationContext())

'''
import lbrynet.daemon.auth
from lbrynet.daemon.auth.util import APIKey, API_KEY_NAME

def load_api_keys(path):
    key_name = API_KEY_NAME
    context = service.getApplicationContext();
    secret = lbrynet_utils.loadApiSecret(context, ks)
    # TODO: For testing. Normally, this should not be displayed.
    log.info('Loaded API Secret: %s', secret);
    return { key_name: APIKey(secret, key_name, None) }

def save_api_keys(keys, path):
    key_name = API_KEY_NAME
    if key_name in keys:
        secret = keys[key_name].secret
        # TODO: For testing. Normally, this should not be displayed.
        log.info('Saving API Secret: %s', secret)
        context = service.getApplicationContext()
        lbrynet_utils.saveApiSecret(secret, context, ks)

def initialize_api_key_file(key_path):
    context = service.getApplicationContext()
    secret = lbrynet_utils.loadApiSecret(context, ks)
    if secret is None:
        keys = {}
        new_api_key = APIKey.new(name=API_KEY_NAME)
        keys.update({new_api_key.name: new_api_key})
        save_api_keys(keys, key_path)

lbrynet.daemon.auth.util.load_api_keys = load_api_keys
lbrynet.daemon.auth.util.save_api_keys = save_api_keys
lbrynet.daemon.auth.util.initialize_api_key_file = initialize_api_key_file
'''

# Keyring backend
class LbryAndroidKeyring(keyring.backend.KeyringBackend):
    priority = 1

    def set_password(self, servicename, username, password):
        context = service.getApplicationContext()
        lbrynet_utils.setPassword(servicename, username, password, context, ks)

    def get_password(self, servicename, username):
        context = service.getApplicationContext()
        return lbrynet_utils.getPassword(servicename, username, context, ks)

    def delete_password(self, servicename, username):
        context = service.getApplicationContext()
        lbrynet_utils.deletePassword(servicename, username, context, ks)

# set the keyring backend
keyring.set_keyring(LbryAndroidKeyring())

import logging.handlers
from lbrynet.core import log_support
from twisted.internet import reactor

from lbrynet import analytics
from lbrynet import conf
from lbrynet.core import utils, system_info
from lbrynet.daemon.Components import PEER_PROTOCOL_SERVER_COMPONENT, REFLECTOR_COMPONENT
from lbrynet.daemon.Daemon import Daemon

# https certificate verification
# TODO: this is bad. Need to find a way to properly verify https requests
def https_context():
    #urllib2
    try:
        _create_unverified_https_context = ssl._create_unverified_context
    except AttributeError:
        # Legacy Python that doesn't verify HTTPS certificates by default
        pass
    else:
        # Handle target environment that doesn't support HTTPS verification
        ssl._create_default_https_context = _create_unverified_https_context

    '''
    # requests
    from functools import partial
    class partialmethod(partial):
        def __get__(self, instance, owner):
            if instance is None:
                return self

            return partial(self.func, instance, *(self.args or ()), **(self.keywords or {}))

    default_request = requests.Session.request
    requests.Session.request = partialmethod(default_request, verify=False)
    '''

# LBRY Daemon
log = logging.getLogger(__name__)

def test_internet_connection():
    return utils.check_connection()

def start():
    # lbry daemon
    https_context()
    conf.initialize_settings()

    lbrynet_log = conf.settings.get_log_filename()
    log_support.configure_logging(lbrynet_log, True, [])

    # TODO: specify components, initialise auth
    conf.settings.update({
        'components_to_skip': [PEER_PROTOCOL_SERVER_COMPONENT, REFLECTOR_COMPONENT],
        'concurrent_announcers': 0,
        'use_upnp': False,
        'data_dir': '%s/lbrynet' % lbrynet_utils.getAppInternalStorageDir(service.getApplicationContext()),
        'lbryum_wallet_dir': '%s/lbryum' % lbrynet_utils.getAppInternalStorageDir(service.getApplicationContext()),
        'download_directory': '%s/Download' % lbrynet_utils.getInternalStorageDir(service.getApplicationContext())
    })

    log.info('Final Settings: %s', conf.settings.get_current_settings_dict())
    log.info("Starting lbrynet-daemon")

    if test_internet_connection():
        daemon = Daemon()
        daemon.start_listening()
        reactor.run()
    else:
        log.info("Not connected to the Internet. Unable to start.")


if __name__ == '__main__':
    start()
