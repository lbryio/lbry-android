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
    fontSize: 20,
    textAlign: 'center',
    margin: 10,
  },
  categoryName: {
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
    marginTop: 8,
    fontSize: 16,
    fontWeight: 'bold'
  },
  channelName: {
    fontSize: 14,
    marginTop: 4,
    color: '#c0c0c0',
    fontWeight: 'bold'
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
    fontSize: 12,
    textAlign: 'center',
    color: '#0c604b',
    fontWeight: 'bold'
  },
  drawerHamburger: {
    marginLeft: 8
  }
});

export default discoverStyle;
