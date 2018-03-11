import React from 'react';
import DiscoverPage from '../page/discover';
import SplashScreen from '../page/splash';
import { addNavigationHelpers, DrawerNavigator, StackNavigator } from 'react-navigation';
import { connect } from 'react-redux';
import { addListener } from '../utils/redux';
import { StyleSheet } from 'react-native';
import Feather from 'react-native-vector-icons/Feather';

const styles = StyleSheet.create({
  drawerHamburger: {
    marginLeft: 8
  }
});

const discoverStack = StackNavigator({
  Discover: {
    screen: DiscoverPage,
  }
}, {
  headerMode: 'screen'
});

const drawer = DrawerNavigator({
  Discover: { screen: DiscoverPage },
}, {
  drawerWidth: 300,
  headerMode: 'screen'
});

export const AppNavigator = new StackNavigator({
  Splash: {
    screen: SplashScreen,
    navigationOptions: { header: null }
  },
  Main: {
    screen: drawer
  }
}, {
  headerMode: 'screen',
  navigationOptions: ({ navigation }) => ({
    headerLeft: () => <Feather name="menu" size={24} style={styles.drawerHamburger} onPress={() => navigation.navigate('DrawerOpen')} />
  })
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