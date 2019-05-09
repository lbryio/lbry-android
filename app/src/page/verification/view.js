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
import EmailVerifyPage from './internal/email-verify-page';
import firstRunStyle from 'styles/firstRun';

class VerificationScreen extends React.PureComponent {
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
    const { navigation } = this.props;

    this.setState({ currentPage: 'emailVerify' });
  }

  componentWillReceiveProps(nextProps) {
    const { emailNewErrorMessage, emailNewPending } = nextProps;
    const { notify } = this.props;

    /*if (this.state.emailSubmitted && !emailNewPending) {
      this.setState({ emailSubmitted: false });
      if (emailNewErrorMessage) {
        notify ({ message: String(emailNewErrorMessage), isError: true });
      } else {
        // Request successful. Navigate to next page (wallet).
        this.showNextPage();
      }
    }*/
  }

  handleLeftButtonPressed = () => {
    /*// Go to setup account page when "Setup account" is pressed
    if (Constants.FIRST_RUN_PAGE_SKIP_ACCOUNT === this.state.currentPage) {
      return this.showPage(Constants.FIRST_RUN_PAGE_EMAIL_COLLECT);
    }

    // Go to skip account page when "No, thanks" is pressed
    if (Constants.FIRST_RUN_PAGE_EMAIL_COLLECT === this.state.currentPage) {
      this.showPage(Constants.FIRST_RUN_PAGE_SKIP_ACCOUNT);
    }*/
  }

  handleContinuePressed = () => {
    /*const { notify } = this.props;
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
    }*/
  }

  handleEmailCollectPageContinue() {
    /*const { notify, addUserEmail } = this.props;

    AsyncStorage.getItem(Constants.KEY_FIRST_RUN_EMAIL).then(email => {
      // validate the email
      if (!email || email.indexOf('@') === -1) {
        return notify({
          message: 'Please provide a valid email address to continue.',
        });
      }

      addUserEmail(email);
      this.setState({ emailSubmitted: true });
    });*/
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

  onCloseButtonPressed = () => {
    const { navigation } = this.props;
    navigation.goBack();
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
      addUserEmail,
      emailNewErrorMessage,
      emailNewPending,
      emailToVerify,
      notify,
      resendVerificationEmail
    } = this.props;

    let page = null;
    switch (this.state.currentPage) {
      case 'emailVerify':
        page = (
          <EmailVerifyPage
            addUserEmail={addUserEmail}
            emailNewErrorMessage={emailNewErrorMessage}
            emailNewPending={emailNewPending}
            emailToVerify={emailToVerify}
            notify={notify}
            resendVerificationEmail={resendVerificationEmail}
          />
        );
        break;
    }

    return (
      <View style={firstRunStyle.screenContainer}>
        {page}

        <TouchableOpacity style={firstRunStyle.closeButton} onPress={this.onCloseButtonPressed}>
          <Text style={firstRunStyle.closeButtonText}>x</Text>
        </TouchableOpacity>
      </View>
    );
  }
}

export default VerificationScreen;
