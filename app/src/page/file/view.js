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
  TouchableWithoutFeedback,
  View,
  WebView
} from 'react-native';
import { navigateToUri } from 'utils/helper';
import Icon from 'react-native-vector-icons/FontAwesome5';
import ImageViewer from 'react-native-image-zoom-viewer';
import Button from 'component/button';
import Colors from 'styles/colors';
import ChannelPage from 'page/channel';
import DateTime from 'component/dateTime';
import FileDownloadButton from 'component/fileDownloadButton';
import FileItemMedia from 'component/fileItemMedia';
import FilePrice from 'component/filePrice';
import FloatingWalletBalance from 'component/floatingWalletBalance';
import Link from 'component/link';
import MediaPlayer from 'component/mediaPlayer';
import RelatedContent from 'component/relatedContent';
import SubscribeButton from 'component/subscribeButton';
import SubscribeNotificationButton from 'component/subscribeNotificationButton';
import UriBar from 'component/uriBar';
import Video from 'react-native-video';
import filePageStyle from 'styles/filePage';
import uriBarStyle from 'styles/uriBar';

class FilePage extends React.PureComponent {
  static navigationOptions = {
    title: ''
  };

  tipAmountInput = null;

  playerBackground = null;

  scrollView = null;

  startTime = null;

  constructor(props) {
    super(props);
    this.state = {
      autoPlayMedia: false,
      autoDownloadStarted: false,
      downloadButtonShown: false,
      downloadPressed: false,
      fileViewLogged: false,
      fullscreenMode: false,
      imageUrls: null,
      isLandscape: false,
      mediaLoaded: false,
      pageSuspended: false,
      relatedContentY: 0,
      showDescription: false,
      showImageViewer: false,
      showWebView: false,
      showTipView: false,
      playerBgHeight: 0,
      playerHeight: 0,
      tipAmount: null,
      uri: null,
      uriVars: null,
      stopDownloadConfirmed: false
    };
  }

  componentDidMount() {
    StatusBar.setHidden(false);

    const { isResolvingUri, resolveUri, navigation } = this.props;
    const { uri, uriVars } = navigation.state.params;
    this.setState({ uri, uriVars });

    if (!isResolvingUri) resolveUri(uri);

    this.fetchFileInfo(this.props);
    this.fetchCostInfo(this.props);

    if (NativeModules.Firebase) {
      NativeModules.Firebase.track('open_file_page', { uri: uri });
    }
    if (NativeModules.UtilityModule) {
      NativeModules.UtilityModule.keepAwakeOn();
    }
  }

  componentDidUpdate(prevProps) {
    this.fetchFileInfo(this.props);
    const { claim, contentType, fileInfo, isResolvingUri, resolveUri, navigation } = this.props;
    const { uri } = this.state;
    if (!isResolvingUri && claim === undefined && uri) {
      resolveUri(uri);
    }

    // Returned to the page. If mediaLoaded, and currentMediaInfo is different, update
    if (this.state.mediaLoaded && window.currentMediaInfo && window.currentMediaInfo.uri !== this.state.uri) {
      const { metadata } = this.props;
      window.currentMediaInfo = {
        channel: claim ? claim.channel_name : null,
        title: metadata ? metadata.title : claim.name,
        uri: this.state.uri
      };
    }

    const prevFileInfo = prevProps.fileInfo;
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
          this.setState({
            downloadPressed: false,
            fileViewLogged: false,
            mediaLoaded: false,
            stopDownloadConfirmed: false
          });
        }}
      ],
      { cancelable: true }
    );
  }

  onStopDownloadPressed = () => {
    const { fileInfo, navigation, notify, stopDownload } = this.props;

    Alert.alert(
      'Stop download',
      'Are you sure you want to stop downloading this file?',
      [
        { text: 'No' },
        { text: 'Yes', onPress: () => {
          stopDownload(navigation.state.params.uri, fileInfo);
          this.setState({
            downloadPressed: false,
            fileViewLogged: false,
            mediaLoaded: false,
            stopDownloadConfirmed: true
          });

          // there can be a bit of lag between the user pressing Yes and the UI being updated
          // after the file_set_status and file_delete operations, so let the user know
          notify({
            message: 'The download will stop momentarily. You do not need to wait to discover something else.',
          });
        }}
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
    window.player = null;
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

    if (!this.playerBackground) {
      return;
    }

    if (isLandscape) {
      this.playerBackground.setNativeProps({ height: screenHeight - StyleSheet.flatten(uriBarStyle.uriContainer).height });
    } else if (this.state.playerBgHeight > 0) {
      this.playerBackground.setNativeProps({ height: this.state.playerBgHeight });
    }
  }

  onMediaLoaded = (channelName, title, uri) => {
    this.setState({ mediaLoaded: true });
    window.currentMediaInfo = { channel: channelName, title, uri };
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

    let payload = { 'uri': uri };
    if (!isNaN(timeToStart)) {
      payload['time_to_start_seconds'] = timeToStart;
      payload['time_to_start_ms'] = timeToStartMillis;
    }
    NativeModules.Firebase.track('play', payload);
  }

  onPlaybackFinished = () => {
    if (this.scrollView && this.state.relatedContentY) {
        this.scrollView.scrollTo({ x: 0, y: this.state.relatedContentY, animated: true});
    }
  }

  setRelatedContentPosition = (evt) =>  {
    if (!this.state.relatedContentY) {
      this.setState({ relatedContentY: evt.nativeEvent.layout.y });
    }
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

  handleSendTip = () => {
    const { claim, balance, navigation, notify, sendTip } = this.props;
    const { uri } = navigation.state.params;
    const { tipAmount } = this.state;

    if (tipAmount > balance) {
      notify({
        message: 'Insufficient credits',
      });
      return;
    }

    sendTip(tipAmount, claim.claim_id, uri, () => { this.setState({ tipAmount: 0, showTipView: false }) });
  }

  startDownloadFailed = () => {
    this.startTime = null;
    setTimeout(() => {
      this.setState({ downloadPressed: false, fileViewLogged: false, mediaLoaded: false });
    }, 500);
  }

  renderTags = (tags) => {
    return tags.map((tag, i) => (
      <Text style={filePageStyle.tagItem} key={`${tag}-${i}`}>{tag}</Text>
    ));
  }

  render() {
    const {
      claim,
      channelUri,
      fileInfo,
      metadata,
      contentType,
      tab,
      rewardedContentClaimIds,
      isResolvingUri,
      blackListedOutpoints,
      navigation,
      position,
      purchaseUri,
      thumbnail,
      title
    } = this.props;
    const { uri, autoplay } = navigation.state.params;

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
      let isClaimBlackListed = false;

      if (blackListedOutpoints) {
        for (let i = 0; i < blackListedOutpoints.length; i += 1) {
          const outpoint = blackListedOutpoints[i];
          if (outpoint.txid === claim.txid && outpoint.nout === claim.nout) {
            isClaimBlackListed = true;
            break;
          }
        }
      }

      if (isClaimBlackListed) {
        innerContent = (
          <View style={filePageStyle.pageContainer}>
            <View style={filePageStyle.dmcaContainer}>
              <Text style={filePageStyle.dmcaText}>
                In response to a complaint we received under the US Digital Millennium Copyright Act, we have blocked access to this content from our applications.
              </Text>
              <Link style={filePageStyle.dmcaLink} href="https://lbry.com/faq/dmca" text="Read More" />
            </View>
            <UriBar value={uri} navigation={navigation} />
          </View>
        );
      } else {

        let tags = [];
        if (claim && claim.value && claim.value.tags) {
          tags = claim.value.tags;
        }

        const completed = fileInfo && fileInfo.completed;
        const isRewardContent = rewardedContentClaimIds.includes(claim.claim_id);
        const description = metadata.description ? metadata.description : null;
        const mediaType = Lbry.getMediaType(contentType);
        const isPlayable = mediaType === 'video' || mediaType === 'audio';
        const { height, channel_name: channelName, value } = claim;
        const showActions = !this.state.fullscreenMode && !this.state.showImageViewer && !this.state.showWebView;
        const showFileActions = (completed || (fileInfo && !fileInfo.stopped && fileInfo.written_bytes < fileInfo.total_bytes));
        const channelClaimId = claim && claim.signing_channel && claim.signing_channel.claim_id;
        const canSendTip = this.state.tipAmount > 0;
        const fullChannelUri = channelClaimId && channelClaimId.trim().length > 0 ? `${channelName}#${channelClaimId}` : channelName;

        const playerStyle = [filePageStyle.player,
          this.state.isLandscape ? filePageStyle.containedPlayerLandscape :
          (this.state.fullscreenMode ? filePageStyle.fullscreenPlayer : filePageStyle.containedPlayer)];
        const playerBgStyle = [filePageStyle.playerBackground, filePageStyle.containedPlayerBackground];
        const fsPlayerBgStyle = [filePageStyle.playerBackground, filePageStyle.fullscreenPlayerBackground];
        // at least 2MB (or the full download) before media can be loaded
        const canLoadMedia = fileInfo &&
          (fileInfo.written_bytes >= 2097152 || fileInfo.written_bytes == fileInfo.total_bytes); // 2MB = 1024*1024*2
        const isViewable = (mediaType === 'image' || mediaType === 'text');
        const isWebViewable = mediaType === 'text';
        const canOpen =  isViewable && completed;
        const localFileUri = this.localUriForFileInfo(fileInfo);

        const openFile = () => {
          if (mediaType === 'image') {
            // use image viewer
            if (!this.state.showImageViewer) {
              this.setState({
                imageUrls: [{
                  url: localFileUri
                }],
                showImageViewer: true
              });
            }
          }
          if (isWebViewable) {
            // show webview
            if (!this.state.showWebView) {
              this.setState({
                showWebView: true
              });
            }
          }
        }

        if (fileInfo && !this.state.autoDownloadStarted && this.state.uriVars && 'true' === this.state.uriVars.download) {
          this.setState({ autoDownloadStarted: true }, () => {
            purchaseUri(uri, this.startDownloadFailed);
          });
        }

        if (this.state.downloadPressed && canOpen) {
          // automatically open a web viewable or image file after the download button is pressed
          openFile();
        }

        innerContent = (
          <View style={filePageStyle.pageContainer}>
            {!this.state.fullscreenMode && <UriBar value={uri} navigation={navigation} />}
            {this.state.showWebView && isWebViewable && <WebView source={{ uri: localFileUri }}
                                                                 style={filePageStyle.viewer} />}

            {this.state.showImageViewer && <ImageViewer style={StyleSheet.flatten(filePageStyle.viewer)}
                                                        imageUrls={this.state.imageUrls}
                                                        renderIndicator={() => null} />}

            {!this.state.showWebView && (
              <View style={this.state.fullscreenMode ? filePageStyle.innerPageContainerFsMode : filePageStyle.innerPageContainer}
                    onLayout={this.checkOrientation}>
                <View style={filePageStyle.mediaContainer}>
                  {((canOpen || (!fileInfo || (isPlayable && !canLoadMedia))) || (!canOpen && fileInfo)) &&
                    <FileItemMedia style={filePageStyle.thumbnail} title={title} thumbnail={thumbnail} />}
                  {((!this.state.downloadButtonShown || this.state.downloadPressed) && !this.state.mediaLoaded) &&
                      <ActivityIndicator size="large" color={Colors.LbryGreen} style={filePageStyle.loading} />}
                  {((isPlayable && !completed && !canLoadMedia) || !completed || canOpen) && (!this.state.downloadPressed) &&
                    <FileDownloadButton uri={uri}
                                        style={filePageStyle.downloadButton}
                                        openFile={openFile}
                                        isPlayable={isPlayable}
                                        isViewable={isViewable}
                                        onPlay={() => {
                                          this.startTime = Date.now();
                                          this.setState({ downloadPressed: true, autoPlayMedia: true, stopDownloadConfirmed: false });
                                        }}
                                        onView={() => this.setState({ downloadPressed: true })}
                                        onButtonLayout={() => this.setState({ downloadButtonShown: true })}
                                        onStartDownloadFailed={this.startDownloadFailed} />}
                  {!fileInfo && <FilePrice uri={uri} style={filePageStyle.filePriceContainer} textStyle={filePageStyle.filePriceText} />}
                </View>
                {(canLoadMedia && fileInfo && isPlayable) &&
                  <View style={playerBgStyle}
                    ref={(ref) => { this.playerBackground = ref; }}
                    onLayout={(evt) => {
                     if (!this.state.playerBgHeight) {
                       this.setState({ playerBgHeight: evt.nativeEvent.layout.height });
                     }
                   }} />}
                {(canLoadMedia && fileInfo && isPlayable && this.state.fullscreenMode) && <View style={fsPlayerBgStyle} />}
                {(canLoadMedia && fileInfo && isPlayable) &&
                  <MediaPlayer
                    fileInfo={fileInfo}
                    assignPlayer={(ref) => { this.player = ref; }}
                    uri={uri}
                    style={playerStyle}
                    autoPlay={autoplay || this.state.autoPlayMedia}
                    onFullscreenToggled={this.handleFullscreenToggle}
                    onLayout={(evt) => {
                      if (!this.state.playerHeight) {
                        this.setState({ playerHeight: evt.nativeEvent.layout.height });
                      }
                    }}
                    onMediaLoaded={() => this.onMediaLoaded(channelName, title, uri)}
                    onPlaybackStarted={this.onPlaybackStarted}
                    onPlaybackFinished={this.onPlaybackFinished}
                    thumbnail={thumbnail}
                    position={position}
                   />}

                {(showActions && showFileActions) &&
                <View style={filePageStyle.actions}>
                  {showFileActions &&
                    <View style={filePageStyle.fileActions}>
                      {completed && <Button style={filePageStyle.actionButton}
                                            theme={"light"}
                                            icon={"trash"}
                                            text={"Delete"}
                                            onPress={this.onDeletePressed} />}
                      {!completed && fileInfo && !fileInfo.stopped &&
                       fileInfo.written_bytes < fileInfo.total_bytes &&
                       !this.state.stopDownloadConfirmed &&
                        <Button style={filePageStyle.actionButton}
                                icon={"stop"}
                                theme={"light"}
                                text={"Stop Download"}
                                onPress={this.onStopDownloadPressed} />
                      }
                    </View>}
                </View>}
                <ScrollView
                  style={showActions ? filePageStyle.scrollContainerActions : filePageStyle.scrollContainer}
                  contentContainerstyle={showActions ? null : filePageStyle.scrollContent}
                  ref={(ref) => { this.scrollView = ref; }}>
                  <View style={filePageStyle.titleRow}>
                    <Text style={filePageStyle.title} selectable={true}>{title}</Text>
                    <TouchableWithoutFeedback style={filePageStyle.descriptionToggle}
                      onPress={() => this.setState({ showDescription: !this.state.showDescription })}>
                      <Icon name={this.state.showDescription ? "caret-up" : "caret-down"} size={24} />
                    </TouchableWithoutFeedback>
                  </View>
                  {channelName &&
                    <View style={filePageStyle.channelRow}>
                      <View style={filePageStyle.publishInfo}>
                        <Link style={filePageStyle.channelName}
                              selectable={true}
                              text={channelName}
                              numberOfLines={1}
                              ellipsizeMode={"tail"}
                              onPress={() => {
                                navigateToUri(navigation, normalizeURI(fullChannelUri));
                              }} />
                        <DateTime
                          style={filePageStyle.publishDate}
                          textStyle={filePageStyle.publishDateText}
                          uri={uri}
                          formatOptions={{ day: 'numeric', month: 'long', year: 'numeric' }}
                          show={DateTime.SHOW_DATE} />
                      </View>
                      <View style={filePageStyle.subscriptionRow}>
                        <Button style={[filePageStyle.actionButton, filePageStyle.tipButton]}
                            theme={"light"}
                            icon={"gift"}
                            onPress={() => this.setState({ showTipView: true })} />
                        <SubscribeButton
                          style={filePageStyle.actionButton}
                          uri={fullChannelUri}
                          name={channelName} />
                        <SubscribeNotificationButton
                          style={[filePageStyle.actionButton, filePageStyle.bellButton]}
                          uri={fullChannelUri}
                          name={channelName} />
                      </View>
                    </View>
                  }

                  {this.state.showTipView && <View style={filePageStyle.divider} />}
                  {this.state.showTipView &&
                  <View style={filePageStyle.tipCard}>
                    <View style={filePageStyle.row}>
                      <View style={filePageStyle.amountRow}>
                        <TextInput ref={ref => this.tipAmountInput = ref}
                                   onChangeText={value => this.setState({tipAmount: value})}
                                   keyboardType={'numeric'}
                                   value={this.state.tipAmount}
                                   style={[filePageStyle.input, filePageStyle.tipAmountInput]} />
                        <Text style={[filePageStyle.text, filePageStyle.currency]}>LBC</Text>
                      </View>
                      <Link style={[filePageStyle.link, filePageStyle.cancelTipLink]} text={'Cancel'} onPress={() => this.setState({ showTipView: false })} />
                      <Button text={'Send a tip'}
                              style={[filePageStyle.button, filePageStyle.sendButton]}
                              disabled={!canSendTip}
                              onPress={this.handleSendTip} />
                    </View>
                  </View>}

                  {(this.state.showDescription && description && description.length > 0) && <View style={filePageStyle.divider} />}
                  {(this.state.showDescription && description) && (
                    <View>
                      <Text style={filePageStyle.description} selectable={true}>{this.linkify(description)}</Text>
                      {tags && tags.length > 0 && (
                        <View style={filePageStyle.tagContainer}>
                          <Text style={filePageStyle.tagTitle}>Tags</Text>
                          <View style={filePageStyle.tagList}>{this.renderTags(tags)}</View>
                        </View>
                      )}
                    </View>)}

                  <View onLayout={this.setRelatedContentPosition} />
                  <RelatedContent navigation={navigation} uri={uri} />
                </ScrollView>
              </View>
            )}
            {!this.state.fullscreenMode && <FloatingWalletBalance navigation={navigation} />}
          </View>
        );
      }
    }

    return innerContent;
  }
}

export default FilePage;
