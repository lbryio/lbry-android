import { StyleSheet } from 'react-native';

const aboutStyle = StyleSheet.create({
  scrollContainer: {
    paddingTop: 16,
    paddingBottom: 16
  },
  row: {
    marginBottom: 1,
    backgroundColor: '#f9f9f9',
    padding: 16,
    flex: 1,
    flexDirection: 'row',
    justifyContent: 'space-between'
  },
  col: {
    alignSelf: 'stretch'
  },
  releaseInfoTitle: {
    fontFamily: 'Metropolis-Regular',
    marginLeft: 12,
    marginRight: 12,
    marginBottom: 12,
    fontSize: 14
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
