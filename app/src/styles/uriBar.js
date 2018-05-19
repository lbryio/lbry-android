import { StyleSheet } from 'react-native';
import Colors from './colors';

const uriBarStyle = StyleSheet.create({
  uriContainer: {
    backgroundColor: Colors.White,
    padding: 8,
    alignSelf: 'flex-end',
    width: '100%',
    shadowColor: Colors.Black,
    shadowOpacity: 0.1,
    shadowRadius: StyleSheet.hairlineWidth,
    shadowOffset: {
      height: StyleSheet.hairlineWidth,
    },
    elevation: 4
  },
  uriText: {
    backgroundColor: Colors.White,
    borderWidth: 1,
    borderColor: Colors.LightGrey,
    padding: 8,
    borderRadius: 4,
    fontFamily: 'Metropolis-Regular',
    fontSize: 16,
    width: '100%'
  }
});

export default uriBarStyle;
