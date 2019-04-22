import React from 'react';
import { Lbry } from 'lbry-redux';
import {
  ActivityIndicator,
  Linking,
  NativeModules,
  Text,
  TouchableOpacity,
  View
} from 'react-native';
import { NavigationActions, StackActions } from 'react-navigation';
import AsyncStorage from '@react-native-community/async-storage';
import Colors from 'styles/colors';
import Constants from 'constants';
import WalletPage from './internal/wallet-page';
import WelcomePage from './internal/welcome-page';
import EmailCollectPage from './internal/email-collect-page';
import SkipAccountPage from './internal/skip-account-page';
import firstRunStyle from 'styles/firstRun';

class FirstRunScreen extends React.PureComponent {
  static pages = [
    Constants.FIRST_RUN_PAGE_WELCOME,
    Constants.FIRST_RUN_PAGE_EMAIL_COLLECT,
    Constants.FIRST_RUN_PAGE_WALLET,
    Constants.FIRST_RUN_PAGE_SKIP_ACCOUNT,
  ];

  state = {
    currentPage: null,
    emailSubmitted: false,
    isFirstRun: false,
    launchUrl: null,
    showSkip: false,
    skipAccountConfirmed: false,
    showBottomContainer: true,
    walletPassword: null
  };

  componentDidMount() {
    Linking.getInitialURL().then((url) => {
      if (url) {
        this.setState({ launchUrl: url });
      }
    });

    if (NativeModules.FirstRun) {
      NativeModules.FirstRun.isFirstRun().then(firstRun => {
        this.setState({ isFirstRun: firstRun });
        if (firstRun) {
          this.setState({ currentPage: FirstRunScreen.pages[0] });
        } else {
          // Not the first run. Navigate to the splash screen right away
          this.launchSplashScreen();
        }
      });
    } else {
      // The first run module was not detected. Go straight to the splash screen.
      this.launchSplashScreen();
    }
  }

  componentWillReceiveProps(nextProps) {
    const { emailNewErrorMessage, emailNewPending } = nextProps;
    const { notify } = this.props;

    if (this.state.emailSubmitted && !emailNewPending) {
      this.setState({ emailSubmitted: false });
      if (emailNewErrorMessage) {
        notify ({ message: String(emailNewErrorMessage), isError: true });
      } else {
        // Request successful. Navigate to next page (wallet).
        this.showNextPage();
      }
    }
  }

  launchSplashScreen() {
    const { navigation } = this.props;
    const resetAction = StackActions.reset({
      index: 0,
      actions: [
        NavigationActions.navigate({ routeName: 'Splash', params: { launchUri: this.state.launchUri } })
      ]
    });
    navigation.dispatch(resetAction);
  }

  handleLeftButtonPressed = () => {
    // Go to setup account page when "Setup account" is pressed
    if (Constants.FIRST_RUN_PAGE_SKIP_ACCOUNT === this.state.currentPage) {
      return this.showPage(Constants.FIRST_RUN_PAGE_EMAIL_COLLECT);
    }

    // Go to skip account page when "No, thanks" is pressed
    if (Constants.FIRST_RUN_PAGE_EMAIL_COLLECT === this.state.currentPage) {
      this.showPage(Constants.FIRST_RUN_PAGE_SKIP_ACCOUNT);
    }
  }

  handleContinuePressed = () => {
    const { notify } = this.props;
    const pageIndex = FirstRunScreen.pages.indexOf(this.state.currentPage);
    if (Constants.FIRST_RUN_PAGE_WALLET === this.state.currentPage) {
      if (!this.state.walletPassword || this.state.walletPassword.trim().length < 6) {
        return notify({ message: 'Your wallet password should be at least 6 characters long' });
      }

      this.closeFinalPage();
      return;
    }

    if (Constants.FIRST_RUN_PAGE_SKIP_ACCOUNT === this.state.currentPage && !this.state.skipAccountConfirmed) {
      notify({ message: 'Please confirm that you want to use LBRY without creating an account.' });
      return;
    }

    if (Constants.FIRST_RUN_PAGE_EMAIL_COLLECT !== this.state.currentPage && pageIndex === (FirstRunScreen.pages.length - 1)) {
      this.closeFinalPage();
    } else {
      // TODO: Actions and page verification for specific pages
      if (Constants.FIRST_RUN_PAGE_EMAIL_COLLECT === this.state.currentPage) {
        // handle email collect
        this.handleEmailCollectPageContinue();
      } else {
        this.showNextPage();
      }
    }
  }

  handleEmailCollectPageContinue() {
    const { notify, addUserEmail } = this.props;

    AsyncStorage.getItem(Constants.KEY_FIRST_RUN_EMAIL).then(email => {
      // validate the email
      if (!email || email.indexOf('@') === -1) {
        return notify({
          message: 'Please provide a valid email address to continue.',
        });
      }

      addUserEmail(email);
      this.setState({ emailSubmitted: true });
    });
  }

  showNextPage() {
    const pageIndex = FirstRunScreen.pages.indexOf(this.state.currentPage);
    const nextPage = FirstRunScreen.pages[pageIndex + 1];
    this.setState({ currentPage: nextPage });
    if (nextPage === 'email-collect') {
      // do not show the buttons (because we're waiting to get things ready)
      this.setState({ showBottomContainer: false });
    }
  }

  showPage(pageName) {
    const pageIndex = FirstRunScreen.pages.indexOf(pageName);
    if (pageIndex > -1) {
      this.setState({ currentPage: pageName });
    }
  }

  closeFinalPage() {
    // Final page. Let the app know that first run experience is completed.
    if (NativeModules.FirstRun) {
      NativeModules.FirstRun.firstRunCompleted();
    }

    // Navigate to the splash screen
    this.launchSplashScreen();
  }

  onEmailChanged = (email) => {
    if (Constants.FIRST_RUN_PAGE_EMAIL_COLLECT == this.state.currentPage) {
      this.setState({ showSkip: (!email || email.trim().length === 0) });
    } else {
      this.setState({ showSkip: false });
    }
  }

  onEmailViewLayout = () => {
    this.setState({ showBottomContainer: true });
    AsyncStorage.getItem('firstRunEmail').then(email => {
      this.setState({ showSkip: !email || email.trim().length === 0 });
    });
  }

  onWalletPasswordChanged = (password) => {
    this.setState({ walletPassword: password });
  }

  onWalletViewLayout = () => {
    this.setState({ showBottomContainer: true });
  }

  onSkipSwitchChanged = (checked) => {
    this.setState({ skipAccountConfirmed: checked });
  }

  render() {
    const {
      authenticate,
      authenticating,
      authToken,
      emailNewErrorMessage,
      emailNewPending,
      emailToVerify
    } = this.props;

    let page = null;
    switch (this.state.currentPage) {
      case 'welcome':
        page = (<WelcomePage />);
        break;

      case 'email-collect':
        page = (<EmailCollectPage
                  authenticating={authenticating}
                  authToken={authToken}
                  authenticate={authenticate}
                  onEmailChanged={this.onEmailChanged}
                  onEmailViewLayout={this.onEmailViewLayout} />);
        break;

      case 'wallet':
        page = (<WalletPage
                onWalletViewLayout={this.onWalletViewLayout}
                onPasswordChanged={this.onWalletPasswordChanged} />);
        break;

      case 'skip-account':
        page = (<SkipAccountPage
                onSkipAccountViewLayout={this.onSkipAccountViewLayout}
                onSkipSwitchChanged={this.onSkipSwitchChanged} />);
        break;
    }

    return (
      <View style={firstRunStyle.screenContainer}>
        {page}
        {this.state.currentPage && this.state.showBottomContainer &&
        <View style={firstRunStyle.bottomContainer}>
          {emailNewPending &&
            <ActivityIndicator size="small" color={Colors.White} style={firstRunStyle.pageWaiting} />}

          <View style={firstRunStyle.buttonRow}>
            {([Constants.FIRST_RUN_PAGE_WELCOME, Constants.FIRST_RUN_PAGE_WALLET].indexOf(this.state.currentPage) > -1) && <View />}
            {Constants.FIRST_RUN_PAGE_SKIP_ACCOUNT === this.state.currentPage &&
            <TouchableOpacity style={firstRunStyle.leftButton} onPress={this.handleLeftButtonPressed}>
              <Text style={firstRunStyle.buttonText}>« Setup account</Text>
            </TouchableOpacity>}
            {!emailNewPending && (Constants.FIRST_RUN_PAGE_EMAIL_COLLECT === this.state.currentPage) &&
            <TouchableOpacity style={firstRunStyle.leftButton} onPress={this.handleLeftButtonPressed}>
              <Text style={firstRunStyle.smallLeftButtonText}>No, thanks »</Text>
            </TouchableOpacity>}

            {!emailNewPending &&
            <TouchableOpacity style={firstRunStyle.button} onPress={this.handleContinuePressed}>
              {Constants.FIRST_RUN_PAGE_SKIP_ACCOUNT === this.state.currentPage &&
              <Text style={firstRunStyle.smallButtonText}>Use LBRY »</Text>}
              {Constants.FIRST_RUN_PAGE_SKIP_ACCOUNT !== this.state.currentPage &&
              <Text style={firstRunStyle.buttonText}>{Constants.FIRST_RUN_PAGE_WALLET === this.state.currentPage ? 'Use LBRY' : 'Continue'} »</Text>}
            </TouchableOpacity>}
          </View>
        </View>}
      </View>
    );
  }
}

export default FirstRunScreen;
