import React from 'react';
import { ScrollView, Text, View } from 'react-native';
import TransactionListRecent from '../../component/transactionListRecent';
import WalletAddress from '../../component/walletAddress';
import WalletBalance from '../../component/walletBalance';
import WalletSend from '../../component/walletSend';
import Button from '../../component/button';
import Constants from '../../constants';
import walletStyle from '../../styles/wallet';

class WalletPage extends React.PureComponent {
  render() {
    const { understandsRisks, setClientSetting } = this.props;
    
    if (!understandsRisks) {
      return (
        <View>
          <View style={walletStyle.warning}>
            <Text style={walletStyle.warningText}>
              This is alpha software. You may lose any LBC that you send to your wallet due to uninstallation, software bugs, deleted files, or malicious third-party software. You should not use this wallet as your primary wallet. If you understand the risks and you wish to continue, please click the button below.
            </Text>
          </View>
          <Button text={'I understand the risks'} style={[walletStyle.button, walletStyle.understand]}
                  onPress={() => setClientSetting(Constants.SETTING_ALPHA_UNDERSTANDS_RISKS, true)}/>
        </View>
      );
    }

    return (
      <ScrollView>
        <WalletBalance />
        <WalletAddress />
        <WalletSend />
        <TransactionListRecent navigation={this.props.navigation} />
      </ScrollView>
    );
  }
}

export default WalletPage;
