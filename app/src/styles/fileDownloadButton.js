import { StyleSheet } from 'react-native';
import Colors from 'styles/colors';

const fileDownloadButtonStyle = StyleSheet.create({
  container: {
    paddingLeft: 32,
    paddingRight: 32,
    height: 36,
    borderRadius: 18,
    justifyContent: 'center',
    backgroundColor: Colors.LbryGreen,
  },
  text: {
    fontFamily: 'Inter-UI-Medium',
    color: Colors.White,
    fontSize: 14,
    textAlign: 'center',
  },
});

export default fileDownloadButtonStyle;
