import { Dimensions, PixelRatio, StyleSheet } from 'react-native';
import Colors from './colors';

const screenDimension = Dimensions.get('window');
export const screenWidth = screenDimension.width;
export const screenHeight = screenDimension.height;
const screenWidthPixels = PixelRatio.getPixelSizeForLayoutSize(screenWidth);
const screenHeightPixels = PixelRatio.getPixelSizeForLayoutSize(screenHeight);
// calculate thumbnail width and height based on device's aspect ratio
export const horizontalMargin = 48; // left and right margins (24 + 24)
export const verticalMargin = (screenWidthPixels > 720 && screenHeightPixels > 1920) ? 0 : ((screenWidthPixels <= 720) ? 20 : 16);
export const mediaWidth = screenWidth - horizontalMargin;
export const mediaHeight = ((screenWidth / screenHeight) * ((screenWidthPixels <= 720) ? screenWidth : mediaWidth)) - verticalMargin;
export const fileItemWidth = screenWidth * 3/5;
export const fileItemMediaWidth = fileItemWidth;
export const fileItemMediaHeight = fileItemWidth * 9/16;

const discoverStyle = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: Colors.PageBackground
  },
  scrollContainer: {
    flex: 1,
    paddingTop: 12,
    marginTop: 60,
  },
  trendingContainer: {
    flex: 1,
    marginTop: 60,
    paddingTop: 30
  },
  busyContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    flexDirection: 'row'
  },
  title: {
    fontFamily: 'Inter-UI-Regular',
    fontSize: 20,
    textAlign: 'center',
    marginLeft: 10
  },
  categoryName: {
    fontFamily: 'Inter-UI-SemiBold',
    fontSize: 18,
    marginLeft: 24,
    marginTop: 12,
    marginBottom: 6,
    color: Colors.Black
  },
  fileItem: {
    width: fileItemWidth,
    marginRight: 12
  },
  fileItemMedia: {
    width: fileItemMediaWidth,
    height: fileItemMediaHeight,
    alignItems: 'center',
    justifyContent: 'center'
  },
  fileItemName: {
    fontFamily: 'Inter-UI-SemiBold',
    marginTop: 8,
    fontSize: 14
  },
  channelName: {
    fontFamily: 'Inter-UI-SemiBold',
    fontSize: 12,
    marginTop: 4,
    color: Colors.LbryGreen
  },
  downloadedIcon: {
    position: 'absolute',
    right: 8,
    top: 8
  },
  filePriceContainer: {
    backgroundColor: Colors.BrightGreen,
    justifyContent: 'center',
    position: 'absolute',
    right: 8,
    top: 8,
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
  drawerMenuButton: {
    height: '100%',
    justifyContent: 'center'
  },
  drawerHamburger: {
    marginLeft: 16,
    marginRight: 16
  },
  rightHeaderIcon: {
    marginRight: 16
  },
  overlay: {
    flex: 1,
    opacity: 1,
    backgroundColor: '#222222',
    alignItems: 'center',
    justifyContent: 'center',
    padding: 32,
    position: 'absolute',
    left: 0,
    top: 0,
    width: '100%',
    height: '100%'
  },
  overlayText: {
    color: Colors.White,
    fontSize: 14,
    textAlign: 'center',
    fontFamily: 'Inter-UI-Regular'
  },
  rewardTitleContainer: {
    alignItems: 'center',
    flexDirection: 'row',
    justifyContent: 'space-between'
  },
  rewardIcon: {
    color: Colors.Red,
    flex: 0.1,
    textAlign: 'right',
    marginTop: 6
  },
  rewardTitle: {
    flex: 0.9
  },
  menuText: {
    fontFamily: 'Inter-UI-Regular',
    fontSize: 16
  },
  titleText: {
    fontFamily: 'Inter-UI-Regular'
  },
  detailsRow: {
    flexDirection: 'row',
    justifyContent: 'space-between'
  },
  dateTime: {
    marginTop: 2
  },
  dateTimeText: {
    fontFamily: 'Inter-UI-Regular',
    fontSize: 12,
    color: Colors.DescriptionGrey
  },
  scrollPadding: {
    paddingBottom: 24
  },
  horizontalScrollContainer: {
    marginBottom: 12
  },
  horizontalScrollPadding: {
    paddingLeft: 20
  }
});

export default discoverStyle;
