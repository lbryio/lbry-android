import React from 'react';
import { Lbry, normalizeURI } from 'lbry-redux';
import { Lbryio } from 'lbryinc';
import {
  ActivityIndicator,
  Alert,
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
import Button from '../../component/button';
import Colors from '../../styles/colors';
import ChannelPage from '../channel';
import FileDownloadButton from '../../component/fileDownloadButton';
import FileItemMedia from '../../component/fileItemMedia';
import FilePrice from '../../component/filePrice';
import FloatingWalletBalance from '../../component/floatingWalletBalance';
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

  startTime = null;

  constructor(props) {
    super(props);
    this.state = {
      fileViewLogged: false,
      mediaLoaded: false,
      autoPlayMedia: false,
      downloadButtonShown: false,
      downloadPressed: false,
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

  componentDidUpdate(prevProps) {
    this.fetchFileInfo(this.props);
    const { isResolvingUri, resolveUri, claim, navigation } = this.props;
    const { uri } = navigation.state.params;

    if (!isResolvingUri && claim === undefined && uri) {
      resolveUri(uri);
    }

    const prevFileInfo = prevProps.fileInfo;
    const { fileInfo, contentType } = this.props;
    if (!prevFileInfo && fileInfo) {
      // started downloading
      const mediaType = Lbry.getMediaType(contentType);
      const isPlayable = mediaType === 'video' || mediaType === 'audio';
      // If the media is playable, file/view will be done in onPlaybackStarted
      if (!isPlayable && !this.state.fileViewLogged) {
        this.logFileView(uri, fileInfo);
      }
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
        if (NativeModules.UtilityModule) {
          // hide the navigation bar (on devices that use have soft navigation bar)
          NativeModules.UtilityModule.hideNavigationBar();
        }
      } else {
        // Switch back to portrait mode when the media is not fullscreen
        NativeModules.ScreenOrientation.lockOrientationPortrait();
        if (NativeModules.UtilityModule) {
          // hide the navigation bar (on devices that use have soft navigation bar)
          NativeModules.UtilityModule.showNavigationBar();
        }
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
        { text: 'Yes', onPress: () => {
          deleteFile(fileInfo.outpoint, true);
          this.setState({ downloadPressed: false, fileViewLogged: false, mediaLoaded: false });
        }}
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
        { text: 'Yes', onPress: () => {
          stopDownload(navigation.state.params.uri, fileInfo);
          this.setState({ downloadPressed: false, fileViewLogged: false, mediaLoaded: false });
        } }
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
      const utility = NativeModules.UtilityModule;
      utility.keepAwakeOff();
      utility.showNavigationBar();
    }
    if (window.currentMediaInfo) {
      window.currentMediaInfo = null;
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

  onMediaLoaded = (title, channelName) => {
    this.setState({ mediaLoaded: true });
    window.currentMediaInfo = {
      title: title,
      channel: channelName
    };
  }

  onPlaybackStarted = () => {
    let timeToStartMillis, timeToStart;
    if (this.startTime) {
      timeToStartMillis = Date.now() - this.startTime;
      timeToStart = Math.ceil(timeToStartMillis / 1000);
      this.startTime = null;
    }

    const { fileInfo, navigation } = this.props;
    const { uri } = navigation.state.params;
    this.logFileView(uri, fileInfo, timeToStartMillis);

    let payload = { 'Uri': uri };
    if (!isNaN(timeToStart)) {
      payload['Time to Start (seconds)'] = timeToStart;
      payload['Time to Start (ms)'] = timeToStartMillis;
    }
    NativeModules.Mixpanel.track('Play', payload);
  }

  logFileView = (uri, fileInfo, timeToStart) => {
    const { outpoint, claim_id: claimId } = fileInfo;
    const params = {
      uri,
      outpoint,
      claim_id: claimId
    };
    if (!isNaN(timeToStart)) {
      params.time_to_start = timeToStart;
    }

    Lbryio.call('file', 'view', params).catch(() => {});
    this.setState({ fileViewLogged: true });
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
                {((!this.state.downloadButtonShown || this.state.downloadPressed) && !this.state.mediaLoaded) &&
                    <ActivityIndicator size="large" color={Colors.LbryGreen} style={filePageStyle.loading} />}
                {((isPlayable && !completed && !canLoadMedia) || !completed || canOpen) && (!this.state.downloadPressed) &&
                  <FileDownloadButton uri={uri}
                                      style={filePageStyle.downloadButton}
                                      openFile={openFile}
                                      isPlayable={isPlayable}
                                      onPlay={() => {
                                        this.startTime = Date.now();
                                        this.setState({ downloadPressed: true, autoPlayMedia: true });
                                      }}
                                      onButtonLayout={() => this.setState({ downloadButtonShown: true })} />}
                {!fileInfo && <FilePrice uri={uri} style={filePageStyle.filePriceContainer} textStyle={filePageStyle.filePriceText} />}
              </View>
              {canLoadMedia && fileInfo && <View style={playerBgStyle}
                                                 ref={(ref) => { this.playerBackground = ref; }}
                                                 onLayout={(evt) => {
                                                  if (!this.state.playerBgHeight) {
                                                    this.setState({ playerBgHeight: evt.nativeEvent.layout.height });
                                                  }
                                                }} />}
              {canLoadMedia && fileInfo && <MediaPlayer
                                             fileInfo={fileInfo}
                                             ref={(ref) => { this.player = ref; }}
                                             uri={uri}
                                             style={playerStyle}
                                             autoPlay={this.state.autoPlayMedia}
                                             onFullscreenToggled={this.handleFullscreenToggle}
                                             onLayout={(evt) => {
                                               if (!this.state.playerHeight) {
                                                 this.setState({ playerHeight: evt.nativeEvent.layout.height });
                                               }
                                             }}
                                             onMediaLoaded={() => this.onMediaLoaded(title, channelName)}
                                             onPlaybackStarted={this.onPlaybackStarted}
                                            />}

              { showActions &&
              <View style={filePageStyle.actions}>
                {completed && <Button style={filePageStyle.actionButton}
                                      theme={"light"}
                                      icon={"trash"}
                                      text={"Delete"}
                                      onPress={this.onDeletePressed} />}
                {!completed && fileInfo && !fileInfo.stopped && fileInfo.written_bytes < fileInfo.total_bytes &&
                  <Button style={filePageStyle.actionButton}
                          theme={"light"}
                          text={"Stop Download"}
                          onPress={this.onStopDownloadPressed} />
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

                {description && description.length > 0 && <View style={filePageStyle.divider} />}

                {description && <Text style={filePageStyle.description} selectable={true}>{this.linkify(description)}</Text>}
              </ScrollView>
            </View>
          )}
          {!this.state.fullscreenMode && <FloatingWalletBalance navigation={navigation} />}
          {!this.state.fullscreenMode && <UriBar value={uri} navigation={navigation} />}
        </View>
      );
    }

    return innerContent;
  }
}

export default FilePage;
