import React from 'react';
import { ScrollView } from 'react-native';
import TransactionListRecent from '../../component/transactionListRecent';
import WalletAddress from '../../component/walletAddress';
import WalletBalance from '../../component/walletBalance';
import WalletSend from '../../component/walletSend';

class WalletPage extends React.PureComponent {
  render() {
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
