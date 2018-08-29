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
    width: '100%',
    height: 72
  },
  thumbnailContainer: {
    width: '40%'
  },
  detailsContainer: {
    width: '55%'
  },
  searchInput: {
    width: '100%',
    height: '100%',
    fontFamily: 'Metropolis-Regular',
    fontSize: 16
  },
  title: {
    fontFamily: 'Metropolis-SemiBold',
    fontSize: 14
  },
  uri: {
    fontFamily: 'Metropolis-SemiBold',
    fontSize: 14,
    marginBottom: 8
  },
  publisher: {
    fontFamily: 'Metropolis-SemiBold',
    fontSize: 12,
    marginTop: 3,
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
