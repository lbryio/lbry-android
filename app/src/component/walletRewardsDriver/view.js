import React from 'react';
import { Text, TouchableOpacity } from 'react-native';
import walletStyle from 'styles/wallet';

class WalletRewardsDriver extends React.PureComponent<Props> {
  render() {
    const { navigation } = this.props;

    return (
      <TouchableOpacity style={walletStyle.rewardDriverCard} onPress={() => navigation.navigate('Rewards')}>
        <Text style={walletStyle.rewardDriverText}>Earn credits while using the LBRY app. Tap to get started.</Text>
      </TouchableOpacity>
    );
  }
}

export default WalletRewardsDriver;
