import { StyleSheet } from 'react-native';

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
    padding: 16,
    marginBottom: 60
  },
  scrollPadding: {
    paddingBottom: 16
  },
  resultItem: {
    flex: 1,
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginBottom: 16
  },
  thumbnail: {
    width: '100%',
    height: 80
  },
  thumbnailContainer: {
    width: '25%' 
  },
  detailsContainer: {
    width: '70%'
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
  publisher: {
    fontFamily: 'Metropolis-SemiBold',
    fontSize: 12,
    marginTop: 4,
    color: '#c0c0c0'
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
