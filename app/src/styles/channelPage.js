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
  title: {
    color: Colors.LbryGreen,
    fontFamily: 'Metropolis-SemiBold',
    fontSize: 30,
    margin: 16
  }
});

export default channelPageStyle;
