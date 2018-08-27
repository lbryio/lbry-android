import React from 'react';
import { Lbry } from 'lbry-redux';
import {
  DeviceEventEmitter,
  NativeModules,
  PanResponder,
  Text,
  View,
  ScrollView,
  TouchableOpacity
} from 'react-native';
import Video from 'react-native-video';
import Icon from 'react-native-vector-icons/FontAwesome5';
import FileItemMedia from '../fileItemMedia';
import mediaPlayerStyle from '../../styles/mediaPlayer';

class MediaPlayer extends React.PureComponent {
  static ControlsTimeout = 3000;

  seekResponder = null;

  seekerWidth = 0;

  trackingOffset = 0;

  tracking = null;

  video = null;

  constructor(props) {
    super(props);
    this.state = {
      encodedFilePath: null,
      rate: 1,
      volume: 1,
      muted: false,
      resizeMode: 'contain',
      duration: 0.0,
      currentTime: 0.0,
      paused: !props.autoPlay,
      fullscreenMode: false,
      areControlsVisible: true,
      controlsTimeout: -1,
      seekerOffset: 0,
      seekerPosition: 0,
      firstPlay: true,
      seekTimeout: -1
    };
  }

  formatTime(time) {
    let str = '';
    let minutes = 0, hours = 0, seconds = parseInt(time, 10);
    if (seconds > 60) {
      minutes = parseInt(seconds / 60, 10);
      seconds = seconds % 60;

      if (minutes > 60) {
        hours = parseInt(minutes / 60, 10);
        minutes = minutes % 60;
      }

      str = (hours > 0 ? this.pad(hours) + ':' : '') + this.pad(minutes) + ':' + this.pad(seconds);
    } else {
      str = '00:' + this.pad(seconds);
    }

    return str;
  }

  pad(value) {
    if (value < 10) {
      return '0' + String(value);
    }

    return value;
  }

  onLoad = (data) => {
    this.setState({
      duration: data.duration
    });
    if (this.props.onMediaLoaded) {
      this.props.onMediaLoaded();
    }
  }

  onProgress = (data) => {
    this.setState({ currentTime: data.currentTime });

    if (!this.state.seeking) {
      this.setSeekerPosition(this.calculateSeekerPosition());
    }

    if (this.state.firstPlay) {
      if (NativeModules.Mixpanel) {
        const { uri } = this.props;
        NativeModules.Mixpanel.track('Play', { Uri: uri });
      }
      this.setState({ firstPlay: false });
      this.hidePlayerControls();
    }
  }

  clearControlsTimeout = () => {
    if (this.state.controlsTimeout > -1) {
      clearTimeout(this.state.controlsTimeout)
    }
  }

  showPlayerControls = () => {
    this.clearControlsTimeout();
    if (!this.state.areControlsVisible) {
      this.setState({ areControlsVisible: true });
    }
    this.hidePlayerControls();
  }

  hidePlayerControls() {
    const player = this;
    let timeout = setTimeout(() => {
      player.setState({ areControlsVisible: false });
    }, MediaPlayer.ControlsTimeout);
    player.setState({ controlsTimeout: timeout });
  }

  togglePlay = () => {
    this.showPlayerControls();
    this.setState({ paused: !this.state.paused });
  }

  toggleFullscreenMode = () => {
    this.showPlayerControls();
    const { onFullscreenToggled } = this.props;
    this.setState({ fullscreenMode: !this.state.fullscreenMode }, () => {
      if (onFullscreenToggled) {
        onFullscreenToggled(this.state.fullscreenMode);
      }
    });
  }

  onEnd = () => {
    this.setState({ paused: true });
    this.video.seek(0);
  }

  setSeekerPosition(position = 0) {
    position = this.checkSeekerPosition(position);
    this.setState({ seekerPosition: position });
    if (!this.state.seeking) {
      this.setState({ seekerOffset: position });
    }
  }

  checkSeekerPosition(val = 0) {
    if (val < 0) {
      val = 0;
    } else if (val >= this.seekerWidth) {
      return this.seekerWidth;
    }

    return val;
  }

  seekTo(time = 0) {
    if (time > this.state.duration) {
      return;
    }
    this.video.seek(time);
    this.setState({ currentTime: time });
  }

  initSeeker() {
    this.seekResponder = PanResponder.create({
      onStartShouldSetPanResponder: (evt, gestureState) => true,
      onMoveShouldSetPanResponder: (evt, gestureState) => true,

      onPanResponderGrant: (evt, gestureState) => {
        this.clearControlsTimeout();
        if (this.state.seekTimeout > 0) {
          clearTimeout(this.state.seekTimeout);
        }
        this.setState({ seeking: true });
      },

      onPanResponderMove: (evt, gestureState) => {
        const position = this.state.seekerOffset + gestureState.dx;
        this.setSeekerPosition(position);
      },

      onPanResponderRelease: (evt, gestureState) => {
        const time = this.getCurrentTimeForSeekerPosition();
        if (time >= this.state.duration) {
          this.setState({ paused: true });
          this.onEnd();
        } else {
          this.seekTo(time);
          this.setState({ seekTimeout: setTimeout(() => { this.setState({ seeking: false }); }, 100) });
        }
        this.hidePlayerControls();
      }
    });
  }

  getTrackingOffset() {
    return this.state.fullscreenMode ? this.trackingOffset : 0;
  }

  getCurrentTimeForSeekerPosition() {
    return this.state.duration * (this.state.seekerPosition / this.seekerWidth);
  }

  calculateSeekerPosition() {
    return this.seekerWidth * this.getCurrentTimePercentage();
  }

  getCurrentTimePercentage() {
    if (this.state.currentTime > 0) {
      return parseFloat(this.state.currentTime) / parseFloat(this.state.duration);
    }
    return 0;
  };

  componentWillMount() {
    this.initSeeker();
  }

  componentDidMount() {
    this.setSeekerPosition(this.calculateSeekerPosition());
    DeviceEventEmitter.addListener('onBackgroundPlayPressed', this.play);
    DeviceEventEmitter.addListener('onBackgroundPausePressed', this.pause);
  }

  componentWillUnmount() {
    DeviceEventEmitter.removeListener('onBackgroundPlayPressed', this.play);
    DeviceEventEmitter.removeListener('onBackgroundPausePressed', this.pause);
    this.clearControlsTimeout();
    this.setState({ paused: true, fullscreenMode: false });
    const { onFullscreenToggled } = this.props;
    if (onFullscreenToggled) {
      onFullscreenToggled(false);
    }
  }

  play = () => {
    this.setState({ paused: false }, this.updateBackgroundMediaNotification);
  }

  pause = () => {
    this.setState({ paused: true }, this.updateBackgroundMediaNotification);
  }

  updateBackgroundMediaNotification = () => {
    const { backgroundPlayEnabled } = this.props;
    if (backgroundPlayEnabled) {
      if (NativeModules.BackgroundMedia && window.currentMediaInfo) {
        const { title, channel } = window.currentMediaInfo;
        NativeModules.BackgroundMedia.showPlaybackNotification(title, channel, null, this.state.paused);
      }
    }
  }

  renderPlayerControls() {
    if (this.state.areControlsVisible) {
      return (
        <View style={mediaPlayerStyle.playerControlsContainer}>
          <TouchableOpacity style={mediaPlayerStyle.playPauseButton}
            onPress={this.togglePlay}>
            {this.state.paused && <Icon name="play" size={32} color="#ffffff" />}
            {!this.state.paused && <Icon name="pause" size={32} color="#ffffff" />}
          </TouchableOpacity>

          <TouchableOpacity style={mediaPlayerStyle.toggleFullscreenButton} onPress={this.toggleFullscreenMode}>
            {this.state.fullscreenMode && <Icon name="compress" size={16} color="#ffffff" />}
            {!this.state.fullscreenMode && <Icon name="expand" size={16} color="#ffffff" />}
          </TouchableOpacity>

          <Text style={mediaPlayerStyle.elapsedDuration}>{this.formatTime(this.state.currentTime)}</Text>
          <Text style={mediaPlayerStyle.totalDuration}>{this.formatTime(this.state.duration)}</Text>
        </View>
      );
    }

    return null;
  }

  getEncodedDownloadPath = (fileInfo) => {
    if (this.state.encodedFilePath) {
      return this.state.encodedFilePath;
    }

    const { file_name: fileName } = fileInfo;
    const encodedFileName = encodeURIComponent(fileName).replace(/!/g, '%21');
    const encodedFilePath = fileInfo.download_path.replace(fileName, encodedFileName);
    this.setState({ encodedFilePath });
    return encodedFilePath;
  }

  render() {
    const { backgroundPlayEnabled, fileInfo, thumbnail, onLayout, style } = this.props;
    const completedWidth = this.getCurrentTimePercentage() * this.seekerWidth;
    const remainingWidth = this.seekerWidth - completedWidth;
    let styles = [this.state.fullscreenMode ? mediaPlayerStyle.fullscreenContainer : mediaPlayerStyle.container];
    if (style) {
      if (style.length) {
        styles = styles.concat(style);
      } else {
        styles.push(style);
      }
    }

    const trackingStyle = [mediaPlayerStyle.trackingControls, this.state.fullscreenMode ?
      mediaPlayerStyle.fullscreenTrackingControls : mediaPlayerStyle.containedTrackingControls];

    return (
      <View style={styles} onLayout={onLayout}>
        <Video source={{ uri: 'file:///' + this.getEncodedDownloadPath(fileInfo) }}
               ref={(ref: Video) => { this.video = ref; }}
               resizeMode={this.state.resizeMode}
               playInBackground={backgroundPlayEnabled}
               style={mediaPlayerStyle.player}
               rate={this.state.rate}
               volume={this.state.volume}
               paused={this.state.paused}
               onLoad={this.onLoad}
               onProgress={this.onProgress}
               onEnd={this.onEnd}
               />

        <TouchableOpacity style={mediaPlayerStyle.playerControls} onPress={this.showPlayerControls}>
          {this.renderPlayerControls()}
        </TouchableOpacity>

        {(!this.state.fullscreenMode || (this.state.fullscreenMode && this.state.areControlsVisible)) &&
        <View style={trackingStyle} onLayout={(evt) => {
              this.trackingOffset = evt.nativeEvent.layout.x;
              this.seekerWidth = evt.nativeEvent.layout.width;
            }}>
          <View style={mediaPlayerStyle.progress}>
            <View style={[mediaPlayerStyle.innerProgressCompleted, { width: completedWidth }]} />
            <View style={[mediaPlayerStyle.innerProgressRemaining, { width: remainingWidth }]} />
          </View>
        </View>}

        {this.state.areControlsVisible &&
        <View style={{ left: this.getTrackingOffset(), width: this.seekerWidth }}>
          <View style={[mediaPlayerStyle.seekerHandle,
                        (this.state.fullscreenMode ? mediaPlayerStyle.seekerHandleFs : mediaPlayerStyle.seekerHandleContained),
                        { left: this.state.seekerPosition }]} { ...this.seekResponder.panHandlers }>
            <View style={this.state.seeking ? mediaPlayerStyle.bigSeekerCircle : mediaPlayerStyle.seekerCircle} />
          </View>
        </View>}
      </View>
    );
  }
}

export default MediaPlayer;
