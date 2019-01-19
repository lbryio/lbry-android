import React from 'react';
import { ScrollView, Text, View } from 'react-native';
import TransactionListRecent from 'component/transactionListRecent';
import WalletAddress from 'component/walletAddress';
import WalletBalance from 'component/walletBalance';
import WalletSend from 'component/walletSend';
import Button from 'component/button';
import Link from 'component/link'
import Constants from 'constants';
import walletStyle from 'styles/wallet';

class WalletPage extends React.PureComponent {
  componentDidMount() {
    this.props.pushDrawerStack();
  }

  render() {
    const { understandsRisks, setClientSetting } = this.props;

    if (!understandsRisks) {
      return (
        <View>
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
      <ScrollView keyboardShouldPersistTaps={'handled'}>
        <View style={walletStyle.warningCard}>
          <Text style={walletStyle.warningText}>
            Please backup your wallet file using the instructions at <Link style={walletStyle.warningText} text="https://lbry.io/faq/how-to-backup-wallet#android" href="https://lbry.io/faq/how-to-backup-wallet#android" />.
          </Text>
        </View>

        <WalletBalance />
        <WalletAddress />
        <WalletSend />
        <TransactionListRecent navigation={this.props.navigation} />
      </ScrollView>
    );
  }
}

export default WalletPage;
