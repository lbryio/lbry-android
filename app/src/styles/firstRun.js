import { StyleSheet } from 'react-native';
import Colors from './colors';

const firstRunStyle = StyleSheet.create({
  screenContainer: {
    flex: 1,
    backgroundColor: Colors.LbryGreen
  },
  container: {
    flex: 9,
    justifyContent: 'center',
    backgroundColor: Colors.LbryGreen
  },
  title: {
    fontFamily: 'Metropolis-SemiBold',
    fontSize: 40,
    marginLeft: 32,
    marginRight: 32,
    marginBottom: 32,
    color: Colors.White
  },
  paragraph: {
    fontFamily: 'Metropolis-Regular',
    fontSize: 18,
    lineHeight: 24,
    marginLeft: 32,
    marginRight: 32,
    marginBottom: 20,
    color: Colors.White
  },
  button: {
    flex: 1,
    alignSelf: 'flex-end',
    marginLeft: 32,
    marginRight: 32
  },
  buttonText: {
    fontFamily: 'Metropolis-Regular',
    fontSize: 28,
    color: Colors.White
  }
});

export default firstRunStyle;
