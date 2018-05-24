import React from 'react';
import { Lbry } from 'lbry-redux';
import {
  ActivityIndicator,
  Alert,
  Button,
  Text,
  TextInput,
  View,
  ScrollView,
  StatusBar,
  TouchableOpacity,
  NativeModules
} from 'react-native';
import Colors from '../../styles/colors';
import ChannelPage from '../channel';
import FileItemMedia from '../../component/fileItemMedia';
import FileDownloadButton from '../../component/fileDownloadButton';
import MediaPlayer from '../../component/mediaPlayer';
import UriBar from '../../component/uriBar';
import Video from 'react-native-video';
import filePageStyle from '../../styles/filePage';

class FilePage extends React.PureComponent {
  static navigationOptions = {
    title: ''
  };
  
  constructor(props) {
    super(props);
    this.state = {
      mediaLoaded: false,
      fullscreenMode: false
    };
  }
  
  componentDidMount() {
    StatusBar.setHidden(false);
    
    const { isResolvingUri, resolveUri, navigation } = this.props;
    const { uri } = navigation.state.params;
    if (!isResolvingUri) resolveUri(uri);
    
    this.fetchFileInfo(this.props);
    this.fetchCostInfo(this.props);

    if (NativeModules.Mixpanel) {
      NativeModules.Mixpanel.track('Open File Page', { Uri: uri });
    }
  }

  componentDidUpdate() {
    this.fetchFileInfo(this.props);
    const { isResolvingUri, resolveUri, claim, navigation } = this.props;
    const { uri } = navigation.state.params;

    if (!isResolvingUri && claim === undefined && uri) {
      resolveUri(uri);
    }
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
  
  onStopDownloadPressed = () => {
    const { deleteFile, stopDownload, fileInfo, navigation } = this.props;
    
    Alert.alert(
      'Stop download',
      'Are you sure you want to stop downloading this file?',
      [
        { text: 'No' },
        { text: 'Yes', onPress: () => { stopDownload(navigation.state.params.uri, fileInfo); } }
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
      isResolvingUri,
      blackListedOutpoints,
      navigation
    } = this.props;
    const { uri } = navigation.state.params;
    
    let innerContent = null;
    if ((isResolvingUri && !claim) || !claim) {
      innerContent = (
        <View style={filePageStyle.container}>
          {isResolvingUri && 
          <View style={filePageStyle.busyContainer}>
            <ActivityIndicator size="large" color={Colors.LbryGreen} />
            <Text style={filePageStyle.infoText}>Loading decentralized data...</Text>
          </View>}
          { claim === null && !isResolvingUri &&
            <View style={filePageStyle.container}>
              <Text style={filePageStyle.emptyClaimText}>There's nothing at this location.</Text>
            </View>
          }
          <UriBar value={uri} navigation={navigation} />
        </View>
      );
    } else if (claim && claim.name.length && claim.name[0] === '@') {
      innerContent = (
        <ChannelPage uri={uri} navigation={navigation} />
      );
    } else if (claim) {
      const completed = fileInfo && fileInfo.completed;  
      const title = metadata.title;
      const isRewardContent = rewardedContentClaimIds.includes(claim.claim_id);
      const description = metadata.description ? metadata.description : null;
      const mediaType = Lbry.getMediaType(contentType);
      const isPlayable = mediaType === 'video' || mediaType === 'audio';
      const { height, channel_name: channelName, value } = claim;
      const showActions = !this.state.fullscreenMode &&
        (completed || (fileInfo && !fileInfo.stopped && fileInfo.written_bytes < fileInfo.total_bytes));
      const channelClaimId =
        value && value.publisherSignature && value.publisherSignature.certificateId;
      
      const playerStyle = [filePageStyle.player, this.state.fullscreenMode ?
        filePageStyle.fullscreenPlayer : filePageStyle.containedPlayer];
      const playerBgStyle = [filePageStyle.playerBackground, this.state.fullscreenMode ?
        filePageStyle.fullscreenPlayerBackground : filePageStyle.containedPlayerBackground]; 
      // at least 2MB (or the full download) before media can be loaded
      const canLoadMedia = fileInfo &&
        (fileInfo.written_bytes >= 2097152 || fileInfo.written_bytes == fileInfo.total_bytes); // 2MB = 1024*1024*2
          
      innerContent = (
        <View style={filePageStyle.pageContainer}>
          <View style={filePageStyle.mediaContainer}>  
            {(!fileInfo || (isPlayable && !canLoadMedia)) &&
              <FileItemMedia style={filePageStyle.thumbnail} title={title} thumbnail={metadata.thumbnail} />}
            {isPlayable && !this.state.mediaLoaded && <ActivityIndicator size="large" color={Colors.LbryGreen} style={filePageStyle.loading} />}
            {!completed && !canLoadMedia && <FileDownloadButton uri={uri} style={filePageStyle.downloadButton} />}
          </View>
          {canLoadMedia && <View style={playerBgStyle} />}
          {canLoadMedia && <MediaPlayer fileInfo={fileInfo}
                                          uri={uri}
                                          style={playerStyle}
                                          onFullscreenToggled={this.handleFullscreenToggle} 
                                          onMediaLoaded={() => { this.setState({ mediaLoaded: true }); }}/>}
          
          { showActions &&
          <View style={filePageStyle.actions}>
            {completed && <Button color="red" title="Delete" onPress={this.onDeletePressed} />}
            {!completed && fileInfo && !fileInfo.stopped && fileInfo.written_bytes < fileInfo.total_bytes &&
              <Button color="red" title="Stop Download" onPress={this.onStopDownloadPressed} />
            }
          </View>}
          <ScrollView style={showActions ? filePageStyle.scrollContainerActions : filePageStyle.scrollContainer}>
            <Text style={filePageStyle.title} selectable={true}>{title}</Text>
            {channelName && <Text style={filePageStyle.channelName} selectable={true}>{channelName}</Text>}
            {description && <Text style={filePageStyle.description} selectable={true}>{description}</Text>}
          </ScrollView>
          <UriBar value={uri} navigation={navigation} />
        </View>
      );
    }
    
    return innerContent;
  }
}

export default FilePage;
