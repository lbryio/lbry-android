import React from 'react';
import { Lbry } from 'lbry-redux';
import { ActivityIndicator, Linking, NativeModules, Text, TouchableOpacity, View } from 'react-native';
import { NavigationActions, StackActions } from 'react-navigation';
import AsyncStorage from '@react-native-community/async-storage';
import Colors from 'styles/colors';
import Constants from 'constants';
import WalletPage from './internal/wallet-page';
import WelcomePage from './internal/welcome-page';
import EmailCollectPage from './internal/email-collect-page';
import EmailVerifyPage from './internal/email-verify-page';
import SkipAccountPage from './internal/skip-account-page';
import firstRunStyle from 'styles/firstRun';

class FirstRunScreen extends React.PureComponent {
  static pages = [
    Constants.FIRST_RUN_PAGE_WELCOME,
    Constants.FIRST_RUN_PAGE_EMAIL_COLLECT,
    Constants.FIRST_RUN_PAGE_EMAIL_VERIFY,
    Constants.FIRST_RUN_PAGE_WALLET,
    Constants.FIRST_RUN_PAGE_SKIP_ACCOUNT,
  ];

  state = {
    currentPage: null,
    email: null,
    emailCollectTracked: false,
    emailSubmitted: false,
    enterPasswordTracked: false,
    isFirstRun: false,
    launchUrl: null,
    showSkip: false,
    isEmailVerified: false,
    skipAccountConfirmed: false,
    showBottomContainer: false,
    walletPassword: null,
    syncApplyStarted: false,
  };

  componentDidMount() {
    Linking.getInitialURL().then(url => {
      if (url) {
        this.setState({ launchUrl: url });
      }
    });

    if (NativeModules.FirstRun) {
      NativeModules.FirstRun.isFirstRun().then(firstRun => {
        AsyncStorage.removeItem(Constants.KEY_FIRST_RUN_EMAIL);
        AsyncStorage.removeItem(Constants.KEY_EMAIL_VERIFY_PENDING);
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
    const { emailNewErrorMessage, emailNewPending, syncApplyErrorMessage, syncApplyIsPending, user } = nextProps;
    const { notify, isApplyingSync, setClientSetting, setDefaultAccount } = this.props;

    if (this.state.emailSubmitted && !emailNewPending) {
      this.setState({ emailSubmitted: false });
      if (emailNewErrorMessage && emailNewErrorMessage.trim().length > 0) {
        notify({ message: String(emailNewErrorMessage), isError: true });
      } else {
        // Request successful. Navigate to email verify page.
        this.showPage(Constants.FIRST_RUN_PAGE_EMAIL_VERIFY);
      }
    }

    if (this.state.syncApplyStarted && !syncApplyIsPending) {
      this.setState({ syncApplyStarted: false });
      if (syncApplyErrorMessage && syncApplyErrorMessage.trim().length > 0) {
        notify({ message: syncApplyErrorMessage, isError: true });
        this.setState({ showBottomContainer: true });
      } else {
        // password successfully verified
        if (NativeModules.UtilityModule) {
          NativeModules.UtilityModule.setSecureValue(Constants.KEY_FIRST_RUN_PASSWORD, this.state.walletPassword);
        }
        setDefaultAccount();
        setClientSetting(Constants.SETTING_DEVICE_WALLET_SYNCED, true);
        this.closeFinalPage();
      }
    }

    this.checkVerificationStatus(user);
  }

  checkVerificationStatus = user => {
    const { navigation } = this.props;

    this.setState(
      {
        isEmailVerified: user && user.primary_email && user.has_verified_email,
      },
      () => {
        if (this.state.isEmailVerified) {
          this.showPage(Constants.FIRST_RUN_PAGE_WALLET);
        }
      }
    );
  };

  launchSplashScreen() {
    const { navigation } = this.props;
    const resetAction = StackActions.reset({
      index: 0,
      actions: [NavigationActions.navigate({ routeName: 'Splash', params: { launchUri: this.state.launchUri } })],
    });
    navigation.dispatch(resetAction);
  }

  handleLeftButtonPressed = () => {
    // Go to setup account page when "Setup account" is pressed
    if (Constants.FIRST_RUN_PAGE_SKIP_ACCOUNT === this.state.currentPage) {
      this.setState({ emailCollectTracked: false }); // reset tracked flag
      return this.showPage(Constants.FIRST_RUN_PAGE_EMAIL_COLLECT);
    }

    // Go to skip account page when "No, thanks" is pressed
    if (Constants.FIRST_RUN_PAGE_EMAIL_COLLECT === this.state.currentPage) {
      this.showPage(Constants.FIRST_RUN_PAGE_SKIP_ACCOUNT);
    }

    // Go to email collection page if user cancels from email verification
    if (Constants.FIRST_RUN_PAGE_EMAIL_VERIFY === this.state.currentPage) {
      this.setState({ emailCollectTracked: false }); // reset tracked flag
      this.showPage(Constants.FIRST_RUN_PAGE_EMAIL_COLLECT);
    }
  };

  checkWalletPassword = () => {
    const { syncApply, syncHash, syncData } = this.props;
    this.setState({ syncApplyStarted: true, showBottomContainer: false }, () => {
      syncApply(syncHash, syncData, this.state.walletPassword);
    });
  };

  handleContinuePressed = () => {
    const { notify, user, hasSyncedWallet } = this.props;
    const pageIndex = FirstRunScreen.pages.indexOf(this.state.currentPage);
    if (Constants.FIRST_RUN_PAGE_WALLET === this.state.currentPage) {
      // do apply sync to check if the password is valid
      if (hasSyncedWallet) {
        this.checkWalletPassword();
      } else {
        this.setFreshPassword();
      }
      return;
    }

    if (Constants.FIRST_RUN_PAGE_SKIP_ACCOUNT === this.state.currentPage && !this.state.skipAccountConfirmed) {
      notify({ message: 'Please confirm that you want to use LBRY without creating an account.' });
      return;
    }

    if (
      Constants.FIRST_RUN_PAGE_EMAIL_COLLECT !== this.state.currentPage &&
      pageIndex === FirstRunScreen.pages.length - 1
    ) {
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
  };

  handleEmailCollectPageContinue() {
    const { notify, addUserEmail } = this.props;
    const { email } = this.state;
    // validate the email
    if (!email || email.indexOf('@') === -1) {
      return notify({
        message: 'Please provide a valid email address to continue.',
      });
    }

    addUserEmail(email);
    this.setState({ emailSubmitted: true });
  }

  checkBottomContainer = pageName => {
    if (Constants.FIRST_RUN_PAGE_EMAIL_COLLECT === pageName || Constants.FIRST_RUN_PAGE_WALLET === pageName) {
      // do not show the buttons (because we're waiting to get things ready)
      this.setState({ showBottomContainer: false });
    }
  };

  showNextPage = () => {
    const pageIndex = FirstRunScreen.pages.indexOf(this.state.currentPage);
    const nextPage = FirstRunScreen.pages[pageIndex + 1];
    this.setState({ currentPage: nextPage });
    this.checkBottomContainer(nextPage);
  };

  showPage(pageName) {
    const pageIndex = FirstRunScreen.pages.indexOf(pageName);
    if (pageIndex > -1) {
      this.setState({ currentPage: pageName });
      this.checkBottomContainer(pageName);
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

  onEmailChanged = email => {
    this.setState({ email });
    if (Constants.FIRST_RUN_PAGE_EMAIL_COLLECT == this.state.currentPage) {
      this.setState({ showSkip: !email || email.trim().length === 0 });
    } else {
      this.setState({ showSkip: false });
    }
  };

  onEmailViewLayout = phase => {
    if ('collect' === phase) {
      if (!this.state.emailCollectTracked) {
        // we only want to track this once
        this.setState({ emailCollectTracked: true }, () =>
          NativeModules.Firebase.track('first_run_email_collect', null)
        );
      }
    } else if ('verify' === phase) {
      NativeModules.Firebase.track('first_run_email_verify', null);
    }

    this.setState({ showBottomContainer: true, showSkip: true });
  };

  onWalletPasswordChanged = password => {
    this.setState({ walletPassword: password });
  };

  onWalletViewLayout = () => {
    if (!this.state.enterPasswordTracked) {
      this.setState({ enterPasswordTracked: true }, () =>
        NativeModules.Firebase.track('first_run_enter_password', null)
      );
    }
    this.setState({ showBottomContainer: true });
  };

  onWelcomePageLayout = () => {
    this.setState({ showBottomContainer: true });
  };

  onSkipAccountViewLayout = () => {
    NativeModules.Firebase.track('first_run_skip_account', null);
    this.setState({ showBottomContainer: true });
  };

  onSkipSwitchChanged = checked => {
    this.setState({ skipAccountConfirmed: checked });
  };

  setFreshPassword = () => {
    const { getSync, setClientSetting } = this.props;
    if (NativeModules.UtilityModule) {
      const newPassword = this.state.walletPassword ? this.state.walletPassword : '';
      NativeModules.UtilityModule.setSecureValue(Constants.KEY_FIRST_RUN_PASSWORD, newPassword);
      Lbry.account_encrypt({ new_password: newPassword }).then(() => {
        // fresh account, new password set
        getSync(newPassword);
        setClientSetting(Constants.SETTING_DEVICE_WALLET_SYNCED, true);
        this.closeFinalPage();
      });
    }
  };

  render() {
    const {
      authenticate,
      authenticating,
      authToken,
      checkSync,
      emailNewErrorMessage,
      emailNewPending,
      emailToVerify,
      notify,
      hasSyncedWallet,
      getSyncIsPending,
      syncApplyIsPending,
      resendVerificationEmail,
      user,
    } = this.props;

    let page = null;
    switch (this.state.currentPage) {
      case Constants.FIRST_RUN_PAGE_WELCOME:
        page = (
          <WelcomePage
            authenticating={authenticating}
            authToken={authToken}
            authenticate={authenticate}
            onWelcomePageLayout={this.onWelcomePageLayout}
          />
        );
        break;

      case Constants.FIRST_RUN_PAGE_EMAIL_COLLECT:
        page = (
          <EmailCollectPage
            user={user}
            showNextPage={this.showNextPage}
            onEmailChanged={this.onEmailChanged}
            onEmailViewLayout={this.onEmailViewLayout}
          />
        );
        break;

      case Constants.FIRST_RUN_PAGE_EMAIL_VERIFY:
        page = (
          <EmailVerifyPage
            onEmailViewLayout={this.onEmailViewLayout}
            email={this.state.email}
            notify={notify}
            resendVerificationEmail={resendVerificationEmail}
          />
        );
        break;

      case Constants.FIRST_RUN_PAGE_WALLET:
        page = (
          <WalletPage
            checkSync={checkSync}
            hasSyncedWallet={hasSyncedWallet}
            getSyncIsPending={getSyncIsPending}
            syncApplyIsPending={syncApplyIsPending}
            onWalletViewLayout={this.onWalletViewLayout}
            onPasswordChanged={this.onWalletPasswordChanged}
          />
        );
        break;

      case Constants.FIRST_RUN_PAGE_SKIP_ACCOUNT:
        page = (
          <SkipAccountPage
            onSkipAccountViewLayout={this.onSkipAccountViewLayout}
            onSkipSwitchChanged={this.onSkipSwitchChanged}
          />
        );
        break;
    }

    return (
      <View style={firstRunStyle.screenContainer}>
        {page}
        {this.state.currentPage && this.state.showBottomContainer && (
          <View style={firstRunStyle.bottomContainer}>
            {emailNewPending && (
              <ActivityIndicator size="small" color={Colors.White} style={firstRunStyle.pageWaiting} />
            )}

            <View style={firstRunStyle.buttonRow}>
              {[Constants.FIRST_RUN_PAGE_WELCOME, Constants.FIRST_RUN_PAGE_WALLET].indexOf(this.state.currentPage) >
                -1 && <View />}
              {(Constants.FIRST_RUN_PAGE_SKIP_ACCOUNT === this.state.currentPage ||
                Constants.FIRST_RUN_PAGE_EMAIL_VERIFY === this.state.currentPage) && (
                <TouchableOpacity style={firstRunStyle.leftButton} onPress={this.handleLeftButtonPressed}>
                  <Text style={firstRunStyle.buttonText}>
                    «{' '}
                    {Constants.FIRST_RUN_PAGE_SKIP_ACCOUNT === this.state.currentPage
                      ? 'Setup account'
                      : 'Change email'}
                  </Text>
                </TouchableOpacity>
              )}
              {!emailNewPending && Constants.FIRST_RUN_PAGE_EMAIL_COLLECT === this.state.currentPage && (
                <TouchableOpacity style={firstRunStyle.leftButton} onPress={this.handleLeftButtonPressed}>
                  <Text style={firstRunStyle.smallLeftButtonText}>No, thanks »</Text>
                </TouchableOpacity>
              )}

              {!emailNewPending && (
                <TouchableOpacity style={firstRunStyle.button} onPress={this.handleContinuePressed}>
                  {Constants.FIRST_RUN_PAGE_SKIP_ACCOUNT === this.state.currentPage && (
                    <Text style={firstRunStyle.smallButtonText}>Use LBRY »</Text>
                  )}

                  {Constants.FIRST_RUN_PAGE_SKIP_ACCOUNT !== this.state.currentPage &&
                    Constants.FIRST_RUN_PAGE_EMAIL_VERIFY !== this.state.currentPage && (
                      <Text style={firstRunStyle.buttonText}>
                        {Constants.FIRST_RUN_PAGE_WALLET === this.state.currentPage ? 'Use LBRY' : 'Continue'} »
                      </Text>
                    )}
                </TouchableOpacity>
              )}
            </View>
          </View>
        )}
      </View>
    );
  }
}

export default FirstRunScreen;
