import React from 'react';
import { Lbry } from 'lbry-redux';
import { ActivityIndicator, Linking, NativeModules, Platform, Text, View } from 'react-native';
import { NavigationActions, StackActions } from 'react-navigation';
import { decode as atob } from 'base-64';
import { navigateToUri } from 'utils/helper';
import moment from 'moment';
import AsyncStorage from '@react-native-community/async-storage';
import Button from 'component/button';
import ProgressBar from 'component/progressBar';
import PropTypes from 'prop-types';
import Colors from 'styles/colors';
import Constants from 'constants';
import splashStyle from 'styles/splash';

const BLOCK_HEIGHT_INTERVAL = 1000 * 60 * 2.5; // every 2.5 minutes

const testingNetwork = 'Testing network';
const waitingForResolution = 'Waiting for name resolution';

class SplashScreen extends React.PureComponent {
  static navigationOptions = {
    title: 'Splash',
  };

  state = {
    accountUnlockFailed: false,
    daemonReady: false,
    details: 'Starting up',
    message: 'Connecting',
    isRunning: false,
    isLagging: false,
    launchUrl: null,
    isDownloadingHeaders: false,
    headersDownloadProgress: 0,
    shouldAuthenticate: false,
    subscriptionsFetched: false,
  };

  componentWillMount() {
    if (NativeModules.DaemonServiceControl) {
      NativeModules.DaemonServiceControl.startService();
    }
  }

  updateStatus() {
    Lbry.status().then(status => {
      this._updateStatusCallback(status);
    });
  }

  navigateToMain = () => {
    const { navigation } = this.props;
    const resetAction = StackActions.reset({
      index: 0,
      actions: [NavigationActions.navigate({ routeName: 'Main' })],
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
          AsyncStorage.setItem(Constants.KEY_SHOULD_VERIFY_EMAIL, 'true');
          try {
            verifyUserEmail(verification.token, verification.recaptcha);
          } catch (error) {
            const message = 'Invalid Verification Token';
            verifyUserEmailFailure(message);
            notify({ message });
          }
        } else {
          notify({
            message: 'Invalid Verification URI',
          });
        }
      } else {
        navigateToUri(navigation, launchUrl);
      }
    }
  };

  componentWillReceiveProps(nextProps) {
    const { emailToVerify, getSync, setEmailToVerify, verifyUserEmail, verifyUserEmailFailure } = this.props;
    const { user } = nextProps;

    if (this.state.daemonReady && this.state.shouldAuthenticate && user && user.id) {
      this.setState({ shouldAuthenticate: false }, () => {
        // user is authenticated, navigate to the main view
        if (user.has_verified_email) {
          NativeModules.UtilityModule.getSecureValue(Constants.KEY_FIRST_RUN_PASSWORD).then(walletPassword => {
            if (walletPassword && walletPassword.trim().length > 0) {
              getSync(walletPassword);
            }
            this.navigateToMain();
          });
          return;
        }

        this.navigateToMain();
      });
    }
  }

  finishSplashScreen = () => {
    const {
      authenticate,
      balanceSubscribe,
      blacklistedOutpointsSubscribe,
      checkSubscriptionsInit,
      getSync,
      navigation,
      notify,
      updateBlockHeight,
      user,
    } = this.props;

    Lbry.resolve({ urls: 'lbry://one' }).then(() => {
      // Leave the splash screen
      balanceSubscribe();
      blacklistedOutpointsSubscribe();
      checkSubscriptionsInit();
      updateBlockHeight();
      setInterval(() => {
        updateBlockHeight();
      }, BLOCK_HEIGHT_INTERVAL);

      if (user && user.id && user.has_verified_email) {
        // user already authenticated
        NativeModules.UtilityModule.getSecureValue(Constants.KEY_FIRST_RUN_PASSWORD).then(walletPassword => {
          if (walletPassword && walletPassword.trim().length > 0) {
            getSync(walletPassword);
          }
          this.navigateToMain();
        });
      } else {
        NativeModules.VersionInfo.getAppVersion().then(appVersion => {
          this.setState({ shouldAuthenticate: true });
          authenticate(appVersion, Platform.OS);
        });
      }
    });
  };

  handleAccountUnlockFailed() {
    this.setState({ accountUnlockFailed: true });
  }

  _updateStatusCallback(status) {
    const { fetchSubscriptions, getSync, setClientSetting } = this.props;
    const startupStatus = status.startup_status;
    // At the minimum, wallet should be started and blocks_behind equal to 0 before calling resolve
    const hasStarted = startupStatus.stream_manager && startupStatus.wallet && status.wallet.blocks_behind <= 0;
    if (hasStarted) {
      // Wait until we are able to resolve a name before declaring
      // that we are done.
      // TODO: This is a hack, and the logic should live in the daemon
      // to give us a better sense of when we are actually started
      this.setState({
        daemonReady: true,
        isLagging: false,
        isRunning: true,
      });

      // For now, automatically unlock the wallet if a password is set so that downloads work
      NativeModules.UtilityModule.getSecureValue(Constants.KEY_FIRST_RUN_PASSWORD).then(password => {
        if (password && password.trim().length > 0) {
          this.setState({
            message: 'Unlocking account',
            details: 'Decrypting wallet',
          });

          // unlock the wallet and then finish the splash screen
          Lbry.account_unlock({ password })
            .then(() => {
              this.setState({
                message: testingNetwork,
                details: waitingForResolution,
              });
              this.finishSplashScreen();
            })
            .catch(() => this.handleAccountUnlockFailed());
          return;
        } else {
          this.setState({
            message: testingNetwork,
            details: waitingForResolution,
          });
          this.finishSplashScreen();
        }
      });

      return;
    }

    const blockchainHeaders = status.blockchain_headers;
    const walletStatus = status.wallet;

    if (blockchainHeaders) {
      this.setState({
        isDownloadingHeaders: blockchainHeaders.downloading_headers,
        headersDownloadProgress: blockchainHeaders.download_progress,
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
        details: 'Initializing LBRY service',
      });
    }

    setTimeout(() => {
      this.updateStatus();
    }, 1000);
  }

  componentDidMount() {
    if (NativeModules.Firebase) {
      NativeModules.Firebase.track('app_launch', null);
    }

    this.props.fetchRewardedContent();
    Linking.getInitialURL().then(url => {
      if (url) {
        this.setState({ launchUrl: url });
      }
    });

    // Start measuring the first launch time from the splash screen
    // (time to first user interaction - after first run completed)
    AsyncStorage.getItem('hasLaunched').then(value => {
      if ('true' !== value) {
        AsyncStorage.setItem('hasLaunched', 'true');
        // only set firstLaunchTime since we've determined that this is the first app launch ever
        AsyncStorage.setItem('firstLaunchTime', String(moment().unix()));
      }
    });

    Lbry.connect()
      .then(() => {
        this.updateStatus();
      })
      .catch(e => {
        this.setState({
          isLagging: true,
          message: 'Connection Failure',
          details:
            'We could not establish a connection to the daemon. Your data connection may be preventing LBRY from connecting. Contact hello@lbry.com if you think this is a software bug.',
        });
      });
  }

  handleContinueAnywayPressed = () => {
    this.setState(
      {
        accountUnlockFailed: false,
        message: testingNetwork,
        details: waitingForResolution,
      },
      () => this.finishSplashScreen()
    );
  };

  render() {
    const { accountUnlockFailed, message, details, isLagging, isRunning } = this.state;

    if (accountUnlockFailed) {
      return (
        <View style={splashStyle.container}>
          <Text style={splashStyle.errorTitle}>Oops! Something went wrong.</Text>
          <Text style={splashStyle.paragraph}>
            Your wallet failed to unlock, which means you may not be able to play any videos or access your funds.
          </Text>
          <Text style={splashStyle.paragraph}>
            You can try to fix this by tapping Stop on the LBRY service notification and starting the app again. If the
            problem continues, you may have to reinstall the app and restore your account.
          </Text>

          <Button
            style={splashStyle.continueButton}
            theme={'light'}
            text={'Continue anyway'}
            onPress={this.handleContinueAnywayPressed}
          />
        </View>
      );
    }

    return (
      <View style={splashStyle.container}>
        <Text style={splashStyle.title}>LBRY</Text>
        {this.state.isDownloadingHeaders && (
          <ProgressBar
            color={Colors.White}
            style={splashStyle.progress}
            progress={this.state.headersDownloadProgress}
          />
        )}
        {!this.state.isDownloadingHeaders && (
          <ActivityIndicator color={Colors.White} style={splashStyle.loading} size={'small'} />
        )}
        <Text style={splashStyle.message}>{message}</Text>
        <Text style={splashStyle.details}>{details}</Text>
      </View>
    );
  }
}

export default SplashScreen;
