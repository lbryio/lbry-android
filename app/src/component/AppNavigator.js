import React from 'react';
import AboutPage from 'page/about';
import DiscoverPage from 'page/discover';
import DownloadsPage from 'page/downloads';
import FilePage from 'page/file';
import FirstRunScreen from 'page/firstRun';
import RewardsPage from 'page/rewards';
import TrendingPage from 'page/trending';
import SearchPage from 'page/search';
import SettingsPage from 'page/settings';
import SplashScreen from 'page/splash';
import SubscriptionsPage from 'page/subscriptions';
import TransactionHistoryPage from 'page/transactionHistory';
import VerificationScreen from 'page/verification';
import WalletPage from 'page/wallet';
import SearchInput from 'component/searchInput';
import {
  createAppContainer,
  createDrawerNavigator,
  createStackNavigator,
  NavigationActions
} from 'react-navigation';
import {
  addListener,
  createReduxContainer,
  createReactNavigationReduxMiddleware,
} from 'react-navigation-redux-helpers';
import { connect } from 'react-redux';
import {
  AppState,
  BackHandler,
  Linking,
  NativeModules,
  TextInput,
  ToastAndroid
} from 'react-native';
import { doDeleteCompleteBlobs } from 'redux/actions/file';
import { selectDrawerStack } from 'redux/selectors/drawer';
import { SETTINGS, doDismissToast, doToast, selectToast } from 'lbry-redux';
import {
  doGetSync,
  doUserCheckEmailVerified,
  doUserEmailVerify,
  doUserEmailVerifyFailure,
  selectEmailToVerify,
  selectEmailVerifyIsPending,
  selectEmailVerifyErrorMessage,
  selectUser
} from 'lbryinc';
import { makeSelectClientSetting } from 'redux/selectors/settings';
import { decode as atob } from 'base-64';
import { dispatchNavigateBack, dispatchNavigateToUri } from 'utils/helper';
import AsyncStorage from '@react-native-community/async-storage';
import Colors from 'styles/colors';
import Constants from 'constants';
import Icon from 'react-native-vector-icons/FontAwesome5';
import NavigationButton from 'component/navigationButton';
import discoverStyle from 'styles/discover';
import searchStyle from 'styles/search';
import SearchRightHeaderIcon from 'component/searchRightHeaderIcon';

const menuNavigationButton = (navigation) => <NavigationButton
                                               name="bars"
                                               size={24}
                                               style={discoverStyle.drawerMenuButton}
                                               iconStyle={discoverStyle.drawerHamburger}
                                               onPress={() => navigation.openDrawer() } />

const discoverStack = createStackNavigator({
  Discover: {
    screen: DiscoverPage,
    navigationOptions: ({ navigation }) => ({
      title: 'Explore',
      header: null
    }),
  },
  File: {
    screen: FilePage,
    navigationOptions: ({ navigation }) => ({
      header: null
    })
  },
  Search: {
    screen: SearchPage,
    navigationOptions: ({ navigation }) => ({
      header: null
    })
  }
}, {
  headerMode: 'screen',
  transitionConfig: () => ({ screenInterpolator: () => null }),
});

discoverStack.navigationOptions = ({ navigation }) => {
  let drawerLockMode = 'unlocked';
  /*if (navigation.state.index > 0) {
    drawerLockMode = 'locked-closed';
  }*/

  return {
    drawerLockMode
  };
};

const trendingStack = createStackNavigator({
  Trending: {
    screen: TrendingPage,
    navigationOptions: ({ navigation }) => ({
      title: 'Trending',
      header: null
    })
  }
});

const myLbryStack = createStackNavigator({
  Downloads: {
    screen: DownloadsPage,
    navigationOptions: ({ navigation }) => ({
      title: 'Downloads',
      header: null
    })
  }
});

const mySubscriptionsStack = createStackNavigator({
  Subscriptions: {
    screen: SubscriptionsPage,
    navigationOptions: ({ navigation }) => ({
      title: 'Subscriptions',
      header: null
    })
  }
});

const rewardsStack = createStackNavigator({
  Rewards: {
    screen: RewardsPage,
    navigationOptions: ({ navigation }) => ({
      title: 'Rewards',
      header: null
    })
  }
});

const walletStack = createStackNavigator({
  Wallet: {
    screen: WalletPage,
    navigationOptions: ({ navigation }) => ({
      title: 'Wallet',
      header: null
    })
  },
  TransactionHistory: {
    screen: TransactionHistoryPage,
    navigationOptions: {
      title: 'Transaction History',
      header: null
    }
  }
}, {
  headerMode: 'screen',
  transitionConfig: () => ({ screenInterpolator: () => null }),
});

const drawer = createDrawerNavigator({
  DiscoverStack: { screen: discoverStack, navigationOptions: {
    title: 'Explore', drawerIcon: ({ tintColor }) => <Icon name="home" size={20} style={{ color: tintColor }} />
  }},
  TrendingStack: { screen: trendingStack, navigationOptions: {
    title: 'Trending', drawerIcon: ({ tintColor }) => <Icon name="fire" size={20} style={{ color: tintColor }} />
  }},
  MySubscriptionsStack: { screen: mySubscriptionsStack, navigationOptions: {
    title: 'Subscriptions', drawerIcon: ({ tintColor }) => <Icon name="heart" solid={true} size={20} style={{ color: tintColor }} />
  }},
  WalletStack: { screen: walletStack, navigationOptions: {
    title: 'Wallet', drawerIcon: ({ tintColor }) => <Icon name="wallet" size={20} style={{ color: tintColor }} />
  }},
  Rewards: { screen: rewardsStack, navigationOptions: {
    drawerIcon: ({ tintColor }) => <Icon name="award" size={20} style={{ color: tintColor }} />
  }},
  MyLBRYStack: { screen: myLbryStack, navigationOptions: {
    title: 'Downloads', drawerIcon: ({ tintColor }) => <Icon name="folder" size={20} style={{ color: tintColor }} />
  }},
  Settings: { screen: SettingsPage, navigationOptions: {
    drawerLockMode: 'locked-closed',
    drawerIcon: ({ tintColor }) => <Icon name="cog" size={20} style={{ color: tintColor }} />
  }},
  About: { screen: AboutPage, navigationOptions: {
    drawerLockMode: 'locked-closed',
    drawerIcon: ({ tintColor }) => <Icon name="info" size={20} style={{ color: tintColor }} />
  }}
}, {
  drawerWidth: 300,
  headerMode: 'none',
  contentOptions: {
    activeTintColor: Colors.LbryGreen,
    labelStyle: discoverStyle.menuText
  }
});

const mainStackNavigator = new createStackNavigator({
  FirstRun: {
    screen: FirstRunScreen,
    navigationOptions: {
      drawerLockMode: 'locked-closed'
    }
  },
  Splash: {
    screen: SplashScreen,
    navigationOptions: {
      drawerLockMode: 'locked-closed'
    }
  },
  Main: {
    screen: drawer
  },
  Verification: {
    screen: VerificationScreen,
    navigationOptions: {
      drawerLockMode: 'locked-closed'
    }
  }
}, {
  headerMode: 'none'
});



export const AppNavigator = mainStackNavigator;
export const reactNavigationMiddleware = createReactNavigationReduxMiddleware(
  state => state.nav,
);
const App = createReduxContainer(mainStackNavigator, "root");
const appMapStateToProps = (state) => ({
  state: state.nav,
});
const ReduxAppNavigator = connect(appMapStateToProps)(App);

class AppWithNavigationState extends React.Component {
  static supportedDisplayTypes = ['toast'];

  constructor() {
    super();
    this.emailVerifyCheckInterval = null;
    this.state = {
      emailVerifyDone: false,
      verifyPending: false
    };
  }

  componentWillMount() {
    AppState.addEventListener('change', this._handleAppStateChange);
    BackHandler.addEventListener('hardwareBackPress', function() {
      const { dispatch, nav, drawerStack } = this.props;
      // There should be a better way to check this
      if (nav.routes.length > 0) {
        if (nav.routes[0].routeName === 'Main') {
          const mainRoute = nav.routes[0];
          if (mainRoute.index > 0 ||
              mainRoute.routes[0].index > 0 /* Discover stack index */ ||
              mainRoute.routes[4].index > 0 /* Wallet stack index */ ||
              mainRoute.index >= 5 /* Settings and About screens */) {
            dispatchNavigateBack(dispatch, nav, drawerStack);
            return true;
          }
        }
      }
      return false;
    }.bind(this));
  }

  componentDidMount() {
    this.emailVerifyCheckInterval = setInterval(() => this.checkEmailVerification(), 5000);
    Linking.addEventListener('url', this._handleUrl);
  }

  checkEmailVerification = () => {
    const { dispatch } = this.props;
    AsyncStorage.getItem(Constants.KEY_EMAIL_VERIFY_PENDING).then(pending => {
      this.setState({ verifyPending: ('true' === pending) });
      if ('true' === pending) {
        dispatch(doUserCheckEmailVerified());
      }
    });
  }

  componentWillUnmount() {
    AppState.removeEventListener('change', this._handleAppStateChange);
    BackHandler.removeEventListener('hardwareBackPress');
    Linking.removeEventListener('url', this._handleUrl);
  }

  componentDidUpdate() {
    const { dispatch, user } = this.props;
    if (this.state.verifyPending && this.emailVerifyCheckInterval > 0 && user && user.has_verified_email) {
      clearInterval(this.emailVerifyCheckInterval);
      AsyncStorage.setItem(Constants.KEY_EMAIL_VERIFY_PENDING, 'false');
      this.setState({ verifyPending: false });

      ToastAndroid.show('Your email address was successfully verified.', ToastAndroid.LONG);

      // upon successful email verification, check wallet sync
      NativeModules.UtilityModule.getSecureValue(Constants.KEY_FIRST_RUN_PASSWORD).then(walletPassword => {
        dispatch(doGetSync(walletPassword));
      });
    }
  }

  componentWillUpdate(nextProps) {
    const { dispatch } = this.props;
    const {
      toast,
      emailToVerify,
      emailVerifyPending,
      emailVerifyErrorMessage,
      user
    } = nextProps;

    if (toast) {
      const { message } = toast;
      let currentDisplayType;
      if (!currentDisplayType && message) {
        // default to toast if no display type set and there is a message specified
        currentDisplayType = 'toast';
      }

      if ('toast' === currentDisplayType) {
        ToastAndroid.show(message, ToastAndroid.LONG);
      }

      dispatch(doDismissToast());
    }

    if (user &&
        !emailVerifyPending &&
        !this.state.emailVerifyDone &&
        (emailToVerify || emailVerifyErrorMessage)) {
      AsyncStorage.getItem(Constants.KEY_SHOULD_VERIFY_EMAIL).then(shouldVerify => {
        if ('true' === shouldVerify) {
          this.setState({ emailVerifyDone: true });
          const message = emailVerifyErrorMessage ?
            String(emailVerifyErrorMessage) : 'Your email address was successfully verified.';
          if (!emailVerifyErrorMessage) {
            AsyncStorage.removeItem(Constants.KEY_FIRST_RUN_EMAIL);
          }
          AsyncStorage.removeItem(Constants.KEY_SHOULD_VERIFY_EMAIL);
          dispatch(doToast({ message }));
        }
      });
    }
  }

  _handleAppStateChange = (nextAppState) => {
    const { backgroundPlayEnabled, dispatch } = this.props;
    // Check if the app was suspended
    if (AppState.currentState && AppState.currentState.match(/inactive|background/)) {
      AsyncStorage.getItem('firstLaunchTime').then(start => {
        if (start !== null && !isNaN(parseInt(start, 10))) {
          // App suspended during first launch?
          // If so, this needs to be included as a property when tracking
          AsyncStorage.setItem('firstLaunchSuspended', 'true');
        }

        // Background media
        if (backgroundPlayEnabled && NativeModules.BackgroundMedia && window.currentMediaInfo) {
          const { title, channel, uri } = window.currentMediaInfo;
          NativeModules.BackgroundMedia.showPlaybackNotification(title, channel, uri, false);
        }
      });
    }

    if (AppState.currentState && AppState.currentState.match(/active/)) {
      // Cleanup blobs for completed files upon app resume to save space
      dispatch(doDeleteCompleteBlobs());
      if (backgroundPlayEnabled || NativeModules.BackgroundMedia) {
        NativeModules.BackgroundMedia.hidePlaybackNotification();
      }
    }
  }

  _handleUrl = (evt) => {
    const { dispatch, nav } = this.props;
    if (evt.url) {
      if (evt.url.startsWith('lbry://?verify=')) {
        this.setState({ emailVerifyDone: false });
        let verification = {};
        try {
          verification = JSON.parse(atob(evt.url.substring(15)));
        } catch (error) {
          console.log(error);
        }

        if (verification.token && verification.recaptcha) {
          AsyncStorage.setItem(Constants.KEY_SHOULD_VERIFY_EMAIL, 'true');
          try {
            dispatch(doUserEmailVerify(verification.token, verification.recaptcha));
          } catch (error) {
            const message = 'Invalid Verification Token';
            dispatch(doUserEmailVerifyFailure(message));
            dispatch(doToast({ message }));
          }
        } else {
          dispatch(doToast({
            message: 'Invalid Verification URI',
          }));
        }
      } else {
        dispatchNavigateToUri(dispatch, nav, evt.url);
      }
    }
  }

  render() {
    return <ReduxAppNavigator />;
  }
}

const mapStateToProps = state => ({
  backgroundPlayEnabled: makeSelectClientSetting(SETTINGS.BACKGROUND_PLAY_ENABLED)(state),
  keepDaemonRunning: makeSelectClientSetting(SETTINGS.KEEP_DAEMON_RUNNING)(state),
  nav: state.nav,
  toast: selectToast(state),
  drawerStack: selectDrawerStack(state),
  emailToVerify: selectEmailToVerify(state),
  emailVerifyPending: selectEmailVerifyIsPending(state),
  emailVerifyErrorMessage: selectEmailVerifyErrorMessage(state),
  showNsfw: makeSelectClientSetting(SETTINGS.SHOW_NSFW)(state),
  user: selectUser(state)
});

export default connect(mapStateToProps)(AppWithNavigationState);
