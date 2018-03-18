import React from 'react';
import { Lbry } from 'lbry-redux';
import { Text, View, ScrollView, TouchableOpacity } from 'react-native';
import Video from 'react-native-video';
import filePageStyle from '../../styles/filePage';
import FileItemMedia from '../../component/fileItemMedia';
import FileDownloadButton from '../../component/fileDownloadButton';

class FilePage extends React.PureComponent {
  state = {
    rate: 1,
    volume: 1,
    muted: false,
    resizeMode: 'contain',
    duration: 0.0,
    currentTime: 0.0,
    paused: true,
  };
  
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
      rewardedContentClaimIds,
      navigation
    } = this.props; 
    
    if (!claim || !metadata) {
      return (
        <View style={filePageStyle.container}>
          <Text style={filePageStyle.emptyClaimText}>Empty claim or metadata info.</Text>
        </View>
      );
    }
    
    const completed = fileInfo && fileInfo.completed;  
    const title = metadata.title;
    const isRewardContent = rewardedContentClaimIds.includes(claim.claim_id);
    const description = metadata.description ? metadata.description : null;
    const mediaType = Lbry.getMediaType(contentType);
    const isPlayable = mediaType === 'video' || mediaType === 'audio';
    const { height, channel_name: channelName, value } = claim;
    const channelClaimId =
      value && value.publisherSignature && value.publisherSignature.certificateId;
    
    return (
      <View style={filePageStyle.pageContainer}>
        <View style={filePageStyle.mediaContainer}>
          {(!fileInfo || !isPlayable) && <FileItemMedia style={filePageStyle.thumbnail} title={title} thumbnail={metadata.thumbnail} />}
          {!completed && <FileDownloadButton uri={navigation.state.params.uri} style={filePageStyle.downloadButton} />}
          
          {fileInfo && isPlayable &&
            <TouchableOpacity
              style={filePageStyle.player}
              onPress={() => this.setState({ paused: !this.state.paused })}>
              <Video source={{ uri: 'file:///' + fileInfo.download_path }}
                     resizeMode="cover"
                     playInBackground={true}
                     style={filePageStyle.player}
                     rate={this.state.rate}
                     volume={this.state.volume}
                     paused={this.state.paused}
                    />
            </TouchableOpacity>
          }
          
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
