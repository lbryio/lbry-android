import { StyleSheet } from 'react-native';
import Colors from './colors';

const splashStyle = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    backgroundColor: Colors.LbryGreen,
  },
  title: {
    fontFamily: 'Inter-UI-Bold',
    fontSize: 64,
    textAlign: 'center',
    marginBottom: 48,
    color: Colors.White,
  },
  errorTitle: {
    fontFamily: 'Inter-UI-Regular',
    fontSize: 28,
    marginBottom: 24,
    marginLeft: 24,
    marginRight: 24,
    color: Colors.White,
  },
  paragraph: {
    fontFamily: 'Inter-UI-Regular',
    fontSize: 16,
    lineHeight: 24,
    marginBottom: 20,
    marginLeft: 24,
    marginRight: 24,
    color: Colors.White,
  },
  continueButton: {
    fontSize: 16,
    backgroundColor: Colors.White,
    marginTop: 24,
    marginLeft: 24,
    marginRight: 24,
    alignSelf: 'flex-end',
  },
  loading: {
    marginBottom: 36,
  },
  progress: {
    alignSelf: 'center',
    marginBottom: 36,
    width: '50%',
  },
  details: {
    fontFamily: 'Inter-UI-Regular',
    fontSize: 14,
    marginLeft: 16,
    marginRight: 16,
    color: Colors.White,
    textAlign: 'center',
  },
  message: {
    fontFamily: 'Inter-UI-Bold',
    fontSize: 18,
    color: Colors.White,
    marginLeft: 16,
    marginRight: 16,
    marginBottom: 4,
    textAlign: 'center',
  },
});

export default splashStyle;
