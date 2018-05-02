import React from 'react';
import AboutPage from '../page/about';
import DiscoverPage from '../page/discover';
import FilePage from '../page/file';
import SearchPage from '../page/search';
import SettingsPage from '../page/settings';
import SplashScreen from '../page/splash';
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
  NativeModules,
  TextInput,
  ToastAndroid
} from 'react-native';
import { SETTINGS, doHideNotification, selectNotification } from 'lbry-redux';
import { makeSelectClientSetting } from '../redux/selectors/settings';
import Feather from 'react-native-vector-icons/Feather';
import discoverStyle from '../styles/discover';
import searchStyle from '../styles/search';

const discoverStack = StackNavigator({
  Discover: {
    screen: DiscoverPage,
    navigationOptions: ({ navigation }) => ({
      title: 'Discover',
      headerLeft: <Feather name="menu" size={24} style={discoverStyle.drawerHamburger} onPress={() => navigation.navigate('DrawerOpen')} />,
      headerRight: <Feather name="search" size={24} style={discoverStyle.rightHeaderIcon} onPress={() => navigation.navigate('Search')} />
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
      headerTitle: <SearchInput style={searchStyle.searchInput} />,
      headerRight: <Feather name="x" size={24} style={discoverStyle.rightHeaderIcon} onPress={() => navigation.dispatch(NavigationActions.back())} />
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
      headerRight: <Feather name="search" size={24} style={discoverStyle.rightHeaderIcon} onPress={() => navigation.navigate('Search')} />
    })
  }
}, {
  headerMode: 'screen'
});

const drawer = DrawerNavigator({
  Discover: { screen: discoverStack },
  Wallet: { screen: walletStack },
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
      const { dispatch, navigation, nav } = this.props;
      // There should be a better way to check this
      if (nav.routes.length > 1) {
        const subRoutes = nav.routes[1].routes[0].routes;
        const lastRoute = subRoutes[subRoutes.length - 1];
        if (['About', 'Settings'].indexOf(lastRoute.key) > -1) {
          dispatch({ type: 'Navigation/BACK' });
          return true;
        }
        if (nav.routes[1].routeName === 'Main') {
          if (nav.routes[1].routes[0].routes[0].index > 0) {    
            dispatch({ type: 'Navigation/BACK' });
            return true;
          }
        }
      }
      return false;
    }.bind(this));
  }
  
  componentWillUnmount() {
    AppState.removeEventListener('change', this._handleAppStateChange);
    BackHandler.removeEventListener('hardwareBackPress');
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
