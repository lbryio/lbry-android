import { StyleSheet } from 'react-native';

const splashStyle = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    backgroundColor: '#40b89a'
  },
  title: {
    fontFamily: 'Metropolis-Bold',
    fontSize: 64,
    textAlign: 'center',
    marginBottom: 48,
    color: '#ffffff'
  },
  details: {
    fontFamily: 'Metropolis-Regular',
    fontSize: 14,
    marginLeft: 16,
    marginRight: 16,
    color: '#ffffff',
    textAlign: 'center'
  },
  message: {
    fontFamily: 'Metropolis-Bold',
    fontSize: 18,
    color: '#ffffff',
    marginLeft: 16,
    marginRight: 16,
    marginBottom: 4,
    textAlign: 'center'
  }
});

export default splashStyle;
