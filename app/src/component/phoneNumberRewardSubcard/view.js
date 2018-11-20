// @flow
import React from 'react';
import {
  ActivityIndicator,
  AsyncStorage,
  DeviceEventEmitter,
  NativeModules,
  StyleSheet,
  Text,
  TextInput,
  TouchableOpacity,
  View
} from 'react-native';
import Button from '../button';
import Colors from '../../styles/colors';
import Constants from '../../constants';
import CountryPicker from 'react-native-country-picker-modal';
import Icon from 'react-native-vector-icons/FontAwesome5';
import Link from '../link';
import PhoneInput from 'react-native-phone-input';
import rewardStyle from '../../styles/reward';

class PhoneNumberRewardSubcard extends React.PureComponent {
  phoneInput = null;

  picker = null;

  constructor(props) {
    super(props);
    this.state = {
      canReceiveSms: false,
      cca2: 'US',
      codeVerifyStarted: false,
      codeVerifySuccessful: false,
      countryCode: null,
      newPhoneAdded: false,
      number: null,
      verificationCode: null,
    };
  }

  componentDidMount() {
    DeviceEventEmitter.addListener('onReceiveSmsPermissionGranted', this.receiveSmsPermissionGranted);
    DeviceEventEmitter.addListener('onVerificationCodeReceived', this.receiveVerificationCode);

    const { phone } = this.props;
    if (phone && String(phone).trim().length > 0) {
      this.setState({ newPhoneAdded: true });
    }

    if (NativeModules.UtilityModule) {
      NativeModules.UtilityModule.canReceiveSms().then(canReceiveSms => this.setState({ canReceiveSms }));
    }
  }

  componentWillUnmount() {
    DeviceEventEmitter.removeListener('onReceiveSmsPermissionGranted', this.receiveSmsPermissionGranted);
    DeviceEventEmitter.removeListener('onVerificationCodeReceived', this.receiveVerificationCode);
  }

  componentDidUpdate(prevProps) {
    const {
      phoneVerifyIsPending,
      phoneVerifyErrorMessage,
      notify,
      phoneNewErrorMessage,
      phoneNewIsPending,
      onPhoneVerifySuccessful
    } = this.props;

    if (!phoneNewIsPending && (phoneNewIsPending !== prevProps.phoneNewIsPending)) {
      if (phoneNewErrorMessage) {
        notify({ message: String(phoneNewErrorMessage) });
      } else {
        this.setState({ newPhoneAdded: true });
      }
    }
    if (!phoneVerifyIsPending && (phoneVerifyIsPending !== prevProps.phoneVerifyIsPending)) {
      if (phoneVerifyErrorMessage) {
        notify({ message: String(phoneVerifyErrorMessage) });
        this.setState({ codeVerifyStarted: false });
      } else {
        notify({ message: 'Your phone number was successfully verified.' });
        this.setState({ codeVerifySuccessful: true });
        if (onPhoneVerifySuccessful) {
          onPhoneVerifySuccessful();
        }
      }
    }
  }

  receiveSmsPermissionGranted = () => {
    this.setState({ canReceiveSms: true });
  }

  receiveVerificationCode = (evt) => {
    if (!this.state.newPhoneAdded || this.state.codeVerifySuccessful) {
      return;
    }

    const { verifyPhone } = this.props;
    this.setState({ codeVerifyStarted: true });
    verifyPhone(evt.code);
  }

  onAllowAccessPressed = () => {
    if (!NativeModules.UtilityModule) {
      return notify({
        message: 'The required permission could not be obtained due to a missing module.',
      });
    }

    NativeModules.UtilityModule.requestReceiveSmsPermission();
  }

  onSendTextPressed = () => {
    const { addUserPhone, notify } = this.props;

    if (!this.phoneInput.isValidNumber()) {
      return notify({
        message: 'Please provide a valid telephone number.',
      });
    }

    const countryCode = this.phoneInput.getCountryCode();
    const number = this.phoneInput.getValue().replace('+' + countryCode, '');
    this.setState({ countryCode, number });
    addUserPhone(number, countryCode);
  }

  onVerifyPressed = () => {
    if (this.state.codeVerifyStarted) {
      return;
    }

    const { verifyPhone } = this.props;
    this.setState({ codeVerifyStarted: true });
    verifyPhone(this.state.verificationCode);
  }

  onPressFlag = () => {
    if (this.picker) {
      this.picker.openModal();
    }
  }

  selectCountry(country) {
    this.phoneInput.selectCountry(country.cca2.toLowerCase());
    this.setState({ cca2: country.cca2 });
  }

  handleChangeText = (text) => {
    this.setState({ verificationCode: text });
  };

  render() {
    const {
      phoneVerifyIsPending,
      phoneVerifyErrorMessage,
      phone,
      phoneErrorMessage,
      phoneNewIsPending
    } = this.props;

    if (this.state.codeVerifySuccessful) {
      return null;
    }

    return (
      <View style={rewardStyle.subcard}>
        <Text style={rewardStyle.subtitle}>Pending action: Verify Phone Number</Text>
        {!this.state.canReceiveSms &&
          <View style={rewardStyle.smsPermissionContainer}>
            <Text style={[rewardStyle.bottomMarginMedium, rewardStyle.subcardText]}>
              You can grant access to the receive SMS permission in order to verify phone number. Alternatively, you can enter the verification code manually.
            </Text>
            <Button
              style={rewardStyle.actionButton}
              text={"Allow access"}
              onPress={this.onAllowAccessPressed}
            />
          </View>}
        <View style={rewardStyle.phoneVerificationContainer}>
          {!this.state.newPhoneAdded &&
            <View>
              <Text style={[rewardStyle.bottomMarginMedium, rewardStyle.subcardText]}>Please enter your phone number to continue.</Text>
              <PhoneInput
                ref={(ref) => { this.phoneInput = ref; }}
                style={StyleSheet.flatten(rewardStyle.phoneInput)}
                textProps={{ placeholder: '(phone number)' }}
                textStyle={StyleSheet.flatten(rewardStyle.phoneInputText)}
                onPressFlag={this.onPressFlag} />
              {!phoneNewIsPending &&
                <Button
                  style={[rewardStyle.actionButton, rewardStyle.topMarginMedium]}
                  text={"Send verification text"}
                  onPress={this.onSendTextPressed} />}
              {phoneNewIsPending &&
                <ActivityIndicator
                  style={[rewardStyle.loading, rewardStyle.topMarginMedium]}
                  size="small"
                  color={Colors.LbryGreen} />}
            </View>}
          {this.state.newPhoneAdded &&
            <View>
              {!phoneVerifyIsPending && !this.codeVerifyStarted &&
                <View>
                  <Text style={[rewardStyle.bottomMarginSmall, rewardStyle.subcardText]}>
                    Please enter the verification code.
                  </Text>
                  <TextInput
                    style={rewardStyle.verificationCodeInput}
                    keyboardType="numeric"
                    placeholder="0000"
                    underlineColorAndroid="transparent"
                    value={this.state.verificationCode}
                    onChangeText={text => this.handleChangeText(text)}
                  />
                  <Button
                    style={[rewardStyle.actionButton, rewardStyle.topMarginSmall ]}
                    text={"Verify"}
                    onPress={this.onVerifyPressed} />
                </View>
              }
              {phoneVerifyIsPending &&
                <View>
                  <Text style={rewardStyle.subcardText}>Verifying your phone number...</Text>
                  <ActivityIndicator
                    color={Colors.LbryGreen}
                    size="small"
                    style={[rewardStyle.loading, rewardStyle.topMarginMedium]} />
                </View>}
            </View>
          }
        </View>

        <CountryPicker
          ref={(picker) => { this.picker = picker; }}
          cca2={this.state.cca2}
          filterable={true}
          onChange={value => this.selectCountry(value)}
          showCallingCode={true}
          translation="eng">
          <View />
        </CountryPicker>
      </View>
    );
  }
};

export default PhoneNumberRewardSubcard;
