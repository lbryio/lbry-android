import { Dimensions, StyleSheet } from 'react-native';
import Colors from './colors';

const screenDimension = Dimensions.get('window');
const screenWidth = screenDimension.width;
const screenHeight = screenDimension.height;
const thumbnailHeight = 100;
const thumbnailWidth = (screenHeight / screenWidth) * thumbnailHeight;

const searchStyle = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center'
  },
  scrollContainer: {
    flex: 1,
    width: '100%',
    height: '100%',
    paddingLeft: 16,
    paddingRight: 16,
    marginBottom: 60
  },
  scrollPadding: {
    paddingBottom: 16
  },
  resultItem: {
    flex: 1,
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginTop: 16
  },
  thumbnail: {
    width: thumbnailWidth,
    height: thumbnailHeight,
    marginRight: 16,
    justifyContent: 'center'
  },
  detailsContainer: {
    flex: 1
  },
  searchInput: {
    width: '100%',
    height: '100%',
    fontFamily: 'Metropolis-Regular',
    fontSize: 16
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
  noResultsText: {
    textAlign: 'center',
    fontFamily: 'Metropolis-Regular',
    fontSize: 14,
    position: 'absolute'
  },
  loading: {
    position: 'absolute'
  }
});

export default searchStyle;
