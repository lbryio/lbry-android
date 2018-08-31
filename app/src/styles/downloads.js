import { StyleSheet } from 'react-native';

const downloadsStyle = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center'
  },
  itemList: {
    flex: 1,
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
  noDownloadsText: {
    textAlign: 'center',
    fontFamily: 'Metropolis-Regular',
    fontSize: 14,
    position: 'absolute'
  },
  loading: {
    position: 'absolute'
  }
});

export default downloadsStyle;
