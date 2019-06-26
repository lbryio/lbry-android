import React from 'react';
import { Lbry } from 'lbry-redux';
import { ActivityIndicator, Linking, NativeModules, Platform, Switch, Text, TextInput, View } from 'react-native';
import AsyncStorage from '@react-native-community/async-storage';
import Button from 'component/button';
import Colors from 'styles/colors';
import Constants from 'constants';
import Icon from 'react-native-vector-icons/FontAwesome5';
import firstRunStyle from 'styles/firstRun';

class EmailVerifyPage extends React.PureComponent {
  onResendPressed = () => {
    const { email, notify, resendVerificationEmail } = this.props;
    resendVerificationEmail(email);
    AsyncStorage.setItem(Constants.KEY_EMAIL_VERIFY_PENDING, 'true');
    notify({ message: 'Please follow the instructions in the email sent to your address to continue.' });
  };

  render() {
    const { onEmailViewLayout, email } = this.props;

    const content = (
      <View onLayout={() => onEmailViewLayout('verify')}>
        <Text style={firstRunStyle.title}>Verify Email</Text>
        <Text style={firstRunStyle.paragraph}>
          An email has been sent to{' '}
          <Text style={firstRunStyle.nowrap} numberOfLines={1}>
            {email}
          </Text>
          . Please follow the instructions in the message to verify your email address.
        </Text>

        <View style={firstRunStyle.buttonContainer}>
          <Button
            style={firstRunStyle.verificationButton}
            theme={'light'}
            text={'Resend'}
            onPress={this.onResendPressed}
          />
        </View>
      </View>
    );

    return <View style={firstRunStyle.container}>{content}</View>;
  }
}

export default EmailVerifyPage;
