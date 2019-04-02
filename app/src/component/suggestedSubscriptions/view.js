import React from 'react';
import { ActivityIndicator, SectionList, Text, View } from 'react-native';
import { normalizeURI } from 'lbry-redux';
import { navigateToUri } from 'utils/helper';
import SubscribeButton from 'component/subscribeButton';
import SuggestedSubscriptionItem from 'component/suggestedSubscriptionItem';
import Colors from 'styles/colors';
import discoverStyle from 'styles/discover';
import subscriptionsStyle from 'styles/subscriptions';
import Link from 'component/link';

class SuggestedSubscriptions extends React.PureComponent {
  render() {
    const { suggested, loading, navigation } = this.props;

    if (loading) {
      return (
        <View>
          <ActivityIndicator size="large" color={Colors.LbryGreen} />
        </View>
      );
    }

    return suggested ? (
      <SectionList style={subscriptionsStyle.scrollContainer}
        renderItem={ ({item, index, section}) => (
            <SuggestedSubscriptionItem
              key={item}
              categoryLink={normalizeURI(item)}
              navigation={navigation} />
          )
        }
        renderSectionHeader={
          ({section: {title}}) => {
            const titleParts = title.split(';');
            const channelName = titleParts[0];
            const channelUri = normalizeURI(titleParts[1]);
            return (
              <View style={subscriptionsStyle.titleRow}>
                <Link style={subscriptionsStyle.channelTitle} text={channelName} onPress={() => {
                  navigateToUri(navigation, normalizeURI(channelUri));
                }} />
                <SubscribeButton style={subscriptionsStyle.subscribeButton} uri={channelUri} name={channelName} />
              </View>
            )
          }
        }
        sections={suggested.map(({ uri, label }) => ({ title: (label + ';' + uri), data: [uri] }))}
        keyExtractor={(item, index) => item}
      />
    ) : null;
  }
}

export default SuggestedSubscriptions;