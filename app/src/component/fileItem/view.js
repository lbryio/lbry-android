import React from 'react';
import { normalizeURI } from 'lbry-redux';
import { Text, View } from 'react-native';
import FileItemMedia from '../fileItemMedia';
import FilePrice from '../filePrice';
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
      style
    } = this.props;

    const uri = normalizeURI(this.props.uri);
    const title = metadata && metadata.title ? metadata.title : uri;
    const thumbnail = metadata && metadata.thumbnail ? metadata.thumbnail : null;
    const obscureNsfw = this.props.obscureNsfw && metadata && metadata.nsfw;
    const isRewardContent = claim && rewardedContentClaimIds.includes(claim.claim_id);
    const channelName = claim ? claim.channel_name : null;

    let description = '';
    if (isResolvingUri && !claim) {
      description = 'Loading...';
    } else if (metadata && metadata.description) {
      description = metadata.description;
    } else if (claim === null) {
      description = 'This address contains no content.';
    }
    
    return (
      <View style={style}>
        <FileItemMedia title={title} thumbnail={thumbnail} />
        <FilePrice uri={uri} style={discoverStyle.filePriceContainer} textStyle={discoverStyle.filePriceText} />
        <Text style={discoverStyle.fileItemName}>{title}</Text>
        {channelName &&
          <Text style={discoverStyle.channelName}>{channelName}</Text>}
      </View>
    );
  }
}

export default FileItem;