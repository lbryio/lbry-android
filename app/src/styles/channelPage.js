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
    paddingTop: 30,
    flex: 1,
    marginBottom: 16
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
    flexDirection: 'row',
    justifyContent: 'space-between',
    paddingLeft: 16,
    paddingRight: 16,
    marginBottom: 76
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
