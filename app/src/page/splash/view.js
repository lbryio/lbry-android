import React from 'react';
import { Lbry } from 'lbry-redux';
import {
  ActivityIndicator,
  Linking,
  NativeModules,
  Platform,
  ProgressBarAndroid,
  Text,
  View
} from 'react-native';
import { NavigationActions } from 'react-navigation';
import { decode as atob } from 'base-64';
import PropTypes from 'prop-types';
import Colors from '../../styles/colors';
import splashStyle from '../../styles/splash';

class SplashScreen extends React.PureComponent {
  static navigationOptions = {
    title: 'Splash'
  };

  componentWillMount() {
    this.setState({
      daemonReady: false,
      details: 'Starting daemon',
      message: 'Connecting',
      isRunning: false,
      isLagging: false,
      launchUrl: null,
      isDownloadingHeaders: false,
      headersDownloadProgress: 0,
      shouldAuthenticate: false
    });

    if (NativeModules.DaemonServiceControl) {
      NativeModules.DaemonServiceControl.startService();
    }
  }

  componentDidMount() {
    // Start measuring the first launch time from the splash screen (time from daemon start to user interaction)
    AsyncStorage.getItem('hasLaunched').then(value => {
      if (value == null || value !== 'true') {
        AsyncStorage.setItem('hasLaunched', 'true');
        // only set firstLaunchTime since we've determined that this is the first app launch ever
        AsyncStorage.setItem('firstLaunchTime', String(moment().unix()));
      }
    });
  }

  updateStatus() {
    Lbry.status().then(status => {
      this._updateStatusCallback(status);
    });
  }

  componentWillUpdate(nextProps) {
    const { navigation, verifyUserEmail, verifyUserEmailFailure } = this.props;
    const { user } = nextProps;
    if (this.state.daemonReady && this.state.shouldAuthenticate && user && user.id) {
      // user is authenticated, navigate to the main view
      const resetAction = NavigationActions.reset({
        index: 0,
        actions: [
          NavigationActions.navigate({ routeName: 'Main'})
        ]
      });
      navigation.dispatch(resetAction);

      const launchUrl = navigation.state.params.launchUrl || this.state.launchUrl;
      if (launchUrl) {
        if (launchUrl.startsWith('lbry://?verify=')) {
          let verification = {};
          try {
            verification = JSON.parse(atob(launchUrl.substring(15)));
          } catch (error) {
            console.log(error);
          }
          if (verification.token && verification.recaptcha) {
            try {
              verifyUserEmail(verification.token, verification.recaptcha);
            } catch (error) {
              const message = 'Invalid Verification Token';
              verifyUserEmailFailure(message);
              notify({ message, displayType: ['toast'] });
            }
          } else {
            notify({
              message: 'Invalid Verification URI',
              displayType: ['toast'],
            });
          }
        } else {
          navigation.navigate({ routeName: 'File', key: launchUrl, params: { uri: launchUrl } });
        }
      }
    }
  }

  _updateStatusCallback(status) {
    const startupStatus = status.startup_status;
    // At the minimum, wallet should be started and blocks_behind equal to 0 before calling resolve
    const hasStarted = startupStatus.wallet && status.wallet.blocks_behind <= 0;
    if (hasStarted) {
      // Wait until we are able to resolve a name before declaring
      // that we are done.
      // TODO: This is a hack, and the logic should live in the daemon
      // to give us a better sense of when we are actually started
      this.setState({
        daemonReady: true,
        message: 'Testing Network',
        details: 'Waiting for name resolution',
        isLagging: false,
        isRunning: true,
      });

      Lbry.resolve({ uri: 'lbry://one' }).then(() => {
        // Leave the splash screen
        const {
          authenticate,
          balanceSubscribe,
          navigation,
          notify
        } = this.props;

        balanceSubscribe();
        NativeModules.VersionInfo.getAppVersion().then(appVersion => {
          if (NativeModules.UtilityModule) {
            // authenticate with the device ID if the method is available
            NativeModules.UtilityModule.getDeviceId().then(deviceId => {
              authenticate(`android-${appVersion}`, deviceId);
            });
          } else {
            authenticate(appVersion);
          }
          this.setState({ shouldAuthenticate: true });
        });
      });
      return;
    }

    const blockchainHeaders = status.blockchain_headers;
    const walletStatus = status.wallet;

    if (blockchainHeaders) {
      this.setState({
        isDownloadingHeaders: blockchainHeaders.downloading_headers,
        headersDownloadProgress: blockchainHeaders.download_progress
      });
    } else {
      // set downloading flag to false if blockchain_headers isn't in the status response
      this.setState({
        isDownloadingHeaders: false,
      });
    }

    if (blockchainHeaders && blockchainHeaders.downloading_headers) {
      const downloadProgress = blockchainHeaders.download_progress ? blockchainHeaders.download_progress : 0;
      this.setState({
        message: 'Blockchain Sync',
        details: `Catching up with the blockchain (${downloadProgress}%)`,
      });
    } else if (walletStatus && walletStatus.blocks_behind > 0) {
      const behind = walletStatus.blocks_behind;
      const behindText = behind + ' block' + (behind == 1 ? '' : 's') + ' behind';
      this.setState({
        message: 'Blockchain Sync',
        details: behindText,
      });
    } else {
      this.setState({
        message: 'Network Loading',
        details: 'Initializing LBRY service...'
      });
    }
    setTimeout(() => {
      this.updateStatus();
    }, 500);
  }

  componentDidMount() {
    if (NativeModules.Mixpanel) {
      NativeModules.Mixpanel.track('App Launch', null);
    }

    Linking.getInitialURL().then((url) => {
      if (url) {
        this.setState({ launchUrl: url });
      }
    });

    Lbry
      .connect()
      .then(() => {
        this.updateStatus();
      })
      .catch((e) => {
        this.setState({
          isLagging: true,
          message: 'Connection Failure',
          details:
            'We could not establish a connection to the daemon. Your data connection may be preventing LBRY from connecting. Contact hello@lbry.io if you think this is a software bug.'
        });
      });
  }

  render() {
    const { message, details, isLagging, isRunning } = this.state;

    return (
      <View style={splashStyle.container}>
        <Text style={splashStyle.title}>LBRY</Text>
        {'android' === Platform.OS && this.state.isDownloadingHeaders &&
        <ProgressBarAndroid color={Colors.White}
                            indeterminate={false}
                            styleAttr={"Horizontal"}
                            style={splashStyle.progress}
                            progress={this.state.headersDownloadProgress/100.0} />}
        {!this.state.isDownloadingHeaders && <ActivityIndicator color={Colors.White} style={splashStyle.loading} size={"small"} />}
        <Text style={splashStyle.message}>{message}</Text>
        <Text style={splashStyle.details}>{details}</Text>
      </View>
    );
  }
}

export default SplashScreen;
