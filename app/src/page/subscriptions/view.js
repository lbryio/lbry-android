import React from 'react';
import NavigationActions from 'react-navigation';
import {
  ActivityIndicator,
  AsyncStorage,
  FlatList,
  NativeModules,
  SectionList,
  Text,
  View
} from 'react-native';
import { buildURI } from 'lbry-redux';
import { uriFromFileInfo } from 'utils/helper';
import moment from 'moment';
import Colors from 'styles/colors';
import discoverStyle from 'styles/discover';
import subscriptionsStyle from 'styles/subscriptions';
import FloatingWalletBalance from 'component/floatingWalletBalance';
import FileItem from 'component/fileItem';
import UriBar from 'component/uriBar';

class SubscriptionsPage extends React.PureComponent {
  componentDidMount() {
    const {
      doFetchMySubscriptions,
      doFetchRecommendedSubscriptions
    } = this.props;
    doFetchMySubscriptions();
    doFetchRecommendedSubscriptions();
  }

  render() {
    const {
      subscribedChannels,
      allSubscriptions,
      loading,
      viewMode,
      doSetViewMode,
      loadingSuggested,
      firstRunCompleted,
      doCompleteFirstRun,
      doShowSuggestedSubs,
      showSuggestedSubs,
      unreadSubscriptions,
      navigation
    } = this.props;
    const numberOfSubscriptions = subscribedChannels ? subscribedChannels.length : 0;
    const hasSubscriptions = numberOfSubscriptions > 0;

    return (
      <View style={subscriptionsStyle.container}>

      {hasSubscriptions && !loading &&
        <FlatList
          style={subscriptionsStyle.scrollContainer}
          contentContainerStyle={subscriptionsStyle.scrollPadding}
          renderItem={ ({item}) => (
            <FileItem
              style={subscriptionsStyle.fileItem}
              mediaStyle={discoverStyle.fileItemMedia}
              key={item}
              uri={uriFromFileInfo(item)}
              navigation={navigation} />
            )
          }
          data={allSubscriptions}
          keyExtractor={(item, index) => uriFromFileInfo(item)} />}

      {hasSubscriptions && loading &&
        <View style={subscriptionsStyle.busyContainer}>
          <ActivityIndicator size="large" color={Colors.LbryGreen} style={subscriptionsStyle.loading} />
        </View>
      }

      {!hasSubscriptions &&
        <View style={subscriptionsStyle.busyContainer}>
          <Text style={subscriptionsStyle.infoText}>
            You are not subscribed to any channels. Feel free to discover new channels that you can subscribe to.
          </Text>
        </View>}

        <FloatingWalletBalance navigation={navigation} />
        <UriBar navigation={navigation} />
      </View>
    )
  }
}

export default SubscriptionsPage;
