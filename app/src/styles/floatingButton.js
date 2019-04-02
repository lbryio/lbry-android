import { StyleSheet } from 'react-native';
import Colors from './colors';

const floatingButtonStyle = StyleSheet.create({
  view: {
    position: 'absolute',
    zIndex: 100,
    borderRadius: 24,
    padding: 14,
    justifyContent: 'flex-end',
    flexDirection: 'row'
  },
  container: {
    zIndex: 100,
    borderRadius: 24,
    padding: 14,
    paddingLeft: 20,
    paddingRight: 20,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: Colors.LbryGreen,
    shadowColor: 'black',
    shadowOpacity: 0.1,
    shadowRadius: StyleSheet.hairlineWidth,
    shadowOffset: {
      height: StyleSheet.hairlineWidth,
    },
    elevation: 4
  },
  pendingContainer: {
    borderRadius: 24,
    padding: 14,
    paddingLeft: 20,
    paddingRight: 70,
    marginRight: -60,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: Colors.BrighterLbryGreen,
    flexDirection: 'row',
    elevation: 3
  },
  text: {
    color: Colors.White,
    fontFamily: 'Inter-UI-Bold',
    fontSize: 18,
  },
  bottomRight: {
    right: 10,
    bottom: 10
  },
  rewardIcon: {
    color: Colors.White,
    marginRight: 4
  }
});

export default floatingButtonStyle;
