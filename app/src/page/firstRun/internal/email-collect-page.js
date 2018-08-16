import React from 'react';
import { Lbry } from 'lbry-redux';
import {
  ActivityIndicator,
  AsyncStorage,
  Linking,
  Text,
  TextInput,
  View
} from 'react-native';
import Button from '../../../component/button';
import Colors from '../../../styles/colors';
import Constants from '../../../constants';
import firstRunStyle from '../../../styles/firstRun';

class EmailCollectPage extends React.PureComponent {
  static MAX_STATUS_TRIES = 15;

  constructor() {
    super();
    this.state = {
      email: null,
      authenticationStarted: false,
      authenticationFailed: false,
      statusTries: 0
    };
  }

  componentWillReceiveProps(nextProps) {
    const { authenticating, authToken } = this.props;

    if (this.state.authenticationStarted && !authenticating && authToken === null) {
      this.setState({ authenticationFailed: true, authenticationStarted: false });
    }
  }

  componentDidMount() {
    // call user/new
    const { generateAuthToken, authenticating, authToken } = this.props;
    if (!authToken && !authenticating) {
      this.startAuthenticating();
    }

    AsyncStorage.getItem('firstRunEmail').then(email => {
      if (email) {
        this.setState({ email });
      }
    });
  }

  startAuthenticating = () => {
    const { generateAuthToken } = this.props;
    this.setState({ authenticationStarted: true, authenticationFailed: false });
    Lbry.status().then(info => {
        generateAuthToken(info.installation_id)
    }).catch(error => {
      if (this.state.statusTries >= EmailCollectPage.MAX_STATUS_TRIES) {
        this.setState({ authenticationFailed: true });
      } else {
        setTimeout(() => {
          this.startAuthenticating();
          this.setState({ statusTries: this.state.statusTries + 1 });
        }, 1000); // Retry every second for a maximum of MAX_STATUS_TRIES tries (15 seconds)
      }
    });
  }

  handleChangeText = (text) => {
    // save the value to the state email
    this.setState({ email: text });
    AsyncStorage.setItem(Constants.KEY_FIRST_RUN_EMAIL, text);
  }

  render() {
    const { authenticating, authToken, onEmailViewLayout, emailToVerify } = this.props;

    let content;
    if (this.state.authenticationFailed) {
      // Ask the user to try again
      content = (
        <View>
          <Text style={firstRunStyle.paragraph}>The LBRY servers were unreachable at this time. Please check your Internet connection and then restart the app to try again.</Text>
        </View>
      );
    } else if (!authToken || authenticating) {
      content = (
        <View>
          <ActivityIndicator size="large" color={Colors.White} style={firstRunStyle.waiting} />
          <Text style={firstRunStyle.paragraph}>Please wait while we get some things ready...</Text>
        </View>
      );
    } else {
      content = (
        <View onLayout={onEmailViewLayout}>
          <Text style={firstRunStyle.title}>Rewards.</Text>
          <Text style={firstRunStyle.paragraph}>You can earn LBRY Credits (LBC) rewards by completing various tasks in the app.</Text>
          <Text style={firstRunStyle.paragraph}>Please provide a valid email address below to be able to claim your rewards.</Text>
          <TextInput style={firstRunStyle.emailInput}
              placeholder="you@example.com"
              underlineColorAndroid="transparent"
              value={this.state.email}
              onChangeText={text => this.handleChangeText(text)}
              />
          <Text style={firstRunStyle.infoParagraph}>This information is disclosed only to LBRY, Inc. and not to the LBRY network. It is only required to earn LBRY rewards and may be used to sync usage data across devices.</Text>
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

export default EmailCollectPage;
