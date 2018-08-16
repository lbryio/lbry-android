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
  link: {
    color: Colors.LbryGreen,
    fontFamily: 'Metropolis-Regular',
    fontSize: 14,
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
  }
});

export default rewardStyle;
