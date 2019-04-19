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

class EmailCollectPage extends React.PureComponent {
  static MAX_STATUS_TRIES = 30;

  state = {
    email: null,
    authenticationStarted: false,
    authenticationFailed: false,
    placeholder: 'you@example.com',
    statusTries: 0
  };

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
    const { authenticate } = this.props;
    this.setState({ authenticationStarted: true, authenticationFailed: false });
    NativeModules.VersionInfo.getAppVersion().then(appVersion => {
      Lbry.status().then(info => {
        authenticate(appVersion, Platform.OS);
      }).catch(error => {
        if (this.state.statusTries >= EmailCollectPage.MAX_STATUS_TRIES) {
          this.setState({ authenticationFailed: true });
        } else {
          setTimeout(() => {
            this.startAuthenticating();
            this.setState({ statusTries: this.state.statusTries + 1 });
          }, 1000); // Retry every second for a maximum of MAX_STATUS_TRIES tries (30 seconds)
        }
      });
    });
  }

  handleChangeText = (text) => {
    // save the value to the state email
    const { onEmailChanged } = this.props;
    this.setState({ email: text });
    if (onEmailChanged) {
      onEmailChanged(text);
    }
    AsyncStorage.setItem(Constants.KEY_FIRST_RUN_EMAIL, text);
    AsyncStorage.setItem(Constants.KEY_EMAIL_VERIFY_PENDING, 'true');
  }

  render() {
    const { authenticating, authToken, onEmailChanged, onEmailViewLayout, emailToVerify } = this.props;

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
          <Text style={firstRunStyle.title}>Setup account</Text>
          <TextInput style={firstRunStyle.emailInput}
              placeholder={this.state.placeholder}
              underlineColorAndroid="transparent"
              value={this.state.email}
              onChangeText={text => this.handleChangeText(text)}
              onFocus={() => {
                if (!this.state.email || this.state.email.length === 0) {
                  this.setState({ placeholder: '' });
                }
              }}
              onBlur={() => {
                if (!this.state.email || this.state.email.length === 0) {
                  this.setState({ placeholder: 'you@example.com' });
                }
              }}
              />
          <Text style={firstRunStyle.paragraph}>An account will allow you to earn rewards and keep your content and settings synced.</Text>
          <Text style={firstRunStyle.infoParagraph}>This information is disclosed only to LBRY, Inc. and not to the LBRY network.</Text>
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
