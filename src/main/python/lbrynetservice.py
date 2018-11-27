import keyring
from keyring.backend import KeyringBackend
from jnius import autoclass
from lbrynet.extras.cli import start_daemon
from lbrynet.extras.daemon.Components import DHT_COMPONENT, HASH_ANNOUNCER_COMPONENT, PEER_PROTOCOL_SERVER_COMPONENT
from lbrynet.extras.daemon.Components import REFLECTOR_COMPONENT


lbrynet_android_utils = autoclass('io.lbry.browser.Utils')
service = autoclass('io.lbry.browser.LbrynetService').serviceInstance


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


def get_android_internal_storage() -> str:
    return lbrynet_android_utils.getAppExternalStorageDir(service.getApplicationContext())


def start():
    keyring.set_keyring(LbryAndroidKeyring())
    return start_daemon(settings={
        'components_to_skip': [DHT_COMPONENT, HASH_ANNOUNCER_COMPONENT, PEER_PROTOCOL_SERVER_COMPONENT,
                               REFLECTOR_COMPONENT],
        'use_upnp': False,
        'use_https': False,
        'use_auth_http': True
    }, get_android_internal_storage=get_android_internal_storage)


if __name__ == '__main__':
    start()
