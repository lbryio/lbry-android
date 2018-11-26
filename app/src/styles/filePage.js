import { StyleSheet, Dimensions } from 'react-native';
import Colors from './colors';

const screenDimension = Dimensions.get('window');
const screenWidth = screenDimension.width;
const containedMediaHeight = ((screenWidth * 9) / 16); // 16:9 display ratio
const containedMediaHeightWithControls = containedMediaHeight + 17;

const filePageStyle = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
  },
  pageContainer: {
    flex: 1
  },
  divider: {
    backgroundColor: Colors.LighterGrey,
    width: '100%',
    height: 1,
    marginTop: 4,
    marginBottom: 20
  },
  innerPageContainer: {
    flex: 1,
    marginBottom: 60
  },
  innerPageContainerFsMode: {
    flex: 1,
    marginBottom: 0
  },
  mediaContainer: {
    alignItems: 'center',
    width: screenWidth,
    height: containedMediaHeightWithControls
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
    marginTop: -16
  },
  scrollContent: {
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
    color: Colors.LbryGreen
  },
  description: {
    color: Colors.DescriptionGrey,
    fontFamily: 'Metropolis-Regular',
    fontSize: 16,
    lineHeight: 20,
    marginLeft: 20,
    marginRight: 20,
    marginBottom: 16
  },
  thumbnail: {
    width: screenWidth,
    height: containedMediaHeight,
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
    zIndex: 301,
    elevation: 21
  },
  containedPlayer: {
    width: '100%',
    height: containedMediaHeightWithControls,
  },
  containedPlayerLandscape: {
    width: '100%',
    height: '100%'
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
    zIndex: 300,
    elevation: 20,
    backgroundColor: Colors.Black
  },
  containedPlayerBackground: {
    width: '100%',
    height: containedMediaHeight,
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
    flexDirection: 'row',
    justifyContent: 'space-between',
    paddingLeft: 16,
    paddingRight: 16,
    paddingTop: 16,
    paddingBottom: 8,
    marginTop: -14,
    width: '100%',
  },
  fileActions: {
    alignSelf: 'flex-end'
  },
  actionButton: {
    alignSelf: 'flex-start',
    backgroundColor: Colors.White,
    paddingLeft: 24,
    paddingRight: 24
  },
  loading: {
    position: 'absolute',
    top: '40%'
  },
  busyContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    flexDirection: 'row'
  },
  dmcaContainer: {
    flex: 1,
    alignItems: 'flex-start',
    justifyContent: 'center',
    paddingLeft: 24,
    paddingRight: 24
  },
  dmcaText: {
    fontFamily: 'Metropolis-Regular',
    fontSize: 18,
    lineHeight: 24
  },
  dmcaLink: {
    color: Colors.LbryGreen,
    fontFamily: 'Metropolis-Regular',
    fontSize: 18,
    lineHeight: 24,
    marginTop: 24
  },
  infoText: {
    fontFamily: 'Metropolis-Regular',
    fontSize: 20,
    textAlign: 'center',
    marginLeft: 10
  },
  viewer: {
    position: 'absolute',
    flex: 1,
    left: 0,
    right: 0,
    top: 0,
    bottom: 60,
    zIndex: 100
  },
  link: {
    color: Colors.Grey
  },
  linkTapped: {
    color: "rgba(64, 184, 154, .2)"
  },
  tipCard: {
    backgroundColor: Colors.White,
    position: 'absolute',
    top: containedMediaHeightWithControls - 16,
    width: '100%',
    paddingTop: 8,
    paddingBottom: 8,
    paddingLeft: 16,
    paddingRight: 16
  },
  row: {
    flexDirection: 'row',
    flex: 1,
    justifyContent: 'space-between'
  },
  amountRow: {
    flexDirection: 'row',
    flex: 0.75
  },
  button: {
    backgroundColor: Colors.LbryGreen,
    alignSelf: 'flex-end',
    marginBottom: 6
  },
  cancelTipLink: {
    alignSelf: 'flex-end',
    marginBottom: 14
  },
  input: {
    fontFamily: 'Metropolis-Regular',
    fontSize: 14
  },
  tipAmountInput: {
    alignSelf: 'flex-start',
    width: 80,
    fontSize: 16,
    letterSpacing: 1
  },
  currency: {
    alignSelf: 'flex-start',
    marginTop: 17
  },
  text: {
    fontFamily: 'Metropolis-Regular',
    fontSize: 16,
    lineHeight: 24
  }
});

export default filePageStyle;
