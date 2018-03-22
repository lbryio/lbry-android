import { StyleSheet, Dimensions } from 'react-native';

const screenDimension = Dimensions.get('window'); 
const screenWidth = screenDimension.width;

const filePageStyle = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
  },
  pageContainer: {
    flex: 1
  },
  mediaContainer: {
    alignItems: 'center',
    width: screenWidth,
    height: 220,
  },
  emptyClaimText: {
    fontFamily: 'Metropolis-Regular',
    textAlign: 'center',
    fontSize: 20,
    marginLeft: 16,
    marginRight: 16
  },
  scrollContainer: {
    flex: 1
  },
  title: {
    fontFamily: 'Metropolis-Bold',
    fontSize: 24,
    marginTop: 12,
    marginLeft: 20,
    marginRight: 20,
    marginBottom: 12
  },
  channelName: {
    fontFamily: 'Metropolis-SemiBold',
    fontSize: 20,
    marginLeft: 20,
    marginRight: 20,
    marginBottom: 20,
    color: '#9b9b9b'
  },
  description: {
    fontFamily: 'Metropolis-Regular',
    fontSize: 16,
    marginLeft: 20,
    marginRight: 20,
    marginBottom: 20,
    color: '#999999'
  },
  thumbnail: {
    width: screenWidth,
    height: 200
  },
  downloadButton: {
    position: 'absolute',
    top: '50%'
  },
  player: {
    flex: 1,
    width: '100%',
    height: '100%',
    marginBottom: 14
  },
  fullscreenMedia: {
    position: 'absolute',
    left: 0,
    top: 0,
    right: 0,
    bottom: 0,
    flex: 1,
    backgroundColor: '#000000',
    zIndex: 100
  }
});

export default filePageStyle;
