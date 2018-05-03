// @flow
import React from 'react';
import { regexAddress } from 'lbry-redux';
import { TextInput, Text, View } from 'react-native';
import Button from '../button';
import walletStyle from '../../styles/wallet';

type DraftTransaction = {
  address: string,
  amount: ?number, // So we can use a placeholder in the input
};

type Props = {
  sendToAddress: (string, number) => void,
  balance: number,
};

class WalletSend extends React.PureComponent<Props> {
  state = {
    amount: null,
    address: null
  };
  
  componentWillUpdate(nextProps) {
    const { draftTransaction, transactionError } = nextProps;
    if (transactionError && transactionError.trim().length > 0) {
      this.setState({ address: draftTransaction.address, amount: draftTransaction.amount });
    }
  }
  
  handleSend = () => {
    const { balance, sendToAddress, notify } = this.props;
    const { address, amount } = this.state;
    if (address && !regexAddress.test(address)) {
      notify({
        message: 'The recipient address is not a valid LBRY address.',
        displayType: ['toast']
      });
      return;
    }
    
    if (amount > balance) {
      notify({
        message: 'Insufficient credits',
        displayType: ['toast']
      });
      return;
    }
    
    if (amount && address) {
      sendToAddress(address, parseFloat(amount));
      this.setState({ address: null, amount: null });
    }
  }

  render() {
    const { balance } = this.props;
    const canSend = this.state.address &&
      this.state.amount > 0 &&
      this.state.address.trim().length > 0;

    return (
      <View style={walletStyle.card}>
        <Text style={walletStyle.title}>Send Credits</Text>
        <Text style={walletStyle.text}>Amount</Text>
        <View style={[walletStyle.amountRow, walletStyle.bottomMarginMedium]}>
          <TextInput onChangeText={value => this.setState({amount: value})}
                     keyboardType={'numeric'}
                     value={this.state.amount}
                     style={[walletStyle.input, walletStyle.amountInput]} />
          <Text style={[walletStyle.text, walletStyle.currency]}>LBC</Text>
        </View>
        <Text style={walletStyle.text}>Recipient address</Text>
        <View style={walletStyle.row}>
          <TextInput onChangeText={value => this.setState({address: value})}
                     placeholder={'bbFxRyXXXXXXXXXXXZD8nE7XTLUxYnddTs'}
                     value={this.state.address}
                     style={[walletStyle.input, walletStyle.addressInput, walletStyle.bottomMarginMedium]} />
          <Button text={'Send'}
                  style={[walletStyle.button, walletStyle.sendButton]}
                  disabled={!canSend}
                  onPress={this.handleSend} />
        </View>
      </View>
    );
  }
}

export default WalletSend;
