import React from 'react';
import { Lbry } from 'lbry-redux';
import {
  ActivityIndicator,
  Linking,
  NativeModules,
  Platform,
  Text,
  TextInput,
  View
} from 'react-native';
import AsyncStorage from '@react-native-community/async-storage';
import Colors from 'styles/colors';
import Constants from 'constants';
import firstRunStyle from 'styles/firstRun';

class WalletPage extends React.PureComponent {
  state = {
    password: null,
    placeholder: 'password',
    statusTries: 0
  };

  handleChangeText = (text) => {
    // save the value to the state email
    const { onPasswordChanged } = this.props;
    this.setState({ password: text });
    if (onPasswordChanged) {
      onPasswordChanged(text);
    }

    if (NativeModules.UtilityModule) {
      NativeModules.UtilityModule.setSecureValue(Constants.KEY_FIRST_RUN_PASSWORD, text);
      // simply set any string value to indicate that a passphrase was set on first run
      AsyncStorage.setItem(Constants.KEY_FIRST_RUN_PASSWORD, "true");
    }
  }

  render() {
    const { onPasswordChanged, onWalletViewLayout } = this.props;

    const content = (
      <View onLayout={onWalletViewLayout}>
        <Text style={firstRunStyle.title}>Password</Text>
        <Text style={firstRunStyle.paragraph}>Please enter a password to secure your account and wallet.</Text>
        <TextInput style={firstRunStyle.passwordInput}
          placeholder={this.state.placeholder}
          underlineColorAndroid="transparent"
          secureTextEntry={true}
          value={this.state.password}
          onChangeText={text => this.handleChangeText(text)}
          onFocus={() => {
            if (!this.state.password || this.state.password.length === 0) {
              this.setState({ placeholder: '' });
            }
          }}
          onBlur={() => {
            if (!this.state.password || this.state.password.length === 0) {
              this.setState({ placeholder: 'password' });
            }
          }}
          />
        <Text style={firstRunStyle.infoParagraph}>Note: for wallet security purposes, LBRY is unable to reset your password.</Text>
      </View>
    );

    return (
      <View style={firstRunStyle.container}>
        {content}
      </View>
    );
  }
}

export default WalletPage;
