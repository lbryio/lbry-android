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
    color: '#888888'
  },
  filePriceContainer: {
    backgroundColor: '#60f9d6',
    justifyContent: 'center',
    position: 'absolute',
    right: 16,
    top: 16,
    width: 64,
    height: 24
  },
  filePriceText: {
    fontSize: 11,
    textAlign: 'center'
  }
});

export default discoverStyle;
