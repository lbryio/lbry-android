import { StyleSheet } from 'react-native';
import Colors from './colors';

const buttonStyle = StyleSheet.create({
  button: {
    borderRadius: 24,
    padding: 8,
    paddingLeft: 12,
    paddingRight: 12,
    alignItems: 'center',
    justifyContent: 'center'
  },
  disabled: {
    backgroundColor: '#999999'
  },
  row: {
    flexDirection: 'row'
  },
  iconLight: {
    color: Colors.White,
  },
  iconDark: {
    color: Colors.DarkGrey,
  },
  textLight: {
    color: Colors.White,
  },
  textDark: {
    color: Colors.DarkGrey
  },
  text: {
    fontFamily: 'Inter-UI-Regular',
    fontSize: 14
  },
  textWithIcon: {
    marginLeft: 8
  }
});

export default buttonStyle;
