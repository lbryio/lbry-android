import React from 'react';
import { Lbry } from 'lbry-redux';
import {
  ActivityIndicator,
  Alert,
  Button,
  Text,
  View,
  ScrollView,
  StatusBar,
  TouchableOpacity,
  NativeModules
} from 'react-native';
import FileItemMedia from '../../component/fileItemMedia';
import FileDownloadButton from '../../component/fileDownloadButton';
import MediaPlayer from '../../component/mediaPlayer';
import Video from 'react-native-video';
import filePageStyle from '../../styles/filePage';

class FilePage extends React.PureComponent {
  state = {
    mediaLoaded: false,
    fullscreenMode: false
  };
  
  static navigationOptions = {
    title: ''
  };
    
  componentDidMount() {
    StatusBar.setHidden(false);
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
  
  handleFullscreenToggle = (mode) => {
    this.setState({ fullscreenMode: mode });
    StatusBar.setHidden(mode);
    if (NativeModules.ScreenOrientation) {
      if (mode) {
        // fullscreen, so change orientation to landscape mode
        NativeModules.ScreenOrientation.lockOrientationLandscape();
      } else {
        NativeModules.ScreenOrientation.unlockOrientation();
      }
    }
  }
  
  onDeletePressed = () => {
    const { deleteFile, fileInfo } = this.props;
    
    Alert.alert(
      'Delete file',
      'Are you sure you want to remove this file from your device?',
      [
        { text: 'No' },
        { text: 'Yes', onPress: () => { deleteFile(fileInfo.outpoint, true); } }
      ],
      { cancelable: true }
    );
  }
  
  componentWillUnmount() {
    StatusBar.setHidden(false);
    if (NativeModules.ScreenOrientation) {
      NativeModules.ScreenOrientation.unlockOrientation();
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
    const showActions = (completed || (fileInfo && !fileInfo.stopped && fileInfo.written_bytes < fileInfo.total_bytes));
    const channelClaimId =
      value && value.publisherSignature && value.publisherSignature.certificateId;
    
    return (
      <View style={filePageStyle.pageContainer}>
        <View style={this.state.fullscreenMode ? filePageStyle.fullscreenMedia : filePageStyle.mediaContainer}>  
          {(!fileInfo || (isPlayable && !this.state.mediaLoaded)) &&
            <FileItemMedia style={filePageStyle.thumbnail} title={title} thumbnail={metadata.thumbnail} />}
          {isPlayable && !this.state.mediaLoaded && <ActivityIndicator size="large" color="#40b89a" style={filePageStyle.loading} />}
          {!completed && <FileDownloadButton uri={navigation.state.params.uri} style={filePageStyle.downloadButton} />}
          {fileInfo && isPlayable && <MediaPlayer fileInfo={fileInfo}
                                                  style={filePageStyle.player}
                                                  onFullscreenToggled={this.handleFullscreenToggle} 
                                                  onMediaLoaded={() => { this.setState({ mediaLoaded: true }); }}/>}
        </View>
        { showActions &&
        <View style={filePageStyle.actions}>
          {completed && <Button color="red" title="Delete" onPress={this.onDeletePressed} />}
          {fileInfo && !fileInfo.stopped && fileInfo.written_bytes < fileInfo.total_bytes &&
            <Button color="red" title="Stop Download" onPress={this.onStopDownloadPressed} />
          }
        </View>}
        <ScrollView style={showActions ? filePageStyle.scrollContainerActions : filePageStyle.scrollContainer}>
          <Text style={filePageStyle.title}>{title}</Text>
          {channelName && <Text style={filePageStyle.channelName}>{channelName}</Text>}
          {description && <Text style={filePageStyle.description}>{description}</Text>}
        </ScrollView>
      </View>
    );
  }
}

export default FilePage;
