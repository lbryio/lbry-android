import { StyleSheet, Dimensions } from 'react-native';

const screenDimension = Dimensions.get('window'); 
const width = screenDimension.width - 48; // screen width minus combined left and right margins

const fileItemMediaStyle = StyleSheet.create({
  autothumb: {
    flex: 1,
    width: '100%',
    height: 200,
    justifyContent: 'center'
  },
  autothumbText: {
    fontFamily: 'Metropolis-SemiBold',
    textAlign: 'center',
    color: '#ffffff',
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
  thumbnail: {
    flex: 1,
    width: '100%',
    height: 200,
    shadowColor: 'transparent'
  }
});

export default fileItemMediaStyle;
