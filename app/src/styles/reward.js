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
  busyContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
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
    fontFamily: 'Metropolis-SemiBold',
    fontSize: 20,
    marginBottom: 8,
    color: Colors.LbryGreen
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
  leftCol: {
    width: '10%'
  },
  midCol: {
    width: '65%'
  },
  rightCol: {
    width: '15%',
    alignItems: 'center',
    justifyContent: 'center'
  },
  rewardAmount: {
    fontFamily: 'Metropolis-Regular',
    fontSize: 24,
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
    lineHeight: 18
  },
  claimed: {
    color: Colors.LbryGreen,
  },
  disabled: {
    color: Colors.LightGrey
  }
});

export default rewardStyle;
