import { StyleSheet } from 'react-native';

const aboutStyle = StyleSheet.create({
  scrollContainer: {
    paddingTop: 16,
    paddingBottom: 16
  },
  row: {
    marginBottom: 1,
    backgroundColor: '#f9f9f9',
    padding: 12,
    flex: 1,
    flexDirection: 'row',
    justifyContent: 'space-between'
  },
  col: {
    alignSelf: 'stretch'
  },
  releaseInfoTitle: {
    fontFamily: 'Metropolis-Regular',
    marginLeft: 16,
    marginRight: 16,
    marginBottom: 16,
    fontSize: 15
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
