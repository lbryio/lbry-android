import { StyleSheet } from 'react-native';

const downloadsStyle = StyleSheet.create({
  container: {
    flex: 1
  },
  busyContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    flexDirection: 'row'
  },
  subContainer: {
    flex: 1
  },
  itemList: {
    flex: 1,
  },
  scrollContainer: {
    flex: 1,
    paddingLeft: 16,
    paddingRight: 16,
    marginTop: 16,
    marginBottom: 60
  },
  scrollPadding: {
    marginTop: -16,
    paddingBottom: 16
  },
  noDownloadsText: {
    textAlign: 'center',
    fontFamily: 'Inter-UI-Regular',
    fontSize: 14,
    position: 'absolute'
  },
  loading: {
    position: 'absolute'
  }
});

export default downloadsStyle;
