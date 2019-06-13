import { StyleSheet } from 'react-native';
import Colors from 'styles/colors';

const searchStyle = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: Colors.PageBackground,
  },
  scrollContainer: {
    flex: 1,
    marginTop: 60,
  },
  busyContainer: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  scrollPadding: {
    paddingBottom: 16,
  },
  resultItem: {
    flex: 1,
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginTop: 8,
    marginLeft: 8,
    marginRight: 8,
  },
  featuredResultItem: {
    flex: 1,
    flexDirection: 'row',
    justifyContent: 'space-between',
    paddingTop: 8,
    paddingBottom: 8,
    paddingLeft: 8,
    paddingRight: 8,
    backgroundColor: Colors.Black,
  },
  searchInput: {
    width: '100%',
    height: '100%',
    fontFamily: 'Inter-UI-Regular',
    fontSize: 16,
  },
  noResults: {
    flex: 1,
    width: '100%',
    height: '100%',
    justifyContent: 'center',
    alignItems: 'center',
  },
  noResultsText: {
    fontFamily: 'Inter-UI-Regular',
    fontSize: 16,
    marginTop: 16,
    marginLeft: 16,
    marginRight: 16,
  },
  boldText: {
    fontFamily: 'Inter-UI-SemiBold',
    fontSize: 16,
  },
  loading: {
    position: 'absolute',
  },
});

export default searchStyle;
