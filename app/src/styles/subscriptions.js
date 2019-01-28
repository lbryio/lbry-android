import { StyleSheet } from 'react-native';

const subscriptionsStyle = StyleSheet.create({
  container: {
    flex: 1,
  },
  busyContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    flexDirection: 'row',
    padding: 16
  },
  scrollContainer: {
    flex: 1,
    marginBottom: 60
  },
  scrollPadding: {
    paddingTop: 24
  },
  infoText: {
    textAlign: 'center',
    fontFamily: 'Inter-UI-Regular',
    fontSize: 16,
  },
  fileItem: {
    marginLeft: 24,
    marginRight: 24,
    marginBottom: 24
  }
});

export default subscriptionsStyle;
