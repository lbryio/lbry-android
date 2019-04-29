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
    backgroundColor: Colors.PageBackground
  },
  pageContainer: {
    flex: 1,
    backgroundColor: Colors.PageBackground
  },
  divider: {
    backgroundColor: Colors.LighterGrey,
    width: '100%',
    height: 1,
    marginTop: 4,
    marginBottom: 20
  },
  innerPageContainer: {
    flex: 1
  },
  innerPageContainerFsMode: {
    flex: 1,
    marginBottom: 0
  },
  mediaContainer: {
    alignItems: 'center',
    width: screenWidth,
    height: containedMediaHeightWithControls,
    marginTop: 60,
    marginBottom: -17
  },
  emptyClaimText: {
    fontFamily: 'Inter-UI-Regular',
    textAlign: 'center',
    fontSize: 20,
    marginLeft: 16,
    marginRight: 16
  },
  scrollContainer: {
    flex: 1
  },
  scrollContent: {
    paddingTop: 10
  },
  scrollContainerActions: {
    flex: 1
  },
  title: {
    fontFamily: 'Inter-UI-Bold',
    fontSize: 16,
    flex: 18
  },
  titleRow: {
    flexDirection: 'row',
    marginTop: 12,
    marginBottom: 2,
    marginLeft: 12,
    marginRight: 12,
    alignItems: 'center',
    justifyContent: 'center'
  },
  channelRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginLeft: 12,
    marginRight: 12,
    marginBottom: 16,
  },
  subscriptionRow: {
    flex: 0.5,
    flexDirection: 'row',
    justifyContent: 'flex-end',
    marginTop: 8
  },
  publishInfo: {
    flex: 0.5,
    marginTop: 6,
  },
  channelName: {
    fontFamily: 'Inter-UI-SemiBold',
    fontSize: 14,
    color: Colors.LbryGreen
  },
  publishDateText: {
    fontFamily: 'Inter-UI-SemiBold',
    fontSize: 12,
    color: Colors.DescriptionGrey
  },
  publishDate: {
    marginTop: 4
  },
  description: {
    color: Colors.DescriptionGrey,
    fontFamily: 'Inter-UI-Regular',
    fontSize: 13,
    lineHeight: 18,
    marginLeft: 12,
    marginRight: 12,
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
    top: 60
  },
  containedPlayerLandscape: {
    width: '100%',
    height: '100%',
    top: 60
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
    marginTop: 60,
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
    fontFamily: 'Inter-UI-Bold',
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
    width: '100%',
  },
  fileActions: {
    alignSelf: 'flex-end'
  },
  socialActions: {
    alignSelf: 'flex-start',
    flexDirection: 'row'
  },
  actionButton: {
    alignSelf: 'flex-start',
    backgroundColor: Colors.White,
    paddingLeft: 16,
    paddingRight: 16
  },
  bellButton: {
    marginLeft: 8
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
    fontFamily: 'Inter-UI-Regular',
    fontSize: 18,
    lineHeight: 24
  },
  dmcaLink: {
    color: Colors.LbryGreen,
    fontFamily: 'Inter-UI-Regular',
    fontSize: 18,
    lineHeight: 24,
    marginTop: 24
  },
  infoText: {
    fontFamily: 'Inter-UI-Regular',
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
    width: '100%',
    marginTop: -12,
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
    fontFamily: 'Inter-UI-Regular',
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
  descriptionToggle: {
    alignItems: 'center',
    justifyContent: 'center',
    width: 36,
    height: 36,
    marginTop: -8
  },
  text: {
    fontFamily: 'Inter-UI-Regular',
    fontSize: 16,
    lineHeight: 24
  },
  tipButton: {
    marginRight: 8
  },
  tagContainer: {
    marginLeft: 12,
    marginRight: 12,
    marginBottom: 16,
    flexDirection: 'row'
  },
  tagTitle: {
    fontFamily: 'Inter-UI-SemiBold',
    flex: 0.2
  },
  tagList: {
    fontFamily: 'Inter-UI-Regular',
    flex: 0.8,
    flexDirection: 'row'
  },
  tagItem: {
    marginRight: 16
  }
});

export default filePageStyle;
