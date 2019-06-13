import React from 'react';
import { Lbry } from 'lbry-redux';
import { ActivityIndicator, NativeModules, Platform, Text, View } from 'react-native';
import AsyncStorage from '@react-native-community/async-storage';
import Colors from 'styles/colors';
import Constants from 'constants';
import firstRunStyle from 'styles/firstRun';

class WelcomePage extends React.PureComponent {
  static MAX_STATUS_TRIES = 60;

  state = {
    authenticationStarted: false,
    authenticationFailed: false,
    sdkStarted: false,
    statusTries: 0,
  };

  componentWillReceiveProps(nextProps) {
    const { authenticating, authToken } = this.props;

    if (this.state.authenticationStarted && !authenticating) {
      if (authToken === null) {
        this.setState({ authenticationFailed: true, authenticationStarted: false });
      } else {
        // first_user_auth because it's the first time
        AsyncStorage.getItem(Constants.KEY_FIRST_USER_AUTH).then(firstUserAuth => {
          if ('true' !== firstUserAuth) {
            // first_user_auth
            NativeModules.Firebase.track('first_user_auth', null);
            AsyncStorage.setItem(Constants.KEY_FIRST_USER_AUTH, 'true');
          }
        });
      }
    }
  }

  componentDidMount() {
    // call user/new
    const { generateAuthToken, authenticating, authToken } = this.props;
    if (!authenticating) {
      this.startAuthenticating();
    }
  }

  startAuthenticating = () => {
    const { authenticate } = this.props;
    this.setState({ authenticationStarted: true, authenticationFailed: false });
    NativeModules.VersionInfo.getAppVersion().then(appVersion => {
      Lbry.status()
        .then(info => {
          this.setState({ sdkStarted: true });

          authenticate(appVersion, Platform.OS);
        })
        .catch(error => {
          if (this.state.statusTries >= WelcomePage.MAX_STATUS_TRIES) {
            this.setState({ authenticationFailed: true });

            // sdk_start_failed
            NativeModules.Firebase.track('sdk_start_failed', null);
          } else {
            setTimeout(() => {
              this.startAuthenticating();
              this.setState({ statusTries: this.state.statusTries + 1 });
            }, 1000); // Retry every second for a maximum of MAX_STATUS_TRIES tries (60 seconds)
          }
        });
    });
  };

  render() {
    const { authenticating, authToken, onWelcomePageLayout } = this.props;

    let content;
    if (this.state.authenticationFailed) {
      // Ask the user to try again
      content = (
        <View>
          <Text style={firstRunStyle.paragraph}>
            The LBRY servers were unreachable at this time. Please check your Internet connection and then restart the
            app to try again.
          </Text>
        </View>
      );
    } else if (!authToken || authenticating) {
      content = (
        <View style={firstRunStyle.centered}>
          <ActivityIndicator size="large" color={Colors.White} style={firstRunStyle.waiting} />
          <Text style={firstRunStyle.paragraph}>Please wait while we get some things ready...</Text>
        </View>
      );
    } else {
      content = (
        <View onLayout={onWelcomePageLayout}>
          <Text style={firstRunStyle.title}>Welcome to LBRY.</Text>
          <Text style={firstRunStyle.paragraph}>
            LBRY is a community-controlled content platform where you can find and publish videos, music, books, and
            more.
          </Text>
        </View>
      );
    }

    return <View style={firstRunStyle.container}>{content}</View>;
  }
}

export default WelcomePage;
