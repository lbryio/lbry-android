import { StyleSheet } from 'react-native';
import Colors from './colors';

const walletStyle = StyleSheet.create({
  row: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center'
  },
  amountRow: {
    flexDirection: 'row'
  },
  address: {
    fontFamily: 'Metropolis-Regular',
    borderWidth: 1,
    borderStyle: 'dashed',
    borderColor: '#cccccc',
    backgroundColor: '#f9f9f9',
    padding: 8,
    width: '85%'
  },
  button: {
    backgroundColor: Colors.LbryGreen
  },
  historyList: {
    backgroundColor: '#ffffff'
  },
  card: {
    backgroundColor: '#ffffff',
    marginTop: 16,
    marginLeft: 16,
    marginRight: 16,
    padding: 16
  },
  transactionsCard: {
    backgroundColor: '#ffffff',
    margin: 16
  },
  title: {
    fontFamily: 'Metropolis-Bold',
    fontSize: 20,
    marginBottom: 24
  },
  transactionsTitle: {
    fontFamily: 'Metropolis-Bold',
    fontSize: 20
  },
  transactionsHeader: {
    paddingTop: 12,
    paddingBottom: 12,
    paddingLeft: 16,
    paddingRight: 16,
    borderBottomWidth: 1,
    borderBottomColor: '#eeeeee'
  },
  text: {
    fontFamily: 'Metropolis-Regular',
    fontSize: 14
  },
  link: {
    color: Colors.LbryGreen,
    fontFamily: 'Metropolis-Regular',
    fontSize: 14
  },
  smallText: {
    fontFamily: 'Metropolis-Regular',
    fontSize: 12
  },
  balanceCard: {
    marginTop: 16,
    marginLeft: 16,
    marginRight: 16
  },
  balanceBackground: {
    position: 'absolute',
    alignSelf: 'stretch',
    width: '100%',
    height: '100%',
  },
  balanceTitle: {
    color: '#ffffff',
    fontFamily: 'Metropolis-Bold',
    fontSize: 18,
    marginLeft: 16,
    marginTop: 16
  },
  balanceCaption: {
    color: '#caedB9',
    fontFamily: 'Metropolis-Medium',
    fontSize: 14,
    marginLeft: 16,
    marginTop: 8,
    marginBottom: 96
  },
  balance: {
    color: '#ffffff',
    fontFamily: 'Metropolis-Bold',
    fontSize: 36,
    marginLeft: 16,
    marginBottom: 16
  },
  infoText: {
    color: '#aaaaaa',
    fontFamily: 'Metropolis-Regular',
    fontSize: 14,
    padding: 16,
    textAlign: 'center'
  },
  input: {
    fontFamily: 'Metropolis-Regular',
    fontSize: 14
  },
  amountInput: {
    alignSelf: 'flex-start',
    width: 150
  },
  addressInput: {
    width: '80%'
  },
  warning: {
    backgroundColor: '#ffbb00',
    padding: 16,
    margin: 16
  },
  warningText: {
    color: '#ffffff',
    fontFamily: 'Metropolis-Regular',
    fontSize: 16,
    lineHeight: 30
  },
  understand: {
    marginLeft: 16
  },
  currency: {
    alignSelf: 'flex-start',
    marginTop: 17
  },
  sendButton: {
    marginTop: 8
  },
  bottomMarginSmall: {
    marginBottom: 8
  },
  bottomMarginMedium: {
    marginBottom: 16
  },
  bottomMarginLarge: {
    marginBottom: 24
  }
});

export default walletStyle;
