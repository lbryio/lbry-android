import asyncio
import keyring
import logging
import pathlib
import platform
import sys
from jnius import autoclass
from keyring.backend import KeyringBackend
from lbrynet import __version__ as lbrynet_version, build_type
from lbrynet.conf import Config
from lbrynet.extras.daemon.loggly_handler import get_loggly_handler
from lbrynet.extras.daemon.Components import DHT_COMPONENT, HASH_ANNOUNCER_COMPONENT, PEER_PROTOCOL_SERVER_COMPONENT
from lbrynet.extras.daemon.Daemon import Daemon
from lbrynet.extras.daemon.loggly_handler import get_loggly_handler
from lbrynet.utils import check_connection

log = logging.getLogger(__name__)
log.setLevel(logging.DEBUG)

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

def ensure_directory_exists(path: str):
    if not os.path.isdir(path):
        pathlib.Path(path).mkdir(parents=True, exist_ok=True)

def configure_logging(conf):
    default_formatter = logging.Formatter("%(asctime)s %(levelname)-8s %(name)s:%(lineno)d: %(message)s")

    file_handler = logging.handlers.RotatingFileHandler(
        conf.log_file_path, maxBytes=2097152, backupCount=5
    )
    file_handler.setFormatter(default_formatter)
    logging.getLogger('lbrynet').addHandler(file_handler)
    logging.getLogger('torba').addHandler(file_handler)

    handler = logging.StreamHandler()
    handler.setFormatter(default_formatter)

    log.addHandler(handler)
    logging.getLogger('lbrynet').addHandler(handler)
    logging.getLogger('torba').addHandler(handler)

    logging.getLogger('aioupnp').setLevel(logging.WARNING)
    logging.getLogger('aiohttp').setLevel(logging.CRITICAL)
    logging.getLogger('lbrynet').setLevel(logging.DEBUG if lbrynet_android_utils.isDebug() else logging.INFO)
    logging.getLogger('torba').setLevel(logging.INFO)

    loggly_handler = get_loggly_handler()
    loggly_handler.setLevel(logging.ERROR)
    logging.getLogger('lbrynet').addHandler(loggly_handler)

def start():
    keyring.set_keyring(LbryAndroidKeyring())
    private_storage_dir = lbrynet_android_utils.getAppInternalStorageDir(service.getApplicationContext())
    conf = Config(
        data_dir=f'{private_storage_dir}/lbrynet',
        wallet_dir=f'{private_storage_dir}/lbryum',
        download_dir=f'{lbrynet_android_utils.getInternalStorageDir(service.getApplicationContext())}/Download',
        components_to_skip=[DHT_COMPONENT, HASH_ANNOUNCER_COMPONENT, PEER_PROTOCOL_SERVER_COMPONENT],
        use_upnp=False
    )

    for directory in (conf.data_dir, conf.download_dir, conf.wallet_dir):
        ensure_directory_exists(directory)

    configure_logging(conf)

    log.info('Starting lbry sdk {}'.format(lbrynet_version));

    loop = asyncio.get_event_loop()
    loop.set_debug(lbrynet_android_utils.isDebug())

    daemon = Daemon(conf)
    try:
        loop.run_until_complete(daemon.start())
        loop.run_until_complete(daemon.stop_event.wait())
    except (GracefulExit):
        pass
    finally:
        loop.run_until_complete(daemon.stop())
    if hasattr(loop, 'shutdown_asyncgens'):
        loop.run_until_complete(loop.shutdown_asyncgens())

if __name__ == '__main__':
    start()
