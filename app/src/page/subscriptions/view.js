import React from 'react';
import NavigationActions from 'react-navigation';
import {
  ActivityIndicator,
  FlatList,
  NativeModules,
  SectionList,
  ScrollView,
  Text,
  View
} from 'react-native';
import { buildURI, parseURI } from 'lbry-redux';
import { uriFromFileInfo } from 'utils/helper';
import AsyncStorage from '@react-native-community/async-storage';
import moment from 'moment';
import Button from 'component/button';
import Colors from 'styles/colors';
import Constants from 'constants';
import fileListStyle from 'styles/fileList';
import subscriptionsStyle from 'styles/subscriptions';
import FloatingWalletBalance from 'component/floatingWalletBalance';
import FileItem from 'component/fileItem';
import Link from 'component/link';
import SuggestedSubscriptions from 'component/suggestedSubscriptions';
import UriBar from 'component/uriBar';

class SubscriptionsPage extends React.PureComponent {
  state = {
    showingSuggestedSubs: false
  };

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

    if (!hasSubscriptions && !this.state.showingSuggestedSubs) {
      this.setState({ showingSuggestedSubs: true });
    }

    return (
      <View style={subscriptionsStyle.container}>
        <UriBar navigation={navigation} />

        {(!this.state.showingSuggestedSubs && hasSubscriptions && !loading) &&
        <View style={subscriptionsStyle.viewModeRow}>
          <Link
            text={'All Subscriptions'}
            style={[subscriptionsStyle.viewModeLink,
                    ((viewMode === Constants.SUBSCRIPTIONS_VIEW_ALL) ? subscriptionsStyle.activeMode : subscriptionsStyle.inactiveMode)]}
            onPress={() => this.changeViewMode(Constants.SUBSCRIPTIONS_VIEW_ALL)}
          />
          <Link
            text={'Latest Only'}
            style={[subscriptionsStyle.viewModeLink,
                    ((viewMode === Constants.SUBSCRIPTIONS_VIEW_LATEST_FIRST) ? subscriptionsStyle.activeMode : subscriptionsStyle.inactiveMode)]}
            onPress={() => this.changeViewMode(Constants.SUBSCRIPTIONS_VIEW_LATEST_FIRST)}
          />
        </View>}

        {(!this.state.showingSuggestedSubs && hasSubscriptions && !loading) &&
        <View style={subscriptionsStyle.subContainer}>
          {(viewMode === Constants.SUBSCRIPTIONS_VIEW_ALL) &&
          <FlatList
            style={subscriptionsStyle.scrollContainer}
            contentContainerStyle={subscriptionsStyle.scrollPadding}
            renderItem={ ({item}) => (
              <FileItem
                style={subscriptionsStyle.fileItem}
                mediaStyle={fileListStyle.fileItemMedia}
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
          <View style={subscriptionsStyle.subContainer}>
            {unreadSubscriptions.length ?
              (<ScrollView
                style={subscriptionsStyle.scrollContainer}
                contentContainerStyle={subscriptionsStyle.scrollPadding}>
                {unreadSubscriptions.map(({ channel, uris }) => {
                    const { claimName } = parseURI(channel);
                    return uris.map(uri => (
                      <FileItem
                        style={subscriptionsStyle.fileItem}
                        mediaStyle={fileListStyle.fileItemMedia}
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

        {this.state.showingSuggestedSubs &&
          <View style={subscriptionsStyle.suggestedSubsContainer}>
            {!hasSubscriptions &&
            <Text style={subscriptionsStyle.infoText}>
              You are not subscribed to any channels at the moment. Here are some channels that we think you might enjoy.
            </Text>}

            {hasSubscriptions &&
            <View>
              <Text style={subscriptionsStyle.infoText}>
                You are currently subscribed to {numberOfSubscriptions} channel{(numberOfSubscriptions > 1) ? 's' : ''}.
              </Text>
              <Button
                style={subscriptionsStyle.button}
                text={"View my subscriptions"}
                onPress={() => this.setState({ showingSuggestedSubs: false })} />
            </View>
            }

            {loadingSuggested && <ActivityIndicator size="large" colors={Colors.LbryGreen} style={subscriptionsStyle.loading} />}
            {!loadingSuggested && <SuggestedSubscriptions navigation={navigation} />}
          </View>}

        <FloatingWalletBalance navigation={navigation} />
      </View>
    )
  }
}

export default SubscriptionsPage;
