import { StyleSheet, Dimensions } from 'react-native';
import Colors from './colors';

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
    height: 220
  },
  emptyClaimText: {
    fontFamily: 'Metropolis-Regular',
    textAlign: 'center',
    fontSize: 20,
    marginLeft: 16,
    marginRight: 16
  },
  scrollContainer: {
    flex: 1,
    marginTop: -16,
    marginBottom: -4,
    paddingTop: 10
  },
  scrollContainerActions: {
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
    color: Colors.ChannelGrey
  },
  description: {
    fontFamily: 'Metropolis-Regular',
    fontSize: 16,
    marginLeft: 20,
    marginRight: 20,
    marginBottom: 40,
    color: Colors.DescriptionGrey
  },
  thumbnail: {
    width: screenWidth,
    height: 204,
    justifyContent: 'center',
    alignItems: 'center'
  },
  downloadButton: {
    position: 'absolute',
    top: '40%'
  },
  player: {
    position: 'absolute',
    left: 0,
    top: 0,
    zIndex: 101
  },
  containedPlayer: {
    width: '100%',
    height: 220,
  },
  fullscreenPlayer: {
    width: '100%',
    height: '100%',
    right: 0,
    bottom: 0
  },
  playerBackground: {
    position: 'absolute',
    left: 0,
    top: 0,
    zIndex: 100,
    backgroundColor: Colors.Black
  },
  containedPlayerBackground: {
    width: '100%',
    height: 206,
  },
  fullscreenPlayerBackground: {
    width: '100%',
    height: '100%',
    right: 0,
    bottom: 0
  },
  filePriceContainer: {
    backgroundColor: '#61fcd8',
    justifyContent: 'center',
    position: 'absolute',
    right: 16,
    top: 16,
    width: 56,
    height: 24,
    borderRadius: 4 
  },
  filePriceText: {
    fontFamily: 'Metropolis-Bold',
    fontSize: 12,
    textAlign: 'center',
    color: '#0c604b'
  },
  actions: {
    paddingLeft: 16,
    paddingRight: 16,
    paddingTop: 16,
    paddingBottom: 8,
    marginTop: -14,
    width: '50%',
  },
  deleteButton: {
    backgroundColor: Colors.Red,
    width: 80
  },
  loading: {
    position: 'absolute',
    top: '40%'
  },
  uriContainer: {
    padding: 8,
    backgroundColor: Colors.VeryLightGrey,
    alignSelf: 'flex-end',
    width: '100%'
  },
  uriText: {
    backgroundColor: Colors.White,
    borderWidth: 1,
    borderColor: Colors.LightGrey,
    padding: 8,
    borderRadius: 4,
    fontFamily: 'Metropolis-Regular',
    fontSize: 16,
    width: '100%'
  }
});

export default filePageStyle;
