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
import PropTypes from 'prop-types';
import Colors from '../../styles/colors';
import splashStyle from '../../styles/splash';

class SplashScreen extends React.PureComponent {
  static navigationOptions = {
    title: 'Splash'
  };

  componentWillMount() {
    this.setState({
      details: 'Starting daemon',
      message: 'Connecting',
      isRunning: false,
      isLagging: false,
      launchUrl: null,
      didDownloadHeaders: false,
      isDownloadingHeaders: false,
      headersDownloadProgress: 0
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

  _updateStatusCallback(status) {
    const startupStatus = status.startup_status;
    if (startupStatus.code == 'started') {
      // Wait until we are able to resolve a name before declaring
      // that we are done.
      // TODO: This is a hack, and the logic should live in the daemon
      // to give us a better sense of when we are actually started
      this.setState({
        message: 'Testing Network',
        details: 'Waiting for name resolution',
        isLagging: false,
        isRunning: true,
      });

      Lbry.resolve({ uri: 'lbry://one' }).then(() => {
        // Leave the splash screen
        const { balanceSubscribe, navigation } = this.props;
        balanceSubscribe();

        const resetAction = NavigationActions.reset({
          index: 0,
          actions: [
            NavigationActions.navigate({ routeName: 'Main'})
          ]
        });
        navigation.dispatch(resetAction);

        const launchUrl = navigation.state.params.launchUrl || this.state.launchUrl;
        if (launchUrl) {
          navigation.navigate({ routeName: 'File', key: launchUrl, params: { uri: launchUrl } });
        }
      });
      return;
    }

    const blockchainStatus = status.blockchain_status;
    if (blockchainStatus) {
      this.setState({
        isDownloadingHeaders: blockchainStatus.is_downloading_headers,
        headersDownloadProgress: blockchainStatus.headers_download_progress
      });
    }

    if (blockchainStatus && (blockchainStatus.is_downloading_headers ||
      (this.state.didDownloadHeaders && 'loading_wallet' === startupStatus.code))) {
      if (!this.state.didDownloadHeaders) {
        this.setState({ didDownloadHeaders: true });
      }
      this.setState({
        message: 'Blockchain Sync',
        details: `Catching up with the blockchain (${blockchainStatus.headers_download_progress}%)`,
        isLagging: startupStatus.is_lagging
      });
    } else if (blockchainStatus && blockchainStatus.blocks_behind > 0) {
      const behind = blockchainStatus.blocks_behind;
      const behindText = behind + ' block' + (behind == 1 ? '' : 's') + ' behind';
      this.setState({
        message: 'Blockchain Sync',
        details: behindText,
        isLagging: startupStatus.is_lagging,
      });
    } else {
      this.setState({
        message: 'Network Loading',
        details: startupStatus.message + (startupStatus.is_lagging ? '' : '...'),
        isLagging: startupStatus.is_lagging,
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
