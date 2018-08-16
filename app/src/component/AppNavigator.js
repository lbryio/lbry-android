import React from 'react';
import AboutPage from '../page/about';
import DiscoverPage from '../page/discover';
import FilePage from '../page/file';
import FirstRunScreen from '../page/firstRun';
import SearchPage from '../page/search';
import TrendingPage from '../page/trending';
import SettingsPage from '../page/settings';
import SplashScreen from '../page/splash';
import TransactionHistoryPage from '../page/transactionHistory';
import WalletPage from '../page/wallet';
import SearchInput from '../component/searchInput';
import {
  addNavigationHelpers,
  DrawerNavigator,
  StackNavigator,
  NavigationActions
} from 'react-navigation';
import { connect } from 'react-redux';
import { addListener } from '../utils/redux';
import {
  AppState,
  AsyncStorage,
  BackHandler,
  Linking,
  NativeModules,
  TextInput,
  ToastAndroid
} from 'react-native';
import { SETTINGS, doHideNotification, doNotify, selectNotification } from 'lbry-redux';
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
import Icon from 'react-native-vector-icons/FontAwesome5';
import Constants from '../constants';
import discoverStyle from '../styles/discover';
import searchStyle from '../styles/search';
import SearchRightHeaderIcon from '../component/searchRightHeaderIcon';

const discoverStack = StackNavigator({
  Discover: {
    screen: DiscoverPage,
    navigationOptions: ({ navigation }) => ({
      title: 'Discover',
      headerLeft: <Icon name="bars" size={24} style={discoverStyle.drawerHamburger} onPress={() => navigation.navigate('DrawerOpen')} />,
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
      drawerLockMode: 'locked-closed'
    })
  }
}, {
  headerMode: 'screen',
});

const trendingStack = StackNavigator({
  Trending: {
    screen: TrendingPage,
    navigationOptions: ({ navigation }) => ({
      title: 'Trending',
      headerLeft: <Icon name="bars" size={24} style={discoverStyle.drawerHamburger} onPress={() => navigation.navigate('DrawerOpen')} />,
    })
  }
});

const walletStack = StackNavigator({
  Wallet: {
    screen: WalletPage,
    navigationOptions: ({ navigation }) => ({
      title: 'Wallet',
      headerLeft: <Icon name="bars" size={24} style={discoverStyle.drawerHamburger} onPress={() => navigation.navigate('DrawerOpen')} />,
    })
  },
  TransactionHistory: {
    screen: TransactionHistoryPage,
    navigationOptions: {
      title: 'Transaction History',
      drawerLockMode: 'locked-closed'
    }
  }
}, {
  headerMode: 'screen'
});

const drawer = DrawerNavigator({
  DiscoverStack: { screen: discoverStack },
  TrendingStack: { screen: trendingStack },
  WalletStack: { screen: walletStack },
  Settings: { screen: SettingsPage, navigationOptions: { drawerLockMode: 'locked-closed' } },
  About: { screen: AboutPage, navigationOptions: { drawerLockMode: 'locked-closed' } }
}, {
  drawerWidth: 300,
  headerMode: 'none'
});

export const AppNavigator = new StackNavigator({
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
        if (nav.routes[0].routes && nav.routes[0].routes.length > 0) {
          const subRoutes = nav.routes[0].routes[0].routes;
          const lastRoute = subRoutes[subRoutes.length - 1];
          if (nav.routes[0].routes[0].index > 0 &&
              ['About', 'Settings'].indexOf(lastRoute.key) > -1) {
            dispatch(NavigationActions.back());
            return true;
          }
        }
        if (nav.routes[0].routeName === 'Main') {
          if (nav.routes[0].routes[0].routes[0].index > 0) {
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
      notification,
      emailToVerify,
      emailVerifyPending,
      emailVerifyErrorMessage,
      user
    } = nextProps;

    if (notification) {
      const { displayType, message } = notification;
      let currentDisplayType;
      if (displayType.length) {
        for (let i = 0; i < displayType.length; i++) {
          const type = displayType[i];
          if (AppWithNavigationState.supportedDisplayTypes.indexOf(type) > -1) {
            currentDisplayType = type;
            break;
          }
        }
      } else if (AppWithNavigationState.supportedDisplayTypes.indexOf(displayType) > -1) {
        currentDisplayType = displayType;
      }

      if ('toast' === currentDisplayType) {
        ToastAndroid.show(message, ToastAndroid.SHORT);
      }

      dispatch(doHideNotification());
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
          dispatch(doNotify({ message, displayType: ['toast'] }));
        }
      });
    }
  }

  _handleAppStateChange = (nextAppState) => {
    // Check if the app was suspended
    if (AppState.currentState && AppState.currentState.match(/inactive|background/)) {
      AsyncStorage.getItem('firstLaunchTime').then(start => {
        if (start !== null && !isNaN(parseInt(start, 10))) {
          // App suspended during first launch?
          // If so, this needs to be included as a property when tracking
          AsyncStorage.setItem('firstLaunchSuspended', 'true');
        }
      });
    }
  }

  _handleUrl = (evt) => {
    const { dispatch } = this.props;
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
            dispatch(doNotify({ message, displayType: ['toast'] }));
          }
        } else {
          dispatch(doNotify({
            message: 'Invalid Verification URI',
            displayType: ['toast'],
          }));
        }
      } else {
        const navigateAction = NavigationActions.navigate({
          routeName: 'File',
          key: evt.url,
          params: { uri: evt.url }
        });
        dispatch(navigateAction);
      }
    }
  }

  render() {
    const { dispatch, nav } = this.props;
    return (
      <AppNavigator
        navigation={addNavigationHelpers({
          dispatch,
          state: nav,
          addListener,
        })}
      />
    );
  }
}

const mapStateToProps = state => ({
  keepDaemonRunning: makeSelectClientSetting(SETTINGS.KEEP_DAEMON_RUNNING)(state),
  nav: state.nav,
  notification: selectNotification(state),
  emailToVerify: selectEmailToVerify(state),
  emailVerifyPending: selectEmailVerifyIsPending(state),
  emailVerifyErrorMessage: selectEmailVerifyErrorMessage(state),
  showNsfw: makeSelectClientSetting(SETTINGS.SHOW_NSFW)(state),
  user: selectUser(state),
});

export default connect(mapStateToProps)(AppWithNavigationState);
