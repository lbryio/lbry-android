import React from 'react';
import DiscoverPage from '../page/discover';
import FilePage from '../page/file';
import SettingsPage from '../page/settings';
import SplashScreen from '../page/splash';
import { addNavigationHelpers, DrawerNavigator, StackNavigator } from 'react-navigation';
import { connect } from 'react-redux';
import { addListener } from '../utils/redux';
import { AppState, BackHandler, NativeModules } from 'react-native';
import { SETTINGS } from 'lbry-redux';
import Feather from 'react-native-vector-icons/Feather';
import discoverStyle from '../styles/discover';
import { makeSelectClientSetting } from '../redux/selectors/settings';

const discoverStack = StackNavigator({
  Discover: {
    screen: DiscoverPage,
    navigationOptions: ({ navigation }) => ({
      title: 'Discover',
      headerLeft: <Feather name="menu" size={24} style={discoverStyle.drawerHamburger} onPress={() => navigation.navigate('DrawerOpen')} />
    })
  },
  File: {
    screen: FilePage,
    navigationOptions: {
      header: null,
      drawerLockMode: 'locked-closed'
    }
  }
}, {
  headerMode: 'screen',
});

const drawer = DrawerNavigator({
  Discover: { screen: discoverStack },
  Settings: {
    screen: SettingsPage,
    headerMode: 'screen'
  }
}, {
  drawerWidth: 300,
  headerMode: 'none'
});

export const AppNavigator = new StackNavigator({
  Splash: {
    screen: SplashScreen
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
      if (nav.routes.length === 2 && nav.routes[1].routeName === 'Main') {
        if (nav.routes[1].routes[0].routes[0].index > 0) {    
          dispatch({ type: 'Navigation/BACK' });
          return true;
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