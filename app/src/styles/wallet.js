import { StyleSheet } from 'react-native';
import Colors from './colors';

const walletStyle = StyleSheet.create({
  row: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center'
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
  card: {
    backgroundColor: '#ffffff',
    margin: 16,
    padding: 16
  },
  title: {
    fontFamily: 'Metropolis-Bold',
    fontSize: 20,
    marginBottom: 24
  },
  text: {
    fontFamily: 'Metropolis-Regular',
    fontSize: 14
  },
  smallText: {
    fontFamily: 'Metropolis-Regular',
    fontSize: 12
  },
  balanceCard: {
    backgroundColor: '#cc0000',
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
