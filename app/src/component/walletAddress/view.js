// @flow
import React from 'react';
import { Text, View } from 'react-native';
import Address from '../address';
import Button from '../button';
import walletStyle from '../../styles/wallet';

type Props = {
  checkAddressIsMine: string => void,
  receiveAddress: string,
  getNewAddress: () => void,
  gettingNewAddress: boolean,
};

class WalletAddress extends React.PureComponent<Props> {
  componentWillMount() {
    const { checkAddressIsMine, receiveAddress, getNewAddress } = this.props;
    if (!receiveAddress) {
      getNewAddress();
    } else {
      checkAddressIsMine(receiveAddress);
    }
  }

  render() {
    const { receiveAddress, getNewAddress, gettingNewAddress } = this.props;

    return (
      <View style={walletStyle.card}>
        <Text style={walletStyle.title}>Receive Credits</Text>
        <Text style={[walletStyle.text, walletStyle.bottomMarginMedium]}>Use this wallet address to receive credits sent by another user (or yourself).</Text>
        <Address address={receiveAddress} style={walletStyle.bottomMarginSmall} />
        <Button style={[walletStyle.button, walletStyle.bottomMarginLarge]}
                icon={'sync'}
                text={'Get New Address'}
                onPress={getNewAddress}
                disabled={gettingNewAddress}
                />
        <Text style={walletStyle.smallText}>
          You can generate a new address at any time, and any previous addresses will continue to work. Using multiple addresses can be helpful for keeping track of incoming payments from multiple sources.
        </Text>
      </View>
    );
  }
}

export default WalletAddress;
