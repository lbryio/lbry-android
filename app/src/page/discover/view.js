import React from 'react';
import FeaturedCategory from '../../component/featuredCategory';
import NavigationActions from 'react-navigation';
import { AsyncStorage, NativeModules, ScrollView, Text, View } from 'react-native';
import discoverStyle from '../../styles/discover';
import Feather from 'react-native-vector-icons/Feather';

class DiscoverPage extends React.PureComponent {
  componentWillMount() {
    this.props.fetchFeaturedUris();
    
    // Track the total time taken if this is the first launch
    AsyncStorage.getItem('firstLaunchTime').then(start => {
      if (start !== null && !isNaN(start)) {
        // We know this is the first app launch because firstLaunchTime is set and it's a valid number
        const now = moment().unix();
        const delta = now - start;
        AsyncStorage.getItem('firstLaunchSuspended').then(suspended => {
          if (NativeModules.Mixpanel) {
            NativeModules.Mixpanel.track('First Run Time', {
              totalSeconds: delta,
              appSuspended: !!suspended
            });
          }
        });
      }
    });
  }

  render() {
    const { featuredUris, fetchingFeaturedUris } = this.props;
    const hasContent = typeof featuredUris === 'object' && Object.keys(featuredUris).length,
      failedToLoad = !fetchingFeaturedUris && !hasContent;

    return (
      <View style={discoverStyle.container}>
        {!hasContent && fetchingFeaturedUris && <Text style={discoverStyle.title}>Fetching content...</Text>}
        {hasContent &&
          <ScrollView style={discoverStyle.scrollContainer}>
          {hasContent &&
            Object.keys(featuredUris).map(
              category =>
                featuredUris[category].length ? (
                  <FeaturedCategory
                      key={category}
                      category={category}
                      names={featuredUris[category]}
                      navigation={this.props.navigation}
                    />
                ) : (
                  ''
                )
            )}
          </ScrollView>
        }
      </View>
    );
  }
}

export default DiscoverPage;
