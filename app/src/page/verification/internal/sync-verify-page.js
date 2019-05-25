import React from 'react';
import { Lbry } from 'lbry-redux';
import {
  ActivityIndicator,
  Dimensions,
  NativeModules,
  Text,
  TextInput,
  View
} from 'react-native';
import { BarPasswordStrengthDisplay } from 'react-native-password-strength-meter';
import Button from 'component/button';
import Link from 'component/link';
import Colors from 'styles/colors';
import Constants from 'constants';
import firstRunStyle from 'styles/firstRun';
import rewardStyle from 'styles/reward';


class SyncVerifyPage extends React.PureComponent {
  state = {
    checkSyncStarted: false,
    password: null,
    placeholder: 'password',
    syncApplyStarted: false,
    syncChecked: false,
  }

  componentDidMount() {
    const { checkSync, setEmailVerificationPhase } = this.props;

    this.setState({ checkSyncStarted: true }, () => checkSync());

    if (setEmailVerificationPhase) {
      setEmailVerificationPhase(false);
    }
  }

  onEnableSyncPressed = () => {
    const { syncApply, syncData, syncHash } = this.props;
    this.setState({ syncApplyStarted: true }, () => {
      syncApply(syncHash, syncData, this.state.password);
    });
  }

  componentWillReceiveProps(nextProps) {
    const { getSyncIsPending, syncApplyIsPending, syncApplyErrorMessage } = nextProps;
    const { setClientSetting, navigation, notify } = this.props;
    if (this.state.checkSyncStarted && !getSyncIsPending) {
      this.setState({ syncChecked: true });
    }

    if (this.state.syncApplyStarted && !syncApplyIsPending) {
      if (syncApplyErrorMessage && syncApplyErrorMessage.trim().length > 0) {
        notify({ message: syncApplyErrorMessage, isError: true });
        this.setState({ syncApplyStarted: false });
      } else {
        // password successfully verified
        if (NativeModules.UtilityModule) {
          NativeModules.UtilityModule.setSecureValue(Constants.KEY_FIRST_RUN_PASSWORD, this.state.password);
        }
        setClientSetting(Constants.SETTING_DEVICE_WALLET_SYNCED, true);
        Lbry.account_unlock({ password: this.state.password }).then(() => navigation.goBack()).catch(() => {
          notify({ message: 'Your wallet could not be unlocked. Please restart the app.' });
        });
      }
    }
  }

  handleChangeText = (text) => {
    // save the value to the state email
    const { onPasswordChanged } = this.props;
    this.setState({ password: text });
    if (onPasswordChanged) {
      onPasswordChanged(text);
    }
  }

  render() {
    const { hasSyncedWallet, syncApplyIsPending } = this.props;

    let paragraph;
    if (!hasSyncedWallet) {
      paragraph = (<Text style={firstRunStyle.paragraph}>Please enter a password to secure your account and wallet.</Text>);
    } else {
      paragraph = (<Text style={firstRunStyle.paragraph}>Please enter the password you used to secure your wallet.</Text>);
    }

    let content;
    if (!this.state.syncChecked) {
      content = (
        <View style={firstRunStyle.centered}>
          <ActivityIndicator size="large" color={Colors.White} style={firstRunStyle.waiting} />
          <Text style={firstRunStyle.paragraph}>Retrieving your account information...</Text>
        </View>
      );
    } else {
      content = (
        <View>
          <Text style={rewardStyle.verificationTitle}>Wallet Sync</Text>
          {paragraph}
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
          {(!hasSyncedWallet && this.state.password && this.state.password.trim().length) > 0 &&
            <View style={firstRunStyle.passwordStrength}>
              <BarPasswordStrengthDisplay
                width={Dimensions.get('window').width - 80}
                minLength={1}
                password={this.state.password} />
            </View>}
          <Text style={firstRunStyle.infoParagraph}>Note: for wallet security purposes, LBRY is unable to reset your password.</Text>

          <View style={rewardStyle.buttonContainer}>
            {!this.state.syncApplyStarted &&
              <Button
                style={rewardStyle.verificationButton}
                theme={"light"}
                text={"Enable sync"}
                onPress={this.onEnableSyncPressed} />}
            {syncApplyIsPending &&
              <View style={firstRunStyle.centerInside}>
                <ActivityIndicator size={"small"} color={Colors.White} />
              </View>}
          </View>
        </View>
      );
    }

    return (
      <View style={firstRunStyle.container}>
        {content}
      </View>
    );
  }
}

export default SyncVerifyPage;
