import React from 'react';
import { Text, View, ScrollView } from 'react-native';
import filePageStyle from '../../styles/filePage';
import FileItemMedia from '../../component/fileItemMedia';

class FilePage extends React.PureComponent {
  static navigationOptions = {
    title: ''
  };
    
  componentDidMount() {
    this.fetchFileInfo(this.props);
    this.fetchCostInfo(this.props);
  }

  componentWillReceiveProps(nextProps) {
    this.fetchFileInfo(nextProps);
  }

  fetchFileInfo(props) {
    if (props.fileInfo === undefined) {
      props.fetchFileInfo(props.navigation.state.params.uri);
    }
  }

  fetchCostInfo(props) {
    if (props.costInfo === undefined) {
      props.fetchCostInfo(props.navigation.state.params.uri);
    }
  }

  render() {
    const {
      claim,
      fileInfo,
      metadata,
      contentType,
      tab,
      uri,
      rewardedContentClaimIds,
    } = this.props;
    
    if (!claim || !metadata) {
      return (
        <View style={filePageStyle.container}>
          <Text style={filePageStyle.emptyClaimText}>Empty claim or metadata info.</Text>
        </View>
      );
    }
    
    const title = metadata.title;
    const isRewardContent = rewardedContentClaimIds.includes(claim.claim_id);
    const description = metadata.description ? metadata.description : null;
    //const mediaType = lbry.getMediaType(contentType);
    //const player = require('render-media');
    //const obscureNsfw = this.props.obscureNsfw && metadata && metadata.nsfw;
    /*const isPlayable =
      Object.values(player.mime).indexOf(contentType) !== -1 || mediaType === 'audio';*/
    const { height, channel_name: channelName, value } = claim;
    const channelClaimId =
      value && value.publisherSignature && value.publisherSignature.certificateId;
    
    
    return (
      <View style={filePageStyle.pageContainer}>
        <View>
          <FileItemMedia style={filePageStyle.thumbnail} title={title} thumbnail={metadata.thumbnail} />
        </View>
        <ScrollView style={filePageStyle.scrollContainer}>
          <Text style={filePageStyle.title}>{title}</Text>
          {channelName && <Text style={filePageStyle.channelName}>{channelName}</Text>}
          {description && <Text style={filePageStyle.description}>{description}</Text>}
        </ScrollView>
      </View>
    );
  }
}

export default FilePage;
