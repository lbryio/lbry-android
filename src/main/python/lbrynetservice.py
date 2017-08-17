import platform

# logging override (until we can figure out why logging doesn't work normally)
import logging
from kivy.logger import Logger
class InternalLogger(logging.Logger):
    def __init__(self, name, level=logging.DEBUG):
        self.name = name
        return super(InternalLogger, self).__init__(name, level)

    def debug(self, msg, *args, **kwargs):
        msg = '%s: %s' % (self.name, msg)
        Logger.debug(msg, *args, **kwargs)

    def info(self, msg, *args, **kwargs):
        msg = '%s: %s' % (self.name, msg)
        Logger.info(msg, *args, **kwargs)

    def warning(self, msg, *args, **kwargs):
        msg = '%s: %s' % (self.name, msg)
        Logger.warning(msg, *args, **kwargs)

    def error(self, msg, *args, **kwargs):
        msg = '%s: %s' % (self.name, msg)
        Logger.error(msg, *args, **kwargs)

    def critical(self, msg, *args, **kwargs):
        msg = '%s: %s' % (self.name, msg)
        Logger.critical(msg, *args, **kwargs)

    def log(self, lvl, msg, *args, **kwargs):
        msg = '%s: %s' % (self.name, msg)
        Logger.log(lvl, msg, *args, **kwargs)

    def exception(self, msg, *args, **kwargs):
        msg = '%s: %s' % (self.name, msg)
        Logger.exception(msg, *args, **kwargs)

    # required by twisted for some reason.
    def fail(self, *args, **kwargs):
        pass

def getLoggerOverride(name='root', loglevel=logging.DEBUG):
    return InternalLogger(name, loglevel)

logging.getLogger = getLoggerOverride

from lbrynet.core import log_support

from twisted.internet import defer, reactor
from jsonrpc.proxy import JSONRPCProxy

from lbrynet import analytics
from lbrynet import conf
from lbrynet.core import utils, system_info
from lbrynet.daemon.auth.client import LBRYAPIClient
from lbrynet.daemon.DaemonServer import DaemonServer

import kivy
import ssl

# Fixes / patches / overrides
# platform.platform() in libc_ver: IOError: [Errno 21] Is a directory
if (kivy.platform == 'android'):
    from jnius import autoclass
    util = autoclass('io.lbry.lbrynet.Utils')
    platform.platform = lambda: 'Android %s (API %s)' % (util.getAndroidRelease(), util.getAndroidSdk())

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
