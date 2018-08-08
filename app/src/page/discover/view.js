import React from 'react';
import NavigationActions from 'react-navigation';
import {
  ActivityIndicator,
  AsyncStorage,
  NativeModules,
  SectionList,
  Text,
  View
} from 'react-native';
import { normalizeURI } from 'lbry-redux';
import moment from 'moment';
import FileItem from '../../component/fileItem';
import discoverStyle from '../../styles/discover';
import Colors from '../../styles/colors';
import UriBar from '../../component/uriBar';

class DiscoverPage extends React.PureComponent {
  componentDidMount() {
    // Track the total time taken if this is the first launch
    AsyncStorage.getItem('firstLaunchTime').then(startTime => {
      if (startTime !== null && !isNaN(parseInt(startTime, 10))) {
        // We don't need this value anymore once we've retrieved it
        AsyncStorage.removeItem('firstLaunchTime');

        // We know this is the first app launch because firstLaunchTime is set and it's a valid number
        const start = parseInt(startTime, 10);
        const now = moment().unix();
        const delta = now - start;
        AsyncStorage.getItem('firstLaunchSuspended').then(suspended => {
          AsyncStorage.removeItem('firstLaunchSuspended');
          const appSuspended = (suspended === 'true');
          if (NativeModules.Mixpanel) {
            NativeModules.Mixpanel.track('First Run Time', {
              'Total Seconds': delta, 'App Suspended': appSuspended
            });
          }
        });
      }
    });

    this.props.fetchFeaturedUris();
  }

  render() {
    const { featuredUris, fetchingFeaturedUris, navigation } = this.props;
    const hasContent = typeof featuredUris === 'object' && Object.keys(featuredUris).length,
      failedToLoad = !fetchingFeaturedUris && !hasContent;

    return (
      <View style={discoverStyle.container}>
        {!hasContent && fetchingFeaturedUris && (
          <View style={discoverStyle.busyContainer}>
            <ActivityIndicator size="large" color={Colors.LbryGreen} />
            <Text style={discoverStyle.title}>Fetching content...</Text>
          </View>
        )}
        {hasContent &&
          <SectionList style={discoverStyle.scrollContainer}
            renderItem={ ({item, index, section}) => (
                <FileItem
                  style={discoverStyle.fileItem}
                  key={item}
                  uri={normalizeURI(item)}
                  navigation={navigation} />
              )
            }
            renderSectionHeader={
              ({section: {title}}) => (<Text style={discoverStyle.categoryName}>{title}</Text>)
            }
            sections={Object.keys(featuredUris).map(category => ({ title: category, data: featuredUris[category] }))}
            keyExtractor={(item, index) => item}
          />
        }
        <UriBar navigation={navigation} />
      </View>
    );
  }
}

export default DiscoverPage;
