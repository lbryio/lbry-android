import React from 'react';
import DiscoverPage from '../page/discover';
import FilePage from '../page/file';
import SearchPage from '../page/search';
import SettingsPage from '../page/settings';
import AboutPage from '../page/about';
import SplashScreen from '../page/splash';
import SearchInput from '../component/searchInput';
import {
  addNavigationHelpers,
  DrawerNavigator,
  StackNavigator,
  NavigationActions
} from 'react-navigation';
import { connect } from 'react-redux';
import { addListener } from '../utils/redux';
import { AppState, BackHandler, NativeModules, TextInput } from 'react-native';
import { SETTINGS } from 'lbry-redux';
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
      headerTitle: <SearchInput style={searchStyle.searchInput} />,
      headerRight: <Feather name="x" size={24} style={discoverStyle.rightHeaderIcon} onPress={() => navigation.dispatch(NavigationActions.back())} />
    })
  }
}, {
  headerMode: 'screen',
});

const drawer = DrawerNavigator({
  Discover: { screen: discoverStack },
  Settings: { screen: SettingsPage },
  About: { screen: AboutPage }
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
  componentWillMount() {
    AppState.addEventListener('change', this._handleAppStateChange);
    BackHandler.addEventListener('hardwareBackPress', function() {
      const { dispatch, navigation, nav } = this.props;
      // There should be a better way to check this
      if (nav.routes.length > 1) {
        const subRoutes = nav.routes[1].routes[0].routes;
        const lastRoute = subRoutes[subRoutes.length - 1];
        if (['Settings'].indexOf(lastRoute.key) > -1) {
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

  _handleAppStateChange = (nextAppState) => {
    const { keepDaemonRunning } = this.props;
    if (AppState.currentState &&
        AppState.currentState.match(/inactive|background/) &&
        NativeModules.DaemonServiceControl) {
      if (!keepDaemonRunning) {
        // terminate the daemon background service when is suspended / inactive 
        NativeModules.DaemonServiceControl.stopService();
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
  nav: state.nav,
  keepDaemonRunning: makeSelectClientSetting(SETTINGS.KEEP_DAEMON_RUNNING)(state),
  showNsfw: makeSelectClientSetting(SETTINGS.SHOW_NSFW)(state)
});
  
export default connect(mapStateToProps)(AppWithNavigationState);