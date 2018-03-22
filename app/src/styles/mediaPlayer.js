import { StyleSheet, Dimensions } from 'react-native';

const screenDimension = Dimensions.get('window'); 
const screenWidth = screenDimension.width;

const mediaPlayerStyle = StyleSheet.create({
  player: {
    flex: 1
  },
  container: {
  },
  progress: {
    flex: 1,
    flexDirection: 'row',
    overflow: 'hidden',
  },
  innerProgressCompleted: {
    height: 4,
    backgroundColor: '#40c0a9',
  },
  innerProgressRemaining: {
    height: 4,
    backgroundColor: '#2c2c2c',
  },
  trackingControls: {
    height: 3,
    width: '100%',
    position: 'absolute',
    bottom: 0,
    left: 0,
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
    top: 16,
    left: 8,
    height: 12,
    width: 12,
    backgroundColor: '#40c0a9'
  },
  seekerHandle: {
    position: 'absolute',
    height: 36,
    width: 36,
    bottom: -12,
    marginLeft: -8
  },
  bigSeekerCircle: {
    borderRadius: 24,
    position: 'relative',
    top: 10,
    left: 8,
    height: 24,
    width: 24,
    backgroundColor: '#40c0a9'
  }
});

export default mediaPlayerStyle;
