import { StyleSheet } from 'react-native';
import Colors from './colors';

const fileItemMediaStyle = StyleSheet.create({
  autothumb: {
    flex: 1,
    width: '100%',
    height: 200,
    justifyContent: 'center',
    alignItems: 'center'
  },
  autothumbText: {
    fontFamily: 'Inter-UI-SemiBold',
    textAlign: 'center',
    color: Colors.White,
    fontSize: 40
  },
  autothumbPurple: {
    backgroundColor: '#9c27b0'
  },
  autothumbRed: {
    backgroundColor: '#e53935'
  },
  autothumbPink: {
    backgroundColor: '#e91e63'
  },
  autothumbIndigo: {
    backgroundColor: '#3f51b5'
  },
  autothumbBlue: {
    backgroundColor: '#2196f3'
  },
  autothumbLightBlue: {
    backgroundColor: '#039be5'
  },
  autothumbCyan: {
    backgroundColor: '#00acc1'
  },
  autothumbTeal: {
    backgroundColor: '#009688'
  },
  autothumbGreen: {
    backgroundColor: '#43a047'
  },
  autothumbYellow: {
    backgroundColor: '#ffeb3b'
  },
  autothumbOrange: {
    backgroundColor: '#ffa726'
  },
  resolving: {
    alignItems: 'center',
    flex: 1,
    justifyContent: 'center'
  },
  text: {
    color: '#ffffff',
    fontFamily: 'Inter-UI-Regular',
    fontSize: 16,
    marginTop: 8
  },
  thumbnail: {
    flex: 1,
    width: '100%',
    height: 200,
    shadowColor: 'transparent'
  }
});

export default fileItemMediaStyle;
