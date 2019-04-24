import { Dimensions, PixelRatio, StyleSheet } from 'react-native';
import { mediaWidth, mediaHeight } from './discover';
import Colors from './colors';

const screenDimension = Dimensions.get('window');
const screenWidth = screenDimension.width;
const screenHeight = screenDimension.height;
const screenWidthPixels = PixelRatio.getPixelSizeForLayoutSize(screenWidth);
const screenHeightPixels = PixelRatio.getPixelSizeForLayoutSize(screenHeight);
const verticalAdjust = (screenHeightPixels > 1280 && screenHeightPixels <= 1920) ? 6 : 0;
const thumbnailWidth = (screenWidthPixels <= 720) ? 144 : 156;
const thumbnailHeight = ((screenWidth / screenHeight) * thumbnailWidth) - verticalAdjust;

const fileListStyle = StyleSheet.create({
  item: {
    flex: 1,
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginTop: 8
  },
  detailsContainer: {
    flex: 1
  },
  featuredUri: {
    fontFamily: 'Inter-UI-SemiBold',
    fontSize: 24,
    color: Colors.White
  },
  featuredTitle: {
    fontFamily: 'Inter-UI-SemiBold',
    fontSize: (screenWidthPixels <= 720) ? 12 : 16,
    color: Colors.White
  },
  thumbnail: {
    width: thumbnailWidth,
    height: thumbnailHeight,
    marginRight: (screenWidthPixels <= 720) ? 10 : 12,
    alignItems: 'center',
    justifyContent: 'center',
  },
  title: {
    fontFamily: 'Inter-UI-SemiBold',
    fontSize: (screenWidthPixels <= 720) ? 12 : 14
  },
  uri: {
    fontFamily: 'Inter-UI-SemiBold',
    fontSize: (screenWidthPixels <= 720) ? 12 : 14,
    marginBottom: 8
  },
  publisher: {
    fontFamily: 'Inter-UI-SemiBold',
    fontSize: (screenWidthPixels <= 720) ? 11 : 12,
    marginTop: (screenWidthPixels <= 720) ? 1 : 3,
    color: Colors.LbryGreen
  },
  loading: {
    position: 'absolute'
  },
  info: {
    marginTop: (screenWidthPixels <= 720) ? 1 : 2,
    flexDirection: 'row',
    justifyContent: 'space-between'
  },
  infoText: {
    fontFamily: 'Inter-UI-Regular',
    fontSize: (screenWidthPixels <= 720) ? 11 : 12,
    color: Colors.ChannelGrey
  },
  downloadInfo: {
    marginTop: 2
  },
  progress: {
    marginTop: (screenWidthPixels <= 720) ? 2 : 4,
    height: 3,
    flex: 1,
    flexDirection: 'row'
  },
  progressCompleted: {
    backgroundColor: Colors.LbryGreen
  },
  progressRemaining: {
    backgroundColor: Colors.LbryGreen,
    opacity: 0.2
  },
  downloadedIcon: {
    position: 'absolute',
    top: 8,
    left: 8
  },
  fileItem: {
    marginLeft: 24,
    marginRight: 24,
    marginBottom: 48
  },
  fileItemMedia: {
    width: mediaWidth,
    height: mediaHeight,
    alignItems: 'center',
    justifyContent: 'center'
  },
});

export default fileListStyle;
