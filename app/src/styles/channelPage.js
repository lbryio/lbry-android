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
    flex: 1
  },
  title: {
    color: Colors.LbryGreen,
    fontFamily: 'Metropolis-SemiBold',
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
    fontFamily: 'Metropolis-Regular',
    fontSize: 20,
    textAlign: 'center',
    marginLeft: 10
  }
});

export default channelPageStyle;
