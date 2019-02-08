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
import { normalizeURI, parseURI } from 'lbry-redux';
import moment from 'moment';
import Colors from 'styles/colors';
import discoverStyle from 'styles/discover';
import FloatingWalletBalance from 'component/floatingWalletBalance';
import FileItem from 'component/fileItem';
import RewardSummary from 'component/rewardSummary';
import UriBar from 'component/uriBar';

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

    const {
      fetchFeaturedUris,
      fetchRewardedContent,
      fetchSubscriptions
    } = this.props;

    fetchFeaturedUris();
    fetchRewardedContent();
    fetchSubscriptions();
  }

  subscriptionForUri = (uri, channelName) => {
    const { allSubscriptions } = this.props;
    const { claimId, claimName } = parseURI(uri);

    if (allSubscriptions) {
      for (let i = 0; i < allSubscriptions.length; i++) {
        const sub = allSubscriptions[i];
        if (sub.claim_id === claimId && sub.name === claimName && sub.channel_name === channelName) {
          return sub;
        }
      }
    }

    return null;
  }

  componentDidUpdate(prevProps, prevState) {
    const { allSubscriptions, unreadSubscriptions, enabledChannelNotifications } = this.props;

    const utility = NativeModules.UtilityModule;
    if (utility) {
      const hasUnread = prevProps.unreadSubscriptions &&
        prevProps.unreadSubscriptions.length !== unreadSubscriptions.length &&
        unreadSubscriptions.length > 0;

      if (hasUnread) {
        unreadSubscriptions.map(({ channel, uris }) => {
          const { claimName: channelName } = parseURI(channel);

          // check if notifications are enabled for the channel
          if (enabledChannelNotifications.indexOf(channelName) > -1) {
            uris.forEach(uri => {
              const sub = this.subscriptionForUri(uri, channelName);
              if (sub && sub.value && sub.value.stream) {
                let isPlayable = false;
                const source = sub.value.stream.source;
                const metadata = sub.value.stream.metadata;
                if (source) {
                  isPlayable = source.contentType && ['audio', 'video'].indexOf(source.contentType.substring(0, 5)) > -1;
                }
                if (metadata) {
                  utility.showNotificationForContent(uri, metadata.title, channelName, metadata.thumbnail, isPlayable);
                }
              }
            });
          }
        });
      }
    }
  }

  render() {
    const { featuredUris, fetchingFeaturedUris, navigation } = this.props;
    const hasContent = typeof featuredUris === 'object' && Object.keys(featuredUris).length,
      failedToLoad = !fetchingFeaturedUris && !hasContent;

    return (
      <View style={discoverStyle.container}>
        <RewardSummary navigation={navigation} />
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
                  mediaStyle={discoverStyle.fileItemMedia}
                  key={item}
                  uri={normalizeURI(item)}
                  navigation={navigation}
                  compactView={false}
                  showDetails={true} />
              )
            }
            renderSectionHeader={
              ({section: {title}}) => (<Text style={discoverStyle.categoryName}>{title}</Text>)
            }
            sections={Object.keys(featuredUris).map(category => ({ title: category, data: featuredUris[category] }))}
            keyExtractor={(item, index) => item}
          />
        }
        <FloatingWalletBalance navigation={navigation} />
        <UriBar navigation={navigation} />
      </View>
    );
  }
}

export default DiscoverPage;
