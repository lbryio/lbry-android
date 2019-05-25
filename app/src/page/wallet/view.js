import React from 'react';
import { NativeModules, ScrollView, Text, View } from 'react-native';
import TransactionListRecent from 'component/transactionListRecent';
import WalletAddress from 'component/walletAddress';
import WalletBalance from 'component/walletBalance';
import WalletSend from 'component/walletSend';
import WalletRewardsDriver from 'component/walletRewardsDriver';
import WalletSyncDriver from 'component/walletSyncDriver';
import Button from 'component/button';
import Link from 'component/link';
import UriBar from 'component/uriBar';
import Constants from 'constants';
import walletStyle from 'styles/wallet';

class WalletPage extends React.PureComponent {
  componentDidMount() {
    this.props.pushDrawerStack();

    const { getSync, user } = this.props;
    if (user && user.has_verified_email) {
      NativeModules.UtilityModule.getSecureValue(Constants.KEY_FIRST_RUN_PASSWORD).then(walletPassword => {
        if (walletPassword && walletPassword.trim().length > 0) {
          getSync(walletPassword);
        }
      });
    }
  }

  onDismissBackupPressed = () => {
    const { setClientSetting } = this.props;
    setClientSetting(Constants.SETTING_BACKUP_DISMISSED, true);
  }

  render() {
    const {
      balance,
      backupDismissed,
      hasSyncedWallet,
      rewardsNotInterested,
      understandsRisks,
      setClientSetting,
      navigation
    } = this.props;

    if (!understandsRisks) {
      return (
        <View>
          <UriBar navigation={navigation} />
          <View style={walletStyle.warning}>
            <Text style={walletStyle.warningParagraph}>
              This is beta software. You may lose any credits that you send to your wallet due to software bugs, deleted files, or malicious third-party software. You should not use this wallet as your primary wallet.
            </Text>
            {!hasSyncedWallet &&
            <Text style={walletStyle.warningParagraph}>
              If you are not using the LBRY sync service, you will lose all of your credits if you uninstall this application. Instructions on how to enroll as well as how to backup your wallet manually are available on the next page.
            </Text>}
            <Text style={walletStyle.warningText}>
              If you understand the risks and you wish to continue, please tap the button below.
            </Text>
          </View>
          <Button text={'I understand the risks'} style={[walletStyle.button, walletStyle.understand]}
                  onPress={() => setClientSetting(Constants.SETTING_ALPHA_UNDERSTANDS_RISKS, true)}/>
        </View>
      );
    }

    return (
      <View style={walletStyle.container}>
        <UriBar navigation={navigation} />
        <ScrollView style={walletStyle.scrollContainer} keyboardShouldPersistTaps={'handled'}>
          <WalletSyncDriver navigation={navigation} />
          {(!rewardsNotInterested) && (!balance || balance === 0) && <WalletRewardsDriver navigation={navigation} />}
          <WalletBalance />
          <WalletAddress />
          <WalletSend />
          <TransactionListRecent navigation={navigation} />
        </ScrollView>
      </View>
    );
  }
}

export default WalletPage;
