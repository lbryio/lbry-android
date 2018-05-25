import { StyleSheet } from 'react-native';
import Colors from './colors';

const uriBarStyle = StyleSheet.create({
  uriContainer: {
    backgroundColor: Colors.White,
    padding: 8,
    alignSelf: 'flex-end',
    height: 60,
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
  },
  overlay: {
    position: 'absolute',
    backgroundColor: 'transparent',
    bottom: 0,
    width: '100%',
    zIndex: 200,
    elevation: 16
  },
  inFocus: {
    height: '100%'
  },
  suggestions: {
    backgroundColor: 'white',
    flex: 1
  },
  item: {
    flexDirection: 'row',
    justifyContent: 'flex-start',
    padding: 12
  },
  itemText: {
    fontFamily: 'Metropolis-Regular',
    fontSize: 16,
    marginLeft: 12,
    marginRight: 12
  }
});

export default uriBarStyle;
