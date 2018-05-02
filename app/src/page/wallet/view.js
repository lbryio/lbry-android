import React from 'react';
import { View } from 'react-native';
import WalletBalance from '../../component/walletBalance';
import WalletAddress from '../../component/walletAddress';

class WalletPage extends React.PureComponent {
  render() {
    return (
      <View>
        <WalletBalance />
        <WalletAddress />
      </View>
    );
  }
}

export default WalletPage;
