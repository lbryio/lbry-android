import { StyleSheet } from 'react-native';
import Colors from './colors';

const mediaPlayerStyle = StyleSheet.create({
  player: {
    flex: 1,
    width: '100%',
    height: '100%'
  },
  container: {
    flex: 1,
    paddingBottom: 16
  },
  fullscreenContainer: {
    flex: 1,
    justifyContent: 'center'
  },
  progress: {
    flex: 1,
    flexDirection: 'row',
    height: 3
  },
  innerProgressCompleted: {
    height: 4,
    backgroundColor: Colors.LbryGreen,
  },
  innerProgressRemaining: {
    height: 4,
    backgroundColor: '#2c2c2c',
  },
  trackingControls: {
    height: 3,
    position: 'absolute',
    bottom: 14
  },
  containedTrackingControls: {
    left: 0,
    width: '100%'
  },
  fullscreenTrackingControls: {
    alignSelf: 'center',
    width: '70%'
  },
  playerControls: {
    position: 'absolute',
    left: 0,
    top: 0,
    width: '100%',
    height: '100%',
  },
  playerControlsContainer: {
    backgroundColor: 'transparent',
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center'
  },
  playPauseButton: {
    position: 'absolute',
    width: 64,
    height: 64,
    alignItems: 'center',
    justifyContent: 'center'
  },
  toggleFullscreenButton: {
    position: 'absolute',
    width: 36,
    height: 36,
    alignItems: 'center',
    justifyContent: 'center',
    right: 0,
    bottom: 14,
  },
  elapsedDuration: {
    fontFamily: 'Metropolis-Regular',
    position: 'absolute',
    left: 8,
    bottom: 24,
    fontSize: 12,
    color: '#ffffff'
  },
  totalDuration: {
    fontFamily: 'Metropolis-Regular',
    position: 'absolute',
    right: 40,
    bottom: 24,
    fontSize: 12,
    color: '#ffffff'
  },
  seekerCircle: {
    borderRadius: 12,
    position: 'relative',
    top: 14,
    left: 15,
    height: 12,
    width: 12,
    backgroundColor: '#40c0a9'
  },
  seekerHandle: {
    backgroundColor: 'transparent',
    position: 'absolute',
    height: 36,
    width: 48,
    marginLeft: -18
  },
  seekerHandleContained: {
    bottom: -17
  },
  seekerHandleFs: {
    bottom: 0
  },
  bigSeekerCircle: {
    borderRadius: 24,
    position: 'relative',
    top: 8,
    left: 15,
    height: 24,
    width: 24,
    backgroundColor: '#40c0a9'
  }
});

export default mediaPlayerStyle;
