import { StyleSheet } from 'react-native';

const fileDownloadButtonStyle = StyleSheet.create({
  container: {
    paddingLeft: 32,
    paddingRight: 32,
    height: 36,
    borderRadius: 18,
    justifyContent: 'center',
    backgroundColor: '#40c0a9',
  },
  text: {
    fontFamily: 'Inter-UI-Medium',
    color: '#ffffff',
    fontSize: 14,
    textAlign: 'center'
  }
});

export default fileDownloadButtonStyle;