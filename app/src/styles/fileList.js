import { Dimensions, StyleSheet } from 'react-native';
import Colors from './colors';

const screenDimension = Dimensions.get('window');
const screenWidth = screenDimension.width;
const screenHeight = screenDimension.height;
const thumbnailHeight = 100;
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
    marginRight: 16,
    justifyContent: 'center'
  },
  title: {
    fontFamily: 'Metropolis-SemiBold',
    fontSize: 16
  },
  uri: {
    fontFamily: 'Metropolis-SemiBold',
    fontSize: 14,
    marginBottom: 8
  },
  publisher: {
    fontFamily: 'Metropolis-SemiBold',
    fontSize: 14,
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
    fontSize: 14,
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
