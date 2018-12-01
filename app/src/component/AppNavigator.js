import React from 'react';
import AboutPage from '../page/about';
import DiscoverPage from '../page/discover';
import DownloadsPage from '../page/downloads';
import FilePage from '../page/file';
import FirstRunScreen from '../page/firstRun';
import RewardsPage from '../page/rewards';
import TrendingPage from '../page/trending';
import SearchPage from '../page/search';
import SettingsPage from '../page/settings';
import SplashScreen from '../page/splash';
import TransactionHistoryPage from '../page/transactionHistory';
import WalletPage from '../page/wallet';
import SearchInput from '../component/searchInput';
import {
  createDrawerNavigator,
  createStackNavigator,
  NavigationActions
} from 'react-navigation';
import {
  addListener,
  reduxifyNavigator,
  createReactNavigationReduxMiddleware,
} from 'react-navigation-redux-helpers';
import { connect } from 'react-redux';
import {
  AppState,
  AsyncStorage,
  BackHandler,
  Linking,
  NativeModules,
  TextInput,
  ToastAndroid
} from 'react-native';
import { doDeleteCompleteBlobs } from '../redux/actions/file';
import { SETTINGS, doDismissToast, doToast, selectToast } from 'lbry-redux';
import {
  doUserEmailVerify,
  doUserEmailVerifyFailure,
  selectEmailToVerify,
  selectEmailVerifyIsPending,
  selectEmailVerifyErrorMessage,
  selectUser
} from 'lbryinc';
import { makeSelectClientSetting } from '../redux/selectors/settings';
import { decode as atob } from 'base-64';
import { dispatchNavigateToUri } from '../utils/helper';
import Colors from '../styles/colors';
import Constants from '../constants';
import Icon from 'react-native-vector-icons/FontAwesome5';
import NavigationButton from '../component/navigationButton';
import discoverStyle from '../styles/discover';
import searchStyle from '../styles/search';
import SearchRightHeaderIcon from '../component/searchRightHeaderIcon';

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
      title: 'Discover',
      headerLeft: menuNavigationButton(navigation),
      headerTitleStyle: discoverStyle.titleText
    })
  },
  File: {
    screen: FilePage,
    navigationOptions: {
      header: null,
      drawerLockMode: 'locked-closed'
    }
  },
  Search: {
    screen: SearchPage,
    navigationOptions: ({ navigation }) => ({
      drawerLockMode: 'locked-closed',
      headerTitleStyle: discoverStyle.titleText
    })
  }
}, {
  headerMode: 'screen'
});

const trendingStack = createStackNavigator({
  Trending: {
    screen: TrendingPage,
    navigationOptions: ({ navigation }) => ({
      title: 'Trending',
      headerLeft: menuNavigationButton(navigation),
      headerTitleStyle: discoverStyle.titleText
    })
  }
});

const myLbryStack = createStackNavigator({
  Downloads: {
    screen: DownloadsPage,
    navigationOptions: ({ navigation }) => ({
      title: 'My LBRY',
      headerLeft: menuNavigationButton(navigation),
      headerTitleStyle: discoverStyle.titleText
    })
  }
});

const rewardsStack = createStackNavigator({
  Rewards: {
    screen: RewardsPage,
    navigationOptions: ({ navigation }) => ({
      title: 'Rewards',
      headerLeft: menuNavigationButton(navigation),
      headerTitleStyle: discoverStyle.titleText
    })
  }
});

const walletStack = createStackNavigator({
  Wallet: {
    screen: WalletPage,
    navigationOptions: ({ navigation }) => ({
      title: 'Wallet',
      headerLeft: menuNavigationButton(navigation),
      headerTitleStyle: discoverStyle.titleText
    })
  },
  TransactionHistory: {
    screen: TransactionHistoryPage,
    navigationOptions: {
      title: 'Transaction History',
      drawerLockMode: 'locked-closed',
      headerTitleStyle: discoverStyle.titleText
    }
  }
}, {
  headerMode: 'screen'
});

const drawer = createDrawerNavigator({
  DiscoverStack: { screen: discoverStack, navigationOptions: {
    title: 'Discover', drawerIcon: ({ tintColor }) => <Icon name="compass" size={20} style={{ color: tintColor }} />
  }},
  TrendingStack: { screen: trendingStack, navigationOptions: {
    title: 'Trending', drawerIcon: ({ tintColor }) => <Icon name="fire" size={20} style={{ color: tintColor }} />
  }},
  MyLBRYStack: { screen: myLbryStack, navigationOptions: {
    title: 'My LBRY', drawerIcon: ({ tintColor }) => <Icon name="folder" size={20} style={{ color: tintColor }} />
  }},
  Rewards: { screen: rewardsStack, navigationOptions: {
    drawerIcon: ({ tintColor }) => <Icon name="trophy" size={20} style={{ color: tintColor }} />
  }},
  WalletStack: { screen: walletStack, navigationOptions: {
    title: 'Wallet', drawerIcon: ({ tintColor }) => <Icon name="wallet" size={20} style={{ color: tintColor }} />
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

export const AppNavigator = new createStackNavigator({
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
  }
}, {
  headerMode: 'none'
});

export const reactNavigationMiddleware = createReactNavigationReduxMiddleware(
  "root",
  state => state.nav,
);
const App = reduxifyNavigator(AppNavigator, "root");
const appMapStateToProps = (state) => ({
  state: state.nav,
});
const ReduxAppNavigator = connect(appMapStateToProps)(App);

class AppWithNavigationState extends React.Component {
  static supportedDisplayTypes = ['toast'];

  constructor() {
    super();
    this.state = {
      emailVerifyDone: false
    };
  }

  componentWillMount() {
    AppState.addEventListener('change', this._handleAppStateChange);
    BackHandler.addEventListener('hardwareBackPress', function() {
      const { dispatch, nav } = this.props;
      // There should be a better way to check this
      if (nav.routes.length > 0) {
        if (nav.routes[0].routeName === 'Main') {
          const mainRoute = nav.routes[0];
          if (mainRoute.index > 0 ||
              mainRoute.routes[0].index > 0 /* Discover stack index */ ||
              mainRoute.routes[4].index > 0 /* Wallet stack index */ ||
              mainRoute.index >= 5 /* Settings and About screens */) {
            dispatch(NavigationActions.back());
            return true;
          }
        }
      }
      return false;
    }.bind(this));
  }

  componentDidMount() {
    Linking.addEventListener('url', this._handleUrl);
  }

  componentWillUnmount() {
    AppState.removeEventListener('change', this._handleAppStateChange);
    BackHandler.removeEventListener('hardwareBackPress');
    Linking.removeEventListener('url', this._handleUrl);
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
  emailToVerify: selectEmailToVerify(state),
  emailVerifyPending: selectEmailVerifyIsPending(state),
  emailVerifyErrorMessage: selectEmailVerifyErrorMessage(state),
  showNsfw: makeSelectClientSetting(SETTINGS.SHOW_NSFW)(state),
  user: selectUser(state)
});

export default connect(mapStateToProps)(AppWithNavigationState);
