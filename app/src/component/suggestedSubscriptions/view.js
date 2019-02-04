import React from 'react';
import { ActivityIndicator, SectionList, Text, View } from 'react-native';
import { normalizeURI } from 'lbry-redux';
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
        renderItem={ ({item, index, section}) => { console.log(item); return (
            <SuggestedSubscriptionItem
              key={item}
              categoryLink={normalizeURI(item)}
              navigation={navigation} />
          ); }
        }
        renderSectionHeader={
          ({section: {title}}) => (<Link style={discoverStyle.categoryName} text={title} href={normalizeURI(title)} />)
        }
        sections={suggested.map(({ uri, label }) => ({ title: label, data: [uri] }))}
        keyExtractor={(item, index) => item}
      />
    ) : null;
  }
}

export default SuggestedSubscriptions;