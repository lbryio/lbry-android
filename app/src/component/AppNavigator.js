import React from 'react';
import AboutPage from '../page/about';
import DiscoverPage from '../page/discover';
import FilePage from '../page/file';
import SearchPage from '../page/search';
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
import { SETTINGS, doHideNotification, selectNotification } from 'lbry-redux';
import { makeSelectClientSetting } from '../redux/selectors/settings';
import Feather from 'react-native-vector-icons/Feather';
import discoverStyle from '../styles/discover';
import searchStyle from '../styles/search';
import SearchRightHeaderIcon from "../component/searchRightHeaderIcon";

const discoverStack = StackNavigator({
  Discover: {
    screen: DiscoverPage,
    navigationOptions: ({ navigation }) => ({
      title: 'Discover',
      headerLeft: <Feather name="menu" size={24} style={discoverStyle.drawerHamburger} onPress={() => navigation.navigate('DrawerOpen')} />,
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

const walletStack = StackNavigator({
  Wallet: {
    screen: WalletPage,
    navigationOptions: ({ navigation }) => ({
      title: 'Wallet',
      headerLeft: <Feather name="menu" size={24} style={discoverStyle.drawerHamburger} onPress={() => navigation.navigate('DrawerOpen')} />,
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
  WalletStack: { screen: walletStack },
  Settings: { screen: SettingsPage, navigationOptions: { drawerLockMode: 'locked-closed' } },
  About: { screen: AboutPage, navigationOptions: { drawerLockMode: 'locked-closed' } }
}, {
  drawerWidth: 300,
  headerMode: 'none'
});

export const AppNavigator = new StackNavigator({
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
  
  componentWillMount() {
    AppState.addEventListener('change', this._handleAppStateChange);
    BackHandler.addEventListener('hardwareBackPress', function() {
      const { dispatch, nav } = this.props;
      // There should be a better way to check this
      if (nav.routes.length > 0) {
        const subRoutes = nav.routes[0].routes[0].routes;
        const lastRoute = subRoutes[subRoutes.length - 1];
        if (nav.routes[0].routes[0].index > 0 &&
            ['About', 'Settings'].indexOf(lastRoute.key) > -1) {
          dispatch(NavigationActions.back());
          return true;
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
    const { notification } = nextProps;
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
      const navigateAction = NavigationActions.navigate({
        routeName: 'File',
        key: 'filePage',
        params: { uri: evt.url }
      });
      dispatch(navigateAction);
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
  nav: state.nav,
  notification: selectNotification(state),
  keepDaemonRunning: makeSelectClientSetting(SETTINGS.KEEP_DAEMON_RUNNING)(state),
  showNsfw: makeSelectClientSetting(SETTINGS.SHOW_NSFW)(state)
});
  
export default connect(mapStateToProps)(AppWithNavigationState);
