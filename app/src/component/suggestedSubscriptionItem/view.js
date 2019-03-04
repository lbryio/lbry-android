import React from 'react';
import { buildURI, normalizeURI } from 'lbry-redux';
import { ActivityIndicator, FlatList, Text, View } from 'react-native';
import Colors from 'styles/colors';
import discoverStyle from 'styles/discover';
import FileItem from 'component/fileItem';
import subscriptionsStyle from 'styles/subscriptions';

class SuggestedSubscriptionItem extends React.PureComponent {
  componentDidMount() {
    const { fetching, categoryLink, fetchChannel, resolveUris, claims } = this.props;
    if (!fetching && categoryLink && (!claims || claims.length)) {
      fetchChannel(categoryLink);
    }
  }

  uriForClaim = (claim) => {
    const { name: claimName, claim_name: claimNameDownloaded, claim_id: claimId } = claim;
      const uriParams = {};

      // This is unfortunate
      // https://github.com/lbryio/lbry/issues/1159
      const name = claimName || claimNameDownloaded;
      uriParams.contentName = name;
      uriParams.claimId = claimId;
      const uri = buildURI(uriParams);

      return uri;
  }

  render() {
    const { categoryLink, fetching, obscureNsfw, claims, navigation } = this.props;

    if (!claims || !claims.length) {
      return (
        <View style={subscriptionsStyle.busyContainer}>
          <ActivityIndicator size={'small'} color={Colors.LbryGreen} />
        </View>
      );
    }

    if (claims && claims.length > 0) {
      return (
        <View style={subscriptionsStyle.suggestedContainer}>
          <FileItem
            style={subscriptionsStyle.compactMainFileItem}
            mediaStyle={subscriptionsStyle.fileItemMedia}
            uri={this.uriForClaim(claims[0])}
            navigation={navigation} />
          {(claims.length > 1) &&
          <FlatList style={subscriptionsStyle.compactItems}
            horizontal={true}
            renderItem={ ({item}) => (
                <FileItem
                  style={subscriptionsStyle.compactFileItem}
                  mediaStyle={subscriptionsStyle.compactFileItemMedia}
                  key={item}
                  uri={normalizeURI(item)}
                  navigation={navigation}
                  compactView={true} />
              )
            }
            data={claims.slice(1, 4).map(claim => this.uriForClaim(claim))}
            keyExtractor={(item, index) => item}
          />}
        </View>
      );
    }

    return null;
  }
}

export default SuggestedSubscriptionItem;
