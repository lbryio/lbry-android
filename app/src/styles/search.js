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
    marginTop: 8
  },
  searchInput: {
    width: '100%',
    height: '100%',
    fontFamily: 'Metropolis-Regular',
    fontSize: 16
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
