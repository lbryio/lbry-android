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
import Constants from 'constants';
import discoverStyle from 'styles/discover';
import subscriptionsStyle from 'styles/subscriptions';
import FloatingWalletBalance from 'component/floatingWalletBalance';
import FileItem from 'component/fileItem';
import Link from 'component/link';
import SuggestedSubscriptions from 'component/suggestedSubscriptions';
import UriBar from 'component/uriBar';

class SubscriptionsPage extends React.PureComponent {
  componentWillMount() {
    const {
      doFetchMySubscriptions,
      doFetchRecommendedSubscriptions,
      pushDrawerStack,
    } = this.props;

    pushDrawerStack();
    doFetchMySubscriptions();
    doFetchRecommendedSubscriptions();
  }

  componentDidMount() {
    const { doSetViewMode, subscriptionsViewMode } = this.props;
    doSetViewMode(subscriptionsViewMode ? subscriptionsViewMode : Constants.SUBSCRIPTIONS_VIEW_ALL);
  }

  changeViewMode = (viewMode) => {
    const { setClientSetting, doSetViewMode } = this.props;
    setClientSetting(Constants.SETTING_SUBSCRIPTIONS_VIEW_MODE, viewMode);
    doSetViewMode(viewMode);
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
      navigation,
    } = this.props;
    const numberOfSubscriptions = subscribedChannels ? subscribedChannels.length : 0;
    const hasSubscriptions = numberOfSubscriptions > 0;

    return (
      <View style={subscriptionsStyle.container}>

      {hasSubscriptions && !loading &&
      <View style={subscriptionsStyle.viewModeRow}>
        <Link
          text={'All Subscriptions'}
          style={[subscriptionsStyle.viewModeLink,
                  ((viewMode === Constants.SUBSCRIPTIONS_VIEW_ALL) ? subscriptionsStyle.activeMode : subscriptionsStyle.inactiveMode)]}
          onPress={() => this.changeViewMode(Constants.SUBSCRIPTIONS_VIEW_ALL)}
        />
        <Link
          text={'Latest First'}
          style={[subscriptionsStyle.viewModeLink,
                  ((viewMode === Constants.SUBSCRIPTIONS_VIEW_LATEST_FIRST) ? subscriptionsStyle.activeMode : subscriptionsStyle.inactiveMode)]}
          onPress={() => this.changeViewMode(Constants.SUBSCRIPTIONS_VIEW_LATEST_FIRST)}
        />
      </View>}

      {(hasSubscriptions && !loading) &&
      <View style={subscriptionsStyle.container}>
        {(viewMode === Constants.SUBSCRIPTIONS_VIEW_ALL) &&
        <FlatList
          style={subscriptionsStyle.scrollContainer}
          contentContainerStyle={subscriptionsStyle.scrollPadding}
          renderItem={ ({item}) => (
            <FileItem
              style={subscriptionsStyle.fileItem}
              mediaStyle={discoverStyle.fileItemMedia}
              key={item}
              uri={uriFromFileInfo(item)}
              navigation={navigation}
              compactView={false}
              showDetails={true} />
            )
          }
          data={allSubscriptions.sort((a, b) => {
            return b.height - a.height;
          })}
          keyExtractor={(item, index) => uriFromFileInfo(item)} />}

        {(viewMode === Constants.SUBSCRIPTIONS_VIEW_LATEST_FIRST) &&
        <View style={subscriptionsStyle.container}>
          {unreadSubscriptions.length ?
            (<ScrollView
              style={subscriptionsStyle.scrollContainer}
              contentContainerStyle={subscriptionsStyle.scrollPadding}>
              {unreadSubscriptions.map(({ channel, uris }) => {
                  const { claimName } = parseURI(channel);
                  return uris.map(uri => (
                    <FileItem
                      style={subscriptionsStyle.fileItem}
                      mediaStyle={discoverStyle.fileItemMedia}
                      key={uri}
                      uri={uri}
                      navigation={navigation}
                      compactView={false}
                      showDetails={true} />));
              })}
            </ScrollView>) :
            (<View style={subscriptionsStyle.contentContainer}>
              <Text style={subscriptionsStyle.contentText}>All caught up! You might like the channels below.</Text>
              <SuggestedSubscriptions navigation={navigation} />
            </View>)
          }
        </View>}

      </View>}

      {(hasSubscriptions && loading) &&
        <View style={subscriptionsStyle.busyContainer}>
          <ActivityIndicator size="large" color={Colors.LbryGreen} style={subscriptionsStyle.loading} />
        </View>
      }

      {(!hasSubscriptions && !loading) &&
        <View style={subscriptionsStyle.container}>
          <Text style={subscriptionsStyle.infoText}>
            You are not subscribed to any channels at the moment. Here are some channels that we think you might enjoy.
          </Text>
          {loadingSuggested && <ActivityIndicator size="large" colors={Colors.LbryGreen} style={subscriptionsStyle.loading} />}
          {!loadingSuggested && <SuggestedSubscriptions navigation={navigation} />}
        </View>}

        <FloatingWalletBalance navigation={navigation} />
        <UriBar navigation={navigation} />
      </View>
    )
  }
}

export default SubscriptionsPage;
