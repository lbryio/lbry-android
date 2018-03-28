import { StyleSheet } from 'react-native';

const discoverStyle = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
  },
  scrollContainer: {
    flex: 1
  },
  title: {
    fontFamily: 'Metropolis-Regular',
    fontSize: 20,
    textAlign: 'center',
    margin: 10,
  },
  categoryName: {
    fontFamily: 'Metropolis-Regular',
    fontSize: 20,
    marginLeft: 24,
    marginTop: 16,
    marginBottom: 16,
    color: '#40b89a'
  },
  fileItem: {
    marginLeft: 24,
    marginRight: 24,
    marginBottom: 48
  },
  fileItemName: {
    fontFamily: 'Metropolis-Bold',
    marginTop: 8,
    fontSize: 16
  },
  channelName: {
    fontFamily: 'Metropolis-SemiBold',
    fontSize: 14,
    marginTop: 4,
    color: '#c0c0c0'
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
    marginLeft: 8
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
