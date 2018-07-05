import { StyleSheet } from 'react-native';
import Colors from './colors';

const discoverStyle = StyleSheet.create({
  container: {
    flex: 1
  },
  scrollContainer: {
    flex: 1,
    marginBottom: 60,
    paddingTop: 12
  },
  trendingContainer: {
    flex: 1,
    marginBottom: 60,
    paddingTop: 30
  },
  busyContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    flexDirection: 'row'
  },
  title: {
    fontFamily: 'Metropolis-Regular',
    fontSize: 20,
    textAlign: 'center',
    marginLeft: 10
  },
  categoryName: {
    fontFamily: 'Metropolis-SemiBold',
    fontSize: 20,
    marginLeft: 24,
    marginTop: 16,
    marginBottom: 16,
    color: Colors.Black
  },
  fileItem: {
    marginLeft: 24,
    marginRight: 24,
    marginBottom: 48
  },
  fileItemName: {
    fontFamily: 'Metropolis-Bold',
    marginTop: 8,
    fontSize: 18
  },
  channelName: {
    fontFamily: 'Metropolis-SemiBold',
    fontSize: 16,
    marginTop: 4,
    color: Colors.LbryGreen
  },
  filePriceContainer: {
    backgroundColor: '#61fcd8',
    justifyContent: 'center',
    position: 'absolute',
    right: 16,
    top: 16,
    width: 56,
    height: 24,
    borderRadius: 4
  },
  filePriceText: {
    fontFamily: 'Metropolis-Bold',
    fontSize: 12,
    textAlign: 'center',
    color: '#0c604b'
  },
  drawerHamburger: {
    marginLeft: 16
  },
  rightHeaderIcon: {
    marginRight: 16
  },
  overlay: {
    flex: 1,
    opacity: 1,
    backgroundColor: '#222222',
    alignItems: 'center',
    justifyContent: 'center',
    padding: 32,
    position: 'absolute',
    left: 0,
    top: 0,
    width: '100%',
    height: '100%'
  },
  overlayText: {
    color: '#ffffff',
    fontSize: 14,
    textAlign: 'center',
    fontFamily: 'Metropolis-Regular'
  }
});

export default discoverStyle;
