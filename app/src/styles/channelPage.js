import { StyleSheet } from 'react-native';
import Colors from './colors';

const channelPageStyle = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
  },
  content: {
    flex: 1
  },
  fileList: {
    flex: 1,
    paddingTop: 30,
    marginTop: 60,
  },
  fileListContent: {
    paddingBottom: 16
  },
  title: {
    color: Colors.LbryGreen,
    fontFamily: 'Inter-UI-SemiBold',
    fontSize: 30,
    margin: 16
  },
   busyContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    flexDirection: 'row'
  },
  infoText: {
    fontFamily: 'Inter-UI-Regular',
    fontSize: 20,
    textAlign: 'center',
    marginLeft: 10
  },
  pageButtons: {
    width: '100%',
    flexDirection: 'row',
    justifyContent: 'space-between',
    position: 'absolute',
    bottom: 76,
    paddingLeft: 16,
    paddingRight: 16
  },
  button: {
    backgroundColor: Colors.LbryGreen,
    paddingLeft: 16,
    paddingRight: 16
  },
  nextButton: {
    alignSelf: 'flex-end'
  }
});

export default channelPageStyle;
