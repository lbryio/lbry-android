import React from 'react';
import { NativeModules, ScrollView, Text, View } from 'react-native';
import TransactionListRecent from 'component/transactionListRecent';
import WalletRewardsDriver from 'component/walletRewardsDriver';
import WalletAddress from 'component/walletAddress';
import WalletBalance from 'component/walletBalance';
import WalletSend from 'component/walletSend';
import Button from 'component/button';
import Link from 'component/link';
import UriBar from 'component/uriBar';
import Constants from 'constants';
import walletStyle from 'styles/wallet';

class WalletPage extends React.PureComponent {
  componentDidMount() {
    this.props.pushDrawerStack();

    const { user, getSync } = this.props;
    if (user && user.has_verified_email) {
      NativeModules.UtilityModule.getSecureValue(Constants.KEY_FIRST_RUN_PASSWORD).then(walletPassword => getSync(walletPassword));
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
            <Text style={walletStyle.warningText}>
              This is beta software. You may lose any LBC that you send to your wallet due to uninstallation, software bugs, deleted files, or malicious third-party software. You should not use this wallet as your primary wallet. If you understand the risks and you wish to continue, please tap the button below.
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
          {!backupDismissed &&
          <View style={walletStyle.warningCard}>
            <Text style={walletStyle.warningText}>
              Please backup your wallet file using the instructions at <Link style={walletStyle.warningText} text="https://lbry.com/faq/how-to-backup-wallet#android" href="https://lbry.com/faq/how-to-backup-wallet#android" />.
            </Text>
            <Button text={'Dismiss'} style={walletStyle.button} onPress={this.onDismissBackupPressed} />
          </View>}

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
