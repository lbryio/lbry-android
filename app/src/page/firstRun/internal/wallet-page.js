import React from 'react';
import { Lbry } from 'lbry-redux';
import {
  ActivityIndicator,
  Dimensions,
  Linking,
  NativeModules,
  Platform,
  Text,
  TextInput,
  TouchableOpacity,
  View,
} from 'react-native';
import { BarPasswordStrengthDisplay } from 'react-native-password-strength-meter';
import AsyncStorage from '@react-native-community/async-storage';
import Colors from 'styles/colors';
import Constants from 'constants';
import firstRunStyle from 'styles/firstRun';
import Icon from 'react-native-vector-icons/FontAwesome5';

const firstRunMargins = 80;

class WalletPage extends React.PureComponent {
  state = {
    password: null,
    placeholder: 'password',
    statusTries: 0,
    walletReady: false,
    hasCheckedSync: false,
    revealPassword: false,
  };

  componentDidMount() {
    this.checkWalletReady();
  }

  checkWalletReady = () => {
    // make sure the sdk wallet component is ready
    Lbry.status()
      .then(status => {
        if (status.startup_status && status.startup_status.wallet) {
          this.setState({ walletReady: true }, () => {
            this.props.checkSync();
            setTimeout(() => this.setState({ hasCheckedSync: true }), 1000);
          });
          return;
        }
        setTimeout(this.checkWalletReady, 1000);
      })
      .catch(e => {
        setTimeout(this.checkWalletReady, 1000);
      });
  };

  handleChangeText = text => {
    // save the value to the state email
    const { onPasswordChanged } = this.props;
    this.setState({ password: text });
    if (onPasswordChanged) {
      onPasswordChanged(text);
    }
  };

  render() {
    const { onPasswordChanged, onWalletViewLayout, getSyncIsPending, hasSyncedWallet, syncApplyIsPending } = this.props;

    let content;
    if (!this.state.walletReady || !this.state.hasCheckedSync || getSyncIsPending) {
      content = (
        <View style={firstRunStyle.centered}>
          <ActivityIndicator size="large" color={Colors.White} style={firstRunStyle.waiting} />
          <Text style={firstRunStyle.paragraph}>Retrieving your account information...</Text>
        </View>
      );
    } else if (syncApplyIsPending) {
      content = (
        <View style={firstRunStyle.centered}>
          <ActivityIndicator size="large" color={Colors.White} style={firstRunStyle.waiting} />
          <Text style={firstRunStyle.paragraph}>Validating password...</Text>
        </View>
      );
    } else {
      content = (
        <View onLayout={onWalletViewLayout}>
          <Text style={firstRunStyle.title}>Password</Text>
          <Text style={firstRunStyle.paragraph}>
            {hasSyncedWallet
              ? 'Please enter the password you used to secure your wallet.'
              : 'Please enter a password to secure your account and wallet.'}
          </Text>
          <View style={firstRunStyle.passwordInputContainer}>
            <TextInput
              style={firstRunStyle.passwordInput}
              placeholder={this.state.placeholder}
              underlineColorAndroid="transparent"
              selectionColor={Colors.NextLbryGreen}
              secureTextEntry={!this.state.revealPassword}
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
            <TouchableOpacity
              style={firstRunStyle.revealPasswordIcon}
              onPress={() => this.setState({ revealPassword: !this.state.revealPassword })}
            >
              <Icon name={this.state.revealPassword ? 'eye-slash' : 'eye'} size={16} style={firstRunStyle.revealIcon} />
            </TouchableOpacity>
          </View>
          {(!this.state.password || this.state.password.trim().length === 0) && (
            <View style={firstRunStyle.passwordWarning}>
              <Text style={firstRunStyle.passwordWarningText}>
                {hasSyncedWallet
                  ? 'If you did not provide a password, please press Use LBRY to continue.'
                  : 'You can proceed without a password, but this is not recommended.'}
              </Text>
            </View>
          )}

          {(!hasSyncedWallet && this.state.password && this.state.password.trim().length) > 0 && (
            <View style={firstRunStyle.passwordStrength}>
              <BarPasswordStrengthDisplay
                width={Dimensions.get('window').width - firstRunMargins}
                minLength={1}
                password={this.state.password}
              />
            </View>
          )}
          <Text style={firstRunStyle.infoParagraph}>
            Note: for wallet security purposes, LBRY is unable to reset your password.
          </Text>
        </View>
      );
    }

    return <View style={firstRunStyle.container}>{content}</View>;
  }
}

export default WalletPage;
