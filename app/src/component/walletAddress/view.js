// @flow
import React from 'react';
import { Text, View, Animated } from "react-native";
import QRCode from "react-native-qrcode";
import Colors from '../../styles/colors';
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
  state = {
    showQr: false
  }

  componentWillMount() {
    const { checkAddressIsMine, receiveAddress, getNewAddress } = this.props;
    if (!receiveAddress) {
      getNewAddress();
    } else {
      checkAddressIsMine(receiveAddress);
    }
  }

  renderQrAddress() {
    const { receiveAddress } = this.props;
    return <View>
      <QRCode
        value={receiveAddress}
        size={200}
        bgColor={Colors.LbryGreen}
        fgColor='white' />
    </View>
  }

  toggleQrView() {
    this.setState({showQr: !this.state.showQr});
  }

  render() {
    const { receiveAddress, getNewAddress, gettingNewAddress } = this.props;
    const { showQr } = this.state;
    const qrButtonText = showQr ? "Hide QR" : "Show QR";
    return (
      <View style={walletStyle.card}>
        <Text style={walletStyle.title}>Receive Credits</Text>
        <Text style={[walletStyle.text, walletStyle.bottomMarginMedium]}>Use this wallet address to receive credits sent by another user (or yourself).</Text>
        <Address address={receiveAddress} style={walletStyle.bottomMarginSmall} />

        <View style={[walletStyle.row, walletStyle.bottomMarginLarge ]}>
          <Button style={walletStyle.button}
            icon={'refresh'}
            text={'Get New Address'}
            onPress={getNewAddress}
            disabled={gettingNewAddress}
          />

          <Button style={walletStyle.button}
            icon={'refresh'}
            text={qrButtonText}
            onPress={() => this.toggleQrView()}
            disabled={gettingNewAddress}
          />
        </View>

        {showQr && <View style={[walletStyle.bottomMarginLarge, walletStyle.qrContainer]}>
          <QRCode
            value={receiveAddress}
            size={200}
            bgColor={Colors.LbryGreen}
            fgColor='white' />
        </View>}

        <Text style={walletStyle.smallText}>
          You can generate a new address at any time, and any previous addresses will continue to work. Using multiple addresses can be helpful for keeping track of incoming payments from multiple sources.
        </Text>
      </View>
    );
  }
}

export default WalletAddress;
