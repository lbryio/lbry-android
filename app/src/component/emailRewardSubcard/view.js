// @flow
import React from 'react';
import {
  ActivityIndicator,
  AsyncStorage,
  Text,
  TextInput,
  TouchableOpacity,
  View
} from 'react-native';
import Icon from 'react-native-vector-icons/FontAwesome5';
import Button from '../button';
import Colors from '../../styles/colors';
import Constants from '../../constants';
import Link from '../link';
import rewardStyle from '../../styles/reward';

class EmailRewardSubcard extends React.PureComponent {
  state = {
    email: null,
    verfiyStarted: false
  };

  componentDidMount() {
    const { emailToVerify } = this.props;
    AsyncStorage.getItem(Constants.KEY_FIRST_RUN_EMAIL).then(email => {
      if (email && email.trim().length > 0) {
        this.setState({ email });
      } else {
        this.setState({ email: emailToVerify });
      }
    });
  }

  componentWillReceiveProps(nextProps) {
    const { emailNewErrorMessage, emailNewPending } = nextProps;
    const { notify } = this.props;

    if (this.state.verifyStarted && !emailNewPending) {
      if (emailNewErrorMessage) {
        notify({ message: String(emailNewErrorMessage), displayType: ['toast']});
        this.setState({ verifyStarted: false });
      } else {
        notify({
          message: 'Please follow the instructions in the email sent to your address to continue.',
          displayType: ['toast']
        });
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

    const { addUserEmail, notify } = this.props;
    const { email } = this.state;
    if (!email || email.trim().length === 0 || email.indexOf('@') === -1) {
      return notify({
        message: 'Please provide a valid email address to continue.',
        displayType: ['toast'],
      });
    }

    this.setState({ verifyStarted: true });
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
