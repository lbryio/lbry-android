import React from 'react';
import { Lbry, normalizeURI } from 'lbry-redux';
import {
  ActivityIndicator,
  Alert,
  Button,
  Dimensions,
  NativeModules,
  ScrollView,
  StatusBar,
  StyleSheet,
  Text,
  TextInput,
  TouchableOpacity,
  View,
  WebView
} from 'react-native';
import ImageViewer from 'react-native-image-zoom-viewer';
import Colors from '../../styles/colors';
import ChannelPage from '../channel';
import FileDownloadButton from '../../component/fileDownloadButton';
import FileItemMedia from '../../component/fileItemMedia';
import FilePrice from '../../component/filePrice';
import Link from '../../component/link';
import MediaPlayer from '../../component/mediaPlayer';
import UriBar from '../../component/uriBar';
import Video from 'react-native-video';
import filePageStyle from '../../styles/filePage';
import uriBarStyle from '../../styles/uriBar';

class FilePage extends React.PureComponent {
  static navigationOptions = {
    title: ''
  };

  playerBackground = null;

  player = null;

  constructor(props) {
    super(props);
    this.state = {
      mediaLoaded: false,
      autoplayMedia: false,
      fullscreenMode: false,
      showImageViewer: false,
      showWebView: false,
      imageUrls: null,
      playerBgHeight: 0,
      playerHeight: 0,
      isLandscape: false,
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
    if (NativeModules.UtilityModule) {
      NativeModules.UtilityModule.keepAwakeOn();
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
        // Switch back to portrait mode when the media is not fullscreen
        NativeModules.ScreenOrientation.lockOrientationPortrait();
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
    if (NativeModules.UtilityModule) {
      NativeModules.UtilityModule.keepAwakeOff();
    }
  }

  localUriForFileInfo = (fileInfo) => {
    if (!fileInfo) {
      return null;
    }
    return 'file:///' + fileInfo.download_path;
  }

  linkify = (text) => {
    let linkifiedContent = [];
    let lines = text.split(/\n/g);
    linkifiedContent = lines.map((line, i) => {
      let tokens = line.split(/\s/g);
      let lineContent = tokens.length === 0 ? '' : tokens.map((token, j) => {
        let hasSpace = j !== (tokens.length - 1);
        let space = hasSpace ? ' ' : '';

        if (token.match(/^(lbry|https?):\/\//g)) {
          return (
            <Link key={j}
                  style={filePageStyle.link}
                  href={token}
                  text={token}
                  effectOnTap={filePageStyle.linkTapped} />
          );
        } else {
          return token + space;
        }
      });

      lineContent.push("\n");
      return (<Text key={i}>{lineContent}</Text>);
    });

    return linkifiedContent;
  }

  checkOrientation = () => {
    if (this.state.fullscreenMode) {
      return;
    }

    const screenDimension = Dimensions.get('window');
    const screenWidth = screenDimension.width;
    const screenHeight = screenDimension.height;
    const isLandscape = screenWidth > screenHeight;
    this.setState({ isLandscape });

    if (isLandscape) {
      this.playerBackground.setNativeProps({ height: screenHeight - StyleSheet.flatten(uriBarStyle.uriContainer).height });
    } else if (this.state.playerBgHeight > 0) {
      this.playerBackground.setNativeProps({ height: this.state.playerBgHeight });
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
          {claim === null && !isResolvingUri &&
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
      const showActions = !this.state.fullscreenMode && !this.state.showImageViewer && !this.state.showWebView &&
        (completed || (fileInfo && !fileInfo.stopped && fileInfo.written_bytes < fileInfo.total_bytes));
      const channelClaimId =
        value && value.publisherSignature && value.publisherSignature.certificateId;

      const playerStyle = [filePageStyle.player,
        this.state.isLandscape ? filePageStyle.containedPlayerLandscape :
        (this.state.fullscreenMode ? filePageStyle.fullscreenPlayer : filePageStyle.containedPlayer)];
      const playerBgStyle = [filePageStyle.playerBackground, this.state.fullscreenMode ?
        filePageStyle.fullscreenPlayerBackground : filePageStyle.containedPlayerBackground];
      // at least 2MB (or the full download) before media can be loaded
      const canLoadMedia = fileInfo &&
        (fileInfo.written_bytes >= 2097152 || fileInfo.written_bytes == fileInfo.total_bytes); // 2MB = 1024*1024*2
      const canOpen = (mediaType === 'image' || mediaType === 'text') && completed;
      const isWebViewable = mediaType === 'text';
      const localFileUri = this.localUriForFileInfo(fileInfo);

      const openFile = () => {
        if (mediaType === 'image') {
          // use image viewer
          this.setState({
            imageUrls: [{
              url: localFileUri
            }],
            showImageViewer: true
          });
        }
        if (isWebViewable) {
          // show webview
          this.setState({
            showWebView: true
          });
        }
      }

      innerContent = (
        <View style={filePageStyle.pageContainer}>
          {this.state.showWebView && isWebViewable && <WebView source={{ uri: localFileUri }}
                                                               style={filePageStyle.viewer} />}

          {this.state.showImageViewer && <ImageViewer style={StyleSheet.flatten(filePageStyle.viewer)}
                                                      imageUrls={this.state.imageUrls}
                                                      renderIndicator={() => null} />}

          {!this.state.showWebView && (
            <View style={this.state.fullscreenMode ? filePageStyle.innerPageContainerFsMode : filePageStyle.innerPageContainer}
                  onLayout={this.checkOrientation}>
              <View style={filePageStyle.mediaContainer}>
                {(canOpen || (!fileInfo || (isPlayable && !canLoadMedia))) &&
                  <FileItemMedia style={filePageStyle.thumbnail} title={title} thumbnail={metadata.thumbnail} />}
                {(canOpen || (isPlayable && !this.state.mediaLoaded)) && <ActivityIndicator size="large" color={Colors.LbryGreen} style={filePageStyle.loading} />}
                {((isPlayable && !completed && !canLoadMedia) || !completed || canOpen) &&
                  <FileDownloadButton uri={uri}
                                      style={filePageStyle.downloadButton}
                                      openFile={openFile}
                                      isPlayable={isPlayable}
                                      onPlay={() => this.setState({ autoPlayMedia: true })} />}
                {!fileInfo && <FilePrice uri={uri} style={filePageStyle.filePriceContainer} textStyle={filePageStyle.filePriceText} />}
              </View>
              {canLoadMedia && <View style={playerBgStyle} ref={(ref) => { this.playerBackground = ref; }}
                                     onLayout={(evt) => {
                                       if (!this.state.playerBgHeight) {
                                         this.setState({ playerBgHeight: evt.nativeEvent.layout.height });
                                       }
                                     }} />}
              {canLoadMedia && <MediaPlayer fileInfo={fileInfo}
                                            ref={(ref) => { this.player = ref; }}
                                            uri={uri}
                                            style={playerStyle}
                                            autoPlay={this.state.autoPlayMedia}
                                            onFullscreenToggled={this.handleFullscreenToggle}
                                            onMediaLoaded={() => { this.setState({ mediaLoaded: true }); }}
                                            onLayout={(evt) => {
                                              if (!this.state.playerHeight) {
                                                this.setState({ playerHeight: evt.nativeEvent.layout.height });
                                              }
                                            }}
                                            />}

              { showActions &&
              <View style={filePageStyle.actions}>
                {completed && <Button color="red" title="Delete" onPress={this.onDeletePressed} />}
                {!completed && fileInfo && !fileInfo.stopped && fileInfo.written_bytes < fileInfo.total_bytes &&
                  <Button color="red" title="Stop Download" onPress={this.onStopDownloadPressed} />
                }
              </View>}
              <ScrollView style={showActions ? filePageStyle.scrollContainerActions : filePageStyle.scrollContainer}>
                <Text style={filePageStyle.title} selectable={true}>{title}</Text>
                {channelName && <Link style={filePageStyle.channelName}
                                      selectable={true}
                                      text={channelName}
                                      onPress={() => {
                                        const channelUri = normalizeURI(channelName);
                                        navigation.navigate({ routeName: 'File', key: channelUri, params: { uri: channelUri }});
                                      }} />}
                {description && <Text style={filePageStyle.description} selectable={true}>{this.linkify(description)}</Text>}
              </ScrollView>
            </View>
          )}

          {!this.state.fullscreenMode && <UriBar value={uri} navigation={navigation} />}
        </View>
      );
    }

    return innerContent;
  }
}

export default FilePage;
