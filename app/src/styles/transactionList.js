import { StyleSheet } from 'react-native';
import Colors from './colors';

const transactionListStyle = StyleSheet.create({
  listItem: {
    borderBottomWidth: 1,
    borderBottomColor: '#eeeeee',
    paddingLeft: 16,
    paddingRight: 16,
    paddingTop: 12,
    paddingBottom: 12
  },
  row: {
    flexDirection: 'row',
    justifyContent: 'space-between'
  },
  topRow: {
    marginBottom: 4
  },
  col: {
    alignSelf: 'stretch'
  },
  text: {
    fontFamily: 'Inter-UI-Regular',
    fontSize: 14
  },
  link: {
    color: Colors.LbryGreen,
    fontFamily: 'Inter-UI-Regular',
    fontSize: 14
  },
  amount: {
    textAlign: 'right'
  },
  smallText: {
    fontFamily: 'Inter-UI-Regular',
    fontSize: 12,
    color: '#aaaaaa'
  },
  smallLink: {
    color: Colors.LbryGreen,
    fontFamily: 'Inter-UI-Regular',
    fontSize: 12
  },
  noTransactions: {
    fontFamily: 'Inter-UI-Regular',
    textAlign: 'center',
    padding: 16,
    color: '#aaaaaa'
  }
});

export default transactionListStyle;
