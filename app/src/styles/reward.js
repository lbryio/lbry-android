import { StyleSheet } from 'react-native';
import Colors from './colors';

const rewardStyle = StyleSheet.create({
  container: {
    flex: 1
  },
  row: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center'
  },
  actionButton: {
    backgroundColor: Colors.LbryGreen,
    alignSelf: 'flex-start',
    paddingTop: 9,
    paddingBottom: 9,
    paddingLeft: 24,
    paddingRight: 24
  },
  busyContainer: {
    flex: 1,
    marginTop: 32,
    marginBottom: 16,
    alignItems: 'center',
    justifyContent: 'center',
    flexDirection: 'row'
  },
  rewardsContainer: {
    flex: 1
  },
  scrollContentContainer: {
    paddingBottom: 16
  },
  card: {
    backgroundColor: Colors.White,
    marginTop: 16,
    marginLeft: 16,
    marginRight: 16,
    padding: 16
  },
  rewardCard: {
    backgroundColor: Colors.White,
    marginTop: 16,
    marginLeft: 16,
    marginRight: 16,
    paddingTop: 16,
    paddingBottom: 16
  },
  text: {
    fontFamily: 'Metropolis-Regular',
    fontSize: 16,
    lineHeight: 24
  },
  infoText: {
    fontFamily: 'Metropolis-Regular',
    fontSize: 18,
    marginLeft: 12
  },
  title: {
    fontFamily: 'Metropolis-Regular',
    fontSize: 22,
    marginBottom: 6,
    color: Colors.LbryGreen
  },
  subtitle: {
    fontFamily: 'Metropolis-Regular',
    fontSize: 18,
    marginBottom: 6,
    color: Colors.LbryGreen
  },
  subcardText: {
    fontFamily: 'Metropolis-Regular',
    fontSize: 15,
    lineHeight: 20,
    marginLeft: 2,
    marginRight: 2
  },
  subcardTextInput: {
    fontFamily: 'Metropolis-Regular',
    fontSize: 16,
    marginTop: 2,
    marginBottom: 2
  },
  topMarginSmall: {
    marginTop: 8
  },
  topMarginMedium: {
    marginTop: 16
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
  link: {
    color: Colors.LbryGreen,
    fontFamily: 'Metropolis-Regular',
    fontSize: 14,
  },
  textLink: {
    color: Colors.LbryGreen
  },
  leftCol: {
    width: '15%',
    alignItems: 'center',
    paddingLeft: 6
  },
  midCol: {
    width: '65%'
  },
  rightCol: {
    width: '18%',
    alignItems: 'center'
  },
  rewardAmount: {
    fontFamily: 'Metropolis-Regular',
    fontSize: 26,
    textAlign: 'center'
  },
  rewardCurrency: {
    fontFamily: 'Metropolis-Regular'
  },
  rewardTitle: {
    fontFamily: 'Metropolis-Regular',
    fontSize: 16,
    color: Colors.LbryGreen,
    marginBottom: 4,
  },
  rewardDescription: {
    fontFamily: 'Metropolis-Regular',
    fontSize: 14,
    lineHeight: 18,
    marginBottom: 4
  },
  claimed: {
    color: Colors.LbryGreen,
  },
  disabled: {
    color: Colors.LightGrey
  },
  subcard: {
    borderTopColor: Colors.VeryLightGrey,
    borderTopWidth: 1,
    paddingTop: 16,
    paddingLeft: 8,
    paddingRight: 8,
    marginTop: 16,
    marginLeft: -8,
    marginRight: -8
  },
  summaryContainer: {
    backgroundColor: Colors.LbryGreen,
    padding: 12
  },
  summaryText: {
    color: Colors.White,
    fontFamily: 'Metropolis-Regular',
    fontSize: 14,
    lineHeight: 22
  },
  phoneVerificationContainer: {
    paddingLeft: 4,
    paddingRight: 4
  },
  phoneInput: {
    marginLeft: 8
  },
  phoneInputText: {
    fontFamily: 'Metropolis-Regular',
    fontSize: 16,
    letterSpacing: 1.3
  },
  verifyingText: {
    fontFamily: 'Metropolis-Regular',
    fontSize: 14,
    marginLeft: 12,
    alignSelf: 'flex-start'
  },
  verificationCodeInput: {
    fontFamily: 'Metropolis-Regular',
    fontSize: 24,
    letterSpacing: 12
  },
  loading: {
    alignSelf: 'flex-start'
  },
  smsPermissionContainer: {
    marginBottom: 32
  }
});

export default rewardStyle;
