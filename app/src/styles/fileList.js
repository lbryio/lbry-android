import { Dimensions, PixelRatio, StyleSheet } from 'react-native';
import Colors from './colors';

const screenDimension = Dimensions.get('window');
const screenWidth = screenDimension.width;
const screenHeight = screenDimension.height;
const screenWidthPixels = PixelRatio.getPixelSizeForLayoutSize(screenWidth);
const thumbnailHeight = (screenWidthPixels <= 720) ? 72 : 96;
const thumbnailWidth = (screenHeight / screenWidth) * thumbnailHeight;

const fileListStyle = StyleSheet.create({
  item: {
    flex: 1,
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginTop: 16
  },
  detailsContainer: {
    flex: 1
  },
  thumbnail: {
    width: thumbnailWidth,
    height: thumbnailHeight,
    marginRight: (screenWidthPixels <= 720) ? 10 : 12,
    justifyContent: 'center'
  },
  title: {
    fontFamily: 'Metropolis-SemiBold',
    fontSize: (screenWidthPixels <= 720) ? 14 : 16
  },
  uri: {
    fontFamily: 'Metropolis-SemiBold',
    fontSize: (screenWidthPixels <= 720) ? 12 : 14,
    marginBottom: 8
  },
  publisher: {
    fontFamily: 'Metropolis-SemiBold',
    fontSize: (screenWidthPixels <= 720) ? 12 : 14,
    marginTop: 3,
    color: Colors.LbryGreen
  },
  loading: {
    position: 'absolute'
  },
  downloadInfo: {
    marginTop: 8
  },
  downloadStorage: {
    fontFamily: 'Metropolis-Regular',
    fontSize: (screenWidthPixels <= 720) ? 12 : 14,
    color: Colors.ChannelGrey
  },
  progress: {
    marginTop: 4,
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
  }
});

export default fileListStyle;
