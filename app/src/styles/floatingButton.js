import { StyleSheet } from 'react-native';
import Colors from './colors';

const floatingButtonStyle = StyleSheet.create({
  container: {
    position: 'absolute',
    zIndex: 100,
    borderRadius: 24,
    padding: 14,
    paddingLeft: 20,
    paddingRight: 20,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: Colors.BrighterLbryGreen,
    shadowColor: 'black',
    shadowOpacity: 0.1,
    shadowRadius: StyleSheet.hairlineWidth,
    shadowOffset: {
      height: StyleSheet.hairlineWidth,
    },
    elevation: 4
  },
  text: {
    color: Colors.White,
    fontFamily: 'Metropolis-Bold',
    fontSize: 18,
  },
  bottomRight: {
    right: 10,
    bottom: 70
  }
});

export default floatingButtonStyle;
