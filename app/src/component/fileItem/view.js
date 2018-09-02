import React from 'react';
import { normalizeURI } from 'lbry-redux';
import { NavigationActions } from 'react-navigation';
import { NativeModules, Text, View, TouchableOpacity } from 'react-native';
import { navigateToUri } from '../../utils/helper';
import FileItemMedia from '../fileItemMedia';
import FilePrice from '../filePrice';
import Link from '../link';
import NsfwOverlay from '../nsfwOverlay';
import discoverStyle from '../../styles/discover';

class FileItem extends React.PureComponent {
  constructor(props) {
    super(props);
  }

  componentWillMount() {
    this.resolve(this.props);
  }

  componentWillReceiveProps(nextProps) {
    this.resolve(nextProps);
  }

  resolve(props) {
    const { isResolvingUri, resolveUri, claim, uri } = props;

    if (!isResolvingUri && claim === undefined && uri) {
      resolveUri(uri);
    }
  }

  render() {
    const {
      claim,
      fileInfo,
      metadata,
      isResolvingUri,
      rewardedContentClaimIds,
      style,
      mediaStyle,
      navigation
    } = this.props;

    const uri = normalizeURI(this.props.uri);
    const title = metadata && metadata.title ? metadata.title : uri;
    const thumbnail = metadata && metadata.thumbnail ? metadata.thumbnail : null;
    const obscureNsfw = this.props.obscureNsfw && metadata && metadata.nsfw;
    const isRewardContent = claim && rewardedContentClaimIds.includes(claim.claim_id);
    const channelName = claim ? claim.channel_name : null;

    return (
      <View style={style}>
        <TouchableOpacity style={discoverStyle.container} onPress={() => {
              if (NativeModules.Mixpanel) {
                NativeModules.Mixpanel.track('Discover Tap', { Uri: uri });
              }
              navigateToUri(navigation, uri);
            }
          }>
          <FileItemMedia title={title}
                         thumbnail={thumbnail}
                         blurRadius={obscureNsfw ? 15 : 0}
                         resizeMode="cover"
                         isResolvingUri={isResolvingUri}
                         style={mediaStyle} />
          <FilePrice uri={uri} style={discoverStyle.filePriceContainer} textStyle={discoverStyle.filePriceText} />
          <Text style={discoverStyle.fileItemName}>{title}</Text>
          {channelName &&
            <Link style={discoverStyle.channelName} text={channelName} onPress={() => {
              const channelUri = normalizeURI(channelName);
              navigateToUri(navigation, channelUri);
            }} />}
        </TouchableOpacity>
        {obscureNsfw && <NsfwOverlay onPress={() => navigation.navigate({ routeName: 'Settings', key: 'settingsPage' })} />}
      </View>
    );
  }
}

export default FileItem;