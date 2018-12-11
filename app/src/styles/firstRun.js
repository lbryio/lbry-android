import { StyleSheet } from 'react-native';
import Colors from './colors';

const firstRunStyle = StyleSheet.create({
  screenContainer: {
    flex: 1,
    backgroundColor: Colors.LbryGreen
  },
  container: {
    flex: 9,
    justifyContent: 'center',
    backgroundColor: Colors.LbryGreen
  },
  title: {
    fontFamily: 'Inter-UI-SemiBold',
    fontSize: 40,
    marginLeft: 32,
    marginRight: 32,
    marginBottom: 32,
    color: Colors.White
  },
  paragraph: {
    fontFamily: 'Inter-UI-Regular',
    fontSize: 18,
    lineHeight: 24,
    marginLeft: 32,
    marginRight: 32,
    marginBottom: 20,
    color: Colors.White
  },
  infoParagraph: {
    fontFamily: 'Inter-UI-Regular',
    fontSize: 14,
    lineHeight: 20,
    marginLeft: 32,
    marginRight: 32,
    marginBottom: 20,
    color: Colors.White
  },
  emailInput: {
    fontFamily: 'Inter-UI-Regular',
    fontSize: 24,
    lineHeight: 24,
    marginLeft: 32,
    marginRight: 32,
    marginBottom: 20,
    textAlign: 'center'
  },
  leftButton: {
    flex: 1,
    alignSelf: 'flex-start',
    marginLeft: 32,
    marginRight: 32
  },
  bottomContainer: {
    flex: 1
  },
  actionButton: {
    backgroundColor: Colors.White,
    alignSelf: 'center',
    fontFamily: 'Inter-UI-Regular',
    fontSize: 12,
    paddingLeft: 16,
    paddingRight: 16
  },
  button: {
    alignSelf: 'flex-end',
    padding: 20,
    paddingLeft: 32,
    paddingRight: 32
  },
  buttonText: {
    fontFamily: 'Inter-UI-Regular',
    fontSize: 24,
    color: Colors.White
  },
  waiting: {
    marginBottom: 24
  },
  pageWaiting: {
    alignSelf:  'center'
  }
});

export default firstRunStyle;
