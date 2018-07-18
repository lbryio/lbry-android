import React from 'react';
import { Lbry } from 'lbry-redux';
import {
  NativeModules,
  PanResponder,
  Text,
  View,
  ScrollView,
  TouchableOpacity
} from 'react-native';
import Video from 'react-native-video';
import Icon from 'react-native-vector-icons/FontAwesome';
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
      rate: 1,
      volume: 1,
      muted: false,
      resizeMode: 'stretch',
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
      this.setState({ resizeMode: this.state.fullscreenMode ? 'contain' : 'stretch' });
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
    const offset = this.getTrackingOffset();
    if (val < offset) {
      val = offset;
    } else if (val >= (offset + this.seekerWidth)) {
      return offset + this.seekerWidth;
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
    if (this.state.fullscreenMode) {
      return this.getTrackingOffset() + (this.seekerWidth * this.getCurrentTimePercentage());
    }
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

  }

  componentWillUnmount() {
    this.clearControlsTimeout();
    this.setState({ paused: true, fullscreenMode: false });
    const { onFullscreenToggled } = this.props;
    if (onFullscreenToggled) {
      onFullscreenToggled(false);
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

  render() {
    const { backgroundPlayEnabled, fileInfo, thumbnail, onLayout, style } = this.props;
    const flexCompleted = this.getCurrentTimePercentage() * 100;
    const flexRemaining = (1 - this.getCurrentTimePercentage()) * 100;
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
        <Video source={{ uri: 'file:///' + fileInfo.download_path }}
               ref={(ref: Video) => { this.video = ref }}
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
            <View style={[mediaPlayerStyle.innerProgressCompleted, { flex: flexCompleted }]} />
            <View style={[mediaPlayerStyle.innerProgressRemaining, { flex: flexRemaining }]} />
          </View>
        </View>}

        {this.state.areControlsVisible &&
        <View style={[mediaPlayerStyle.seekerHandle, { left: this.state.seekerPosition }]} { ...this.seekResponder.panHandlers }>
          <View style={this.state.seeking ? mediaPlayerStyle.bigSeekerCircle : mediaPlayerStyle.seekerCircle} />
        </View>}
      </View>
    );
  }
}

export default MediaPlayer;
