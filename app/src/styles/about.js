import { StyleSheet } from 'react-native';
import Colors from './colors';

const aboutStyle = StyleSheet.create({
  container: {
    flex: 1
  },
  scrollContainer: {
    flex: 1
  },
  row: {
    marginBottom: 1,
    backgroundColor: '#f9f9f9',
    padding: 16,
    flex: 1,
    flexDirection: 'row',
    justifyContent: 'space-between'
  },
  innerRow: {
    marginBottom: 4,
    flex: 1,
    flexDirection: 'row',
    justifyContent: 'space-between'
  },
  verticalRow: {
    marginBottom: 1,
    backgroundColor: '#f9f9f9',
    padding: 16,
    flex: 1
  },
  title: {
    color: Colors.LbryGreen,
    fontSize: 24,
    fontFamily: 'Metropolis-SemiBold',
    marginTop: 16,
    marginLeft: 12,
    marginRight: 12,
    marginBottom: 8
  },
  paragraph: {
    fontFamily: 'Metropolis-Regular',
    fontSize: 16,
    lineHeight: 24,
    marginLeft: 12,
    marginRight: 12,
    marginBottom: 24
  },
  links: {
    marginLeft: 12,
    marginRight: 12,
    marginBottom: 18
  },
  link: {
    color: Colors.LbryGreen,
    fontFamily: 'Metropolis-Regular',
    fontSize: 16,
    marginBottom: 24
  },
  listLink: {
    color: Colors.LbryGreen,
    fontFamily: 'Metropolis-Regular',
    fontSize: 15,
    alignSelf: 'flex-end'
  },
  col: {
    alignSelf: 'stretch'
  },
  socialTitle: {
    fontFamily: 'Metropolis-Regular',
    marginLeft: 12,
    marginRight: 12,
    marginBottom: 8,
    fontSize: 20
  },
  releaseInfoTitle: {
    fontFamily: 'Metropolis-Regular',
    marginLeft: 12,
    marginRight: 12,
    marginBottom: 12,
    fontSize: 20
  },
  text: {
    fontFamily: 'Metropolis-SemiBold',
    fontSize: 15
  },
  valueText: {
    fontFamily: 'Metropolis-Regular',
    textAlign: 'right',
    fontSize: 15
  },
  lineValueText: {
    fontFamily: 'Metropolis-Regular',
    fontSize: 15
  }
});

export default aboutStyle;
