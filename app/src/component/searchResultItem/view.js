import React from 'react';
import { normalizeURI, parseURI } from 'lbry-redux';
import { ActivityIndicator, Text, TouchableOpacity, View } from 'react-native';
import Colors from '../../styles/colors';
import FileItemMedia from '../fileItemMedia';
import NsfwOverlay from '../../component/nsfwOverlay';
import searchStyle from '../../styles/search';

class SearchResultItem extends React.PureComponent {
  render() {
    const {
      claim,
      metadata,
      isResolvingUri,
      showUri,
      isDownloaded,
      style,
      onPress,
      navigation
    } = this.props;

    const uri = normalizeURI(this.props.uri);
    const obscureNsfw = this.props.obscureNsfw && metadata && metadata.nsfw;
    const title = metadata && metadata.title ? metadata.title : parseURI(uri).contentName;

    let name;
    let channel;
    if (claim) {
      name = claim.name;
      channel = claim.channel_name;
    }

    return (
      <View style={style}>
        <TouchableOpacity style={style} onPress={onPress}>
          <View style={searchStyle.thumbnailContainer}>
            <FileItemMedia style={searchStyle.thumbnail}
                           blurRadius={obscureNsfw ? 15 : 0}
                           title={title}
                           thumbnail={metadata ? metadata.thumbnail : null} />
          </View>
          <View style={searchStyle.detailsContainer}>
            {isResolvingUri && (
            <View>
              <Text style={searchStyle.uri}>{uri}</Text>
              <View style={searchStyle.row}>
                <ActivityIndicator size={"small"} color={Colors.LbryGreen} />
              </View>
            </View>)}
            {!isResolvingUri && <Text style={searchStyle.title}>{title || name}</Text>}
            {!isResolvingUri && channel && <Text style={searchStyle.publisher}>{channel}</Text>}
          </View>
        </TouchableOpacity>
        {obscureNsfw && <NsfwOverlay onPress={() => navigation.navigate({ routeName: 'Settings', key: 'settingsPage' })} />}
      </View>
    );
  }
}

export default SearchResultItem;
