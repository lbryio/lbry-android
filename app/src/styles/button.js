import { StyleSheet } from 'react-native';

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
    flexDirection: 'row',
  },
  icon: {
    color: '#ffffff',
  },
  text: {
    color: '#ffffff',
    fontFamily: 'Metropolis-Regular',
    fontSize: 14
  },
  textWithIcon: {
    marginLeft: 8
  }
});

export default buttonStyle;
