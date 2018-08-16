import React from 'react';
import { Lbry } from 'lbry-redux';
import {
  ActivityIndicator,
  AsyncStorage,
  Linking,
  NativeModules,
  Text,
  TouchableOpacity,
  View
} from 'react-native';
import { NavigationActions } from 'react-navigation';
import Colors from '../../styles/colors';
import Constants from '../../constants';
import WelcomePage from './internal/welcome-page';
import EmailCollectPage from './internal/email-collect-page';
import firstRunStyle from '../../styles/firstRun';

class FirstRunScreen extends React.PureComponent {
  static pages = [
    'welcome',
    'email-collect'
  ];

  constructor() {
    super();
    this.state = {
      currentPage: null,
      emailSubmitted: false,
      isFirstRun: false,
      launchUrl: null,
      showBottomContainer: true
    };
  }

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
        notify ({ message: String(emailNewErrorMessage), displayType: ['toast']});
      } else {
        // Request successful. Navigate to discover.
        this.closeFinalPage();
      }
    }
  }

  launchSplashScreen() {
    const { navigation } = this.props;
    const resetAction = NavigationActions.reset({
      index: 0,
      actions: [
        NavigationActions.navigate({ routeName: 'Splash', params: { launchUri: this.state.launchUri } })
      ]
    });
    navigation.dispatch(resetAction);
  }

  handleContinuePressed = () => {
    const pageIndex = FirstRunScreen.pages.indexOf(this.state.currentPage);
    if (this.state.currentPage !== 'email-collect' &&
        pageIndex === (FirstRunScreen.pages.length - 1)) {
      this.closeFinalPage();
    } else {
      // TODO: Actions and page verification for specific pages
      if (this.state.currentPage === 'email-collect') {
        // handle email collect
        this.handleEmailCollectPageContinue();
      } else {
        this.showNextPage();
      }
    }
  }

  handleEmailCollectPageContinue() {
    const { notify, addUserEmail } = this.props;
    // validate the email
    AsyncStorage.getItem(Constants.KEY_FIRST_RUN_EMAIL).then(email => {
      if (!email || email.trim().length === 0 || email.indexOf('@') === -1) {
        return notify({
          message: 'Please provide a valid email address to continue.',
          displayType: ['toast'],
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

  closeFinalPage() {
    // Final page. Let the app know that first run experience is completed.
    if (NativeModules.FirstRun) {
      NativeModules.FirstRun.firstRunCompleted();
    }

    // Navigate to the splash screen
    this.launchSplashScreen();
  }

  render() {
    const {
      authenticating,
      authToken,
      generateAuthToken,
      emailNewErrorMessage,
      emailNewPending,
      emailToVerify
    } = this.props;

    let page = null;
    if (this.state.currentPage === 'welcome') {
      // show welcome page
      page = (<WelcomePage />);
    } else if (this.state.currentPage === 'email-collect') {
      page = (<EmailCollectPage authenticating={authenticating}
                                authToken={authToken}
                                generateAuthToken={generateAuthToken}
                                onEmailViewLayout={() => this.setState({ showBottomContainer: true })} />);
    }

    return (
      <View style={firstRunStyle.screenContainer}>
        {page}
        {this.state.currentPage && this.state.showBottomContainer &&
        <View style={firstRunStyle.bottomContainer}>
          {emailNewPending &&
            <ActivityIndicator size="small" color={Colors.White} style={firstRunStyle.pageWaiting} />}

          {!emailNewPending &&
          <TouchableOpacity style={firstRunStyle.button} onPress={this.handleContinuePressed}>
            <Text style={firstRunStyle.buttonText}>Continue</Text>
          </TouchableOpacity>}
        </View>}
      </View>
    )
  }
}

export default FirstRunScreen;
