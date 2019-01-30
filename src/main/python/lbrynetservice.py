import keyring
import platform
from jnius import autoclass
from keyring.backend import KeyringBackend
from lbrynet import build_type
from lbrynet.extras.cli import conf, log_support, check_connection, Daemon, reactor
from lbrynet.extras.daemon.Components import DHT_COMPONENT, HASH_ANNOUNCER_COMPONENT, PEER_PROTOCOL_SERVER_COMPONENT
from lbrynet.extras.daemon.Components import REFLECTOR_COMPONENT


lbrynet_android_utils = autoclass('io.lbry.browser.Utils')
service = autoclass('io.lbry.browser.LbrynetService').serviceInstance
platform.platform = lambda: 'Android %s (API %s)' % (lbrynet_android_utils.getAndroidRelease(), lbrynet_android_utils.getAndroidSdk())
build_type.BUILD = 'dev' if lbrynet_android_utils.isDebug() else 'release'

# Keyring backend
class LbryAndroidKeyring(KeyringBackend):
    priority = 1

    def __init__(self):
        self._keystore = lbrynet_android_utils.initKeyStore(service.getApplicationContext())

    def set_password(self, servicename, username, password):
        context = service.getApplicationContext()
        lbrynet_android_utils.setPassword(servicename, username, password, context, self._keystore)

    def get_password(self, servicename, username):
        context = service.getApplicationContext()
        return lbrynet_android_utils.getPassword(servicename, username, context, self._keystore)

    def delete_password(self, servicename, username):
        context = service.getApplicationContext()
        lbrynet_android_utils.deletePassword(servicename, username, context, self._keystore)


def start():
    keyring.set_keyring(LbryAndroidKeyring())

    private_storage_dir = lbrynet_android_utils.getAppInternalStorageDir(service.getApplicationContext())
    conf.initialize_settings(
        data_dir=f'{private_storage_dir}/lbrynet',
        wallet_dir=f'{private_storage_dir}/lbryum',
        download_dir=f'{lbrynet_android_utils.getInternalStorageDir(service.getApplicationContext())}/Download'
    )
    conf.settings.update({
        'components_to_skip': [
            DHT_COMPONENT, HASH_ANNOUNCER_COMPONENT, PEER_PROTOCOL_SERVER_COMPONENT,
            REFLECTOR_COMPONENT
        ],
        'use_upnp': False,
        # 'use_https': True,     # TODO: does this work on android?
        # 'use_auth_http': True
    })

    log_support.configure_logging(conf.settings.get_log_filename(), True, [])
    log_support.configure_loggly_handler()

    if check_connection():
        daemon = Daemon()
        daemon.start_listening()
        reactor.run()
    else:
        print("Not connected to internet, unable to start")

if __name__ == '__main__':
    start()
