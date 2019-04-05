// @flow
import React from 'react';
import {
  ActivityIndicator,
  Text,
  TextInput,
  TouchableOpacity,
  View
} from 'react-native';
import AsyncStorage from '@react-native-community/async-storage';
import Icon from 'react-native-vector-icons/FontAwesome5';
import Button from 'component/button';
import Colors from 'styles/colors';
import Constants from 'constants';
import Link from 'component/link';
import rewardStyle from 'styles/reward';

class EmailRewardSubcard extends React.PureComponent {
  constructor(props) {
    super(props);
    this.state = {
      email: null,
      emailAlreadySet: false,
      previousEmail: null,
      verfiyStarted: false
    };
  }

  componentDidMount() {
    const { setEmailToVerify } = this.props;
    AsyncStorage.getItem(Constants.KEY_FIRST_RUN_EMAIL).then(email => setEmailToVerify(email));
  }

  componentWillReceiveProps(nextProps) {
    const { emailNewErrorMessage, emailNewPending, emailToVerify } = nextProps;
    const { notify } = this.props;

    if (emailToVerify && emailToVerify.trim().length > 0 && !this.state.email && !this.state.previousEmail) {
      this.setState({ email: emailToVerify, previousEmail: emailToVerify, emailAlreadySet: true });
    }

    if (this.state.verifyStarted && !emailNewPending) {
      if (emailNewErrorMessage) {
        notify({ message: String(emailNewErrorMessage), isError: true });
        this.setState({ verifyStarted: false });
      } else {
        notify({ message: 'Please follow the instructions in the email sent to your address to continue.' });
        AsyncStorage.setItem(Constants.KEY_EMAIL_VERIFY_PENDING, 'true');
      }
    }
  }

  handleChangeText = (text) => {
    // save the value to the state email
    this.setState({ email: text });
    AsyncStorage.setItem(Constants.KEY_FIRST_RUN_EMAIL, text);
  }

  onSendVerificationPressed = () => {
    if (this.state.verifyStarted) {
      return;
    }

    const { addUserEmail, notify, resendVerificationEmail } = this.props;
    const { email } = this.state;
    if (!email || email.trim().length === 0 || email.indexOf('@') === -1) {
      return notify({
        message: 'Please provide a valid email address to continue.',
      });
    }

    this.setState({ verifyStarted: true });
    if (this.state.emailAlreadySet && this.state.previousEmail === email) {
      // resend verification email if there was one previously set (and it wasn't changed)
      resendVerificationEmail(email);
      AsyncStorage.setItem(Constants.KEY_EMAIL_VERIFY_PENDING, 'true');
      notify({ message: 'Please follow the instructions in the email sent to your address to continue.' });
      return;
    }

    addUserEmail(email);
  }

  render() {
    const { emailNewPending } = this.props;

    return (
      <View style={rewardStyle.subcard}>
        <Text style={rewardStyle.subtitle}>Pending action: Verify Email</Text>
        <Text style={rewardStyle.subcardText}>Please provide an email address to verify. If you received a link previously, please follow the instructions in the email to complete verification.</Text>
        <TextInput style={rewardStyle.subcardTextInput}
                   placeholder="you@example.com"
                   underlineColorAndroid="transparent"
                   value={this.state.email}
                   onChangeText={text => this.handleChangeText(text)} />
        {!this.state.verifyStarted && <Button style={rewardStyle.actionButton}
                text={"Send verification email"}
                onPress={this.onSendVerificationPressed} />}
        {this.state.verifyStarted && emailNewPending &&
          <ActivityIndicator size={"small"} color={Colors.LbryGreen} style={rewardStyle.loading} />}
      </View>
    );
  }
};

export default EmailRewardSubcard;
