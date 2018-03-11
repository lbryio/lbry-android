import React from 'react';
import DiscoverPage from '../page/discover';
import FilePage from '../page/file';
import SplashScreen from '../page/splash';
import { addNavigationHelpers, DrawerNavigator, StackNavigator } from 'react-navigation';
import { connect } from 'react-redux';
import { addListener } from '../utils/redux';
import { StyleSheet } from 'react-native';
import Feather from 'react-native-vector-icons/Feather';
import discoverStyle from '../styles/discover';

const discoverStack = StackNavigator({
  Discover: {
    screen: DiscoverPage,
    navigationOptions: ({ navigation }) => ({
      title: 'Discover',
      headerLeft: <Feather name="menu" size={24} style={discoverStyle.drawerHamburger} onPress={() => navigation.navigate('DrawerOpen')} />
    })
  },
  File: { screen: FilePage }
}, {
  headerMode: 'screen',
});

const drawer = DrawerNavigator({
  Discover: { screen: discoverStack },
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
});
  
export default connect(mapStateToProps)(AppWithNavigationState);