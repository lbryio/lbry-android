// @flow
import React from 'react';
import { Image, Text, View } from 'react-native';
import { formatCredits } from 'lbry-redux'
import Address from '../address';
import Button from '../button';
import walletStyle from '../../styles/wallet';

type Props = {
  balance: number,
};

class WalletBalance extends React.PureComponent<Props> {
  render() {
    const { balance } = this.props;
    return (
      <View style={walletStyle.balanceCard}>
        <Image style={walletStyle.balanceBackground} resizeMode={'cover'} source={require('../../assets/stripe.png')} />
        <Text style={walletStyle.balanceTitle}>Balance</Text>
        <Text style={walletStyle.balanceCaption}>You currently have</Text>
        <Text style={walletStyle.balance}>
          {(balance || balance === 0) && (formatCredits(balance, 2) + ' LBC')}
        </Text>
      </View>
    );
  }
}

export default WalletBalance;
