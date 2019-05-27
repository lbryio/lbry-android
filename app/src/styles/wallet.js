import { StyleSheet } from 'react-native';
import Colors from './colors';

const walletStyle = StyleSheet.create({
  container: {
    backgroundColor: Colors.PageBackground
  },
  scrollContainer: {
    marginTop: 60
  },
  row: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center'
  },
  amountRow: {
    flexDirection: 'row'
  },
  address: {
    fontFamily: 'Inter-UI-Regular',
    borderWidth: 1,
    borderStyle: 'dashed',
    borderColor: '#cccccc',
    backgroundColor: '#f9f9f9',
    padding: 8,
    width: '85%'
  },
  button: {
    backgroundColor: Colors.LbryGreen,
    alignSelf: 'flex-start'
  },
  enrollButton: {
    backgroundColor: Colors.White,
    alignSelf: 'flex-start'
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
  warningCard: {
    backgroundColor: Colors.Orange,
    padding: 16,
    marginLeft: 16,
    marginTop: 16,
    marginRight: 16
  },
  transactionsCard: {
    backgroundColor: '#ffffff',
    margin: 16
  },
  title: {
    fontFamily: 'Inter-UI-SemiBold',
    fontSize: 20,
    marginBottom: 24
  },
  transactionsTitle: {
    fontFamily: 'Inter-UI-SemiBold',
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
    fontFamily: 'Inter-UI-Regular',
    fontSize: 14
  },
  link: {
    color: Colors.LbryGreen,
    fontFamily: 'Inter-UI-Regular',
    fontSize: 14
  },
  smallText: {
    fontFamily: 'Inter-UI-Regular',
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
    fontFamily: 'Inter-UI-SemiBold',
    fontSize: 20,
    marginLeft: 16,
    marginTop: 16
  },
  balanceCaption: {
    color: '#caedB9',
    fontFamily: 'Inter-UI-Medium',
    fontSize: 14,
    marginLeft: 16,
    marginTop: 8,
    marginBottom: 96
  },
  balance: {
    color: '#ffffff',
    fontFamily: 'Inter-UI-Bold',
    fontSize: 36,
    marginLeft: 16,
    marginBottom: 16
  },
  infoText: {
    color: '#aaaaaa',
    fontFamily: 'Inter-UI-Regular',
    fontSize: 14,
    padding: 16,
    textAlign: 'center'
  },
  input: {
    fontFamily: 'Inter-UI-Regular',
    fontSize: 14
  },
  amountInput: {
    alignSelf: 'flex-start',
    width: 100,
    fontSize: 16,
    letterSpacing: 1
  },
  addressInput: {
    flex: 1,
    fontSize: 16,
    letterSpacing: 1.5
  },
  warning: {
    backgroundColor: Colors.Orange,
    padding: 16,
    margin: 16,
    marginTop: 76
  },
  warningParagraph: {
    color: Colors.White,
    fontFamily: 'Inter-UI-Regular',
    fontSize: 16,
    lineHeight: 24,
    marginBottom: 16
  },
  warningText: {
    color: Colors.White,
    fontFamily: 'Inter-UI-Regular',
    fontSize: 16,
    lineHeight: 24,
    marginBottom: 8
  },
  understand: {
    marginLeft: 16,
    padding: 12,
    paddingLeft: 18,
    paddingRight: 18
  },
  currency: {
    alignSelf: 'flex-start',
    fontSize: 12,
    marginTop: 16,
    marginLeft: 4
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
  },
  transactionHistoryScroll: {
    marginTop: 60
  },
  rewardDriverCard: {
    padding: 16,
    backgroundColor: Colors.LbryGreen,
    marginLeft: 16,
    marginTop: 16,
    marginRight: 16
  },
  rewardDriverText: {
    color: Colors.White,
    fontFamily: 'Inter-UI-Regular',
    fontSize: 14,
    lineHeight: 16
  },
  syncDriverCard: {
    padding: 16,
    backgroundColor: Colors.LbryGreen,
    marginLeft: 16,
    marginTop: 16,
    marginRight: 16
  },
  syncDriverTitle:  {
    color: Colors.White,
    fontFamily: 'Inter-UI-SemiBold',
    fontSize: 20
  },
  syncDriverText: {
    color: Colors.White,
    fontFamily: 'Inter-UI-Regular',
    fontSize: 14,
  },
  actionRow: {
    marginTop: 20,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between'
  },
  syncDriverRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between'
  }
});

export default walletStyle;
