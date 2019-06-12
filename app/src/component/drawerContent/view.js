import React from 'react';
import { DrawerItems, SafeAreaView } from 'react-navigation';
import { ScrollView } from 'react-native';
import Constants from 'constants';
import discoverStyle from 'styles/discover';

class DrawerContent extends React.PureComponent {
  render() {
    const props = this.props;
    const { navigation, onItemPress } = props;

    return (
      <ScrollView>
        <SafeAreaView style={discoverStyle.drawerContentContainer} forceInset={{ top: 'always', horizontal: 'never' }}>
          <DrawerItems
            {...props}
            onItemPress={route => {
              const { routeName } = route.route;
              if (Constants.FULL_ROUTE_NAME_DISCOVER === routeName) {
                navigation.navigate({ routeName: Constants.DRAWER_ROUTE_DISCOVER });
                return;
              }

              if (Constants.FULL_ROUTE_NAME_WALLET === routeName) {
                navigation.navigate({ routeName: Constants.DRAWER_ROUTE_WALLET });
                return;
              }

              onItemPress(route);
            }}
          />
        </SafeAreaView>
      </ScrollView>
    );
  }
}

export default DrawerContent;
