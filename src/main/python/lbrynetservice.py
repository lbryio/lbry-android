import platform
import ssl

# Fixes / patches / overrides
# platform.platform() in libc_ver: IOError: [Errno 21] Is a directory
from jnius import autoclass
lbrynet_utils = autoclass('io.lbry.lbrynet.Utils')
service = autoclass('io.lbry.lbrynet.LbrynetService').serviceInstance
platform.platform = lambda: 'Android %s (API %s)' % (lbrynet_utils.getAndroidRelease(), lbrynet_utils.getAndroidSdk())

import lbrynet.androidhelpers
lbrynet.androidhelpers.paths.android_files_dir = lambda: lbrynet_utils.getFilesDir(service.getApplicationContext())
lbrynet.androidhelpers.paths.android_internal_storage_dir = lambda: lbrynet_utils.getInternalStorageDir(service.getApplicationContext())
lbrynet.androidhelpers.paths.android_external_storage_dir = lambda: lbrynet_utils.getExternalStorageDir(service.getApplicationContext())
lbrynet.androidhelpers.paths.android_app_internal_storage_dir = lambda: lbrynet_utils.getAppInternalStorageDir(service.getApplicationContext())
lbrynet.androidhelpers.paths.android_app_external_storage_dir = lambda: lbrynet_utils.getAppExternalStorageDir(service.getApplicationContext())

import logging.handlers

from lbrynet.core import log_support
from twisted.internet import defer, reactor
from jsonrpc.proxy import JSONRPCProxy

from lbrynet import analytics
from lbrynet import conf
from lbrynet.core import utils, system_info
from lbrynet.daemon.auth.client import LBRYAPIClient
from lbrynet.daemon.DaemonServer import DaemonServer

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
    log.debug('Final Settings: %s', conf.settings.get_current_settings_dict())
    log.info("Starting lbrynet-daemon")

    if test_internet_connection():
        analytics_manager = analytics.Manager.new_instance()
        start_server_and_listen(False, analytics_manager)
        reactor.run()
    else:
        log.info("Not connected to internet, unable to start")

@defer.inlineCallbacks
def start_server_and_listen(use_auth, analytics_manager, max_tries=5):
    """The primary entry point for launching the daemon.
    Args:
        use_auth: set to true to enable http authentication
        analytics_manager: to send analytics
    """
    analytics_manager.send_server_startup()
    daemon_server = DaemonServer(analytics_manager)
    try:
        yield daemon_server.start(use_auth)
        analytics_manager.send_server_startup_success()
    except Exception as e:
        log.exception('Failed to startup')
        yield daemon_server.stop()
        analytics_manager.send_server_startup_error(str(e))
        reactor.fireSystemEvent("shutdown")


if __name__ == '__main__':
    start()
