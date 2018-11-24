// @flow
import React from 'react';
import { regexAddress } from 'lbry-redux';
import { Alert, TextInput, Text, View } from 'react-native';
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
  amountInput = null;

  state = {
    amount: null,
    address: null,
    addressChanged: false,
    addressValid: false
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
      });
      return;
    }

    if (amount > balance) {
      notify({
        message: 'Insufficient credits',
      });
      return;
    }

    if (amount && address) {
      // Show confirmation before send
      Alert.alert(
      'Send LBC',
      `Are you sure you want to send ${amount} LBC to ${address}?`,
      [
        { text: 'No' },
        { text: 'Yes', onPress: () => {
          sendToAddress(address, parseFloat(amount));
          this.setState({ address: null, amount: null });
        }}
      ]);
    }
  }

  handleAddressInputBlur = () => {
    if (this.state.addressChanged && !this.state.addressValid) {
      const { notify } = this.props;
      notify({
        message: 'The recipient address is not a valid LBRY address.',
      });
    }
  }

  handleAddressInputSubmit = () => {
    if (this.amountInput) {
      this.amountInput.focus();
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
        <Text style={walletStyle.text}>Recipient address</Text>
        <View style={[walletStyle.row, walletStyle.bottomMarginMedium]}>
          <TextInput onChangeText={value => this.setState({
                       address: value,
                       addressChanged: true,
                       addressValid: (value.trim().length == 0 || regexAddress.test(value))
                     })}
                     onBlur={this.handleAddressInputBlur}
                     onSubmitEditing={this.handleAddressInputSubmit}
                     placeholder={'bbFxRyXXXXXXXXXXXZD8nE7XTLUxYnddTs'}
                     value={this.state.address}
                     returnKeyType={'next'}
                     style={[walletStyle.input, walletStyle.addressInput, walletStyle.bottomMarginMedium]} />
        </View>
        <Text style={walletStyle.text}>Amount</Text>
        <View style={walletStyle.row}>
          <View style={walletStyle.amountRow}>
            <TextInput ref={ref => this.amountInput = ref}
                       onChangeText={value => this.setState({amount: value})}
                       keyboardType={'numeric'}
                       value={this.state.amount}
                       style={[walletStyle.input, walletStyle.amountInput]} />
            <Text style={[walletStyle.text, walletStyle.currency]}>LBC</Text>
          </View>
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
