import { StyleSheet, Dimensions } from 'react-native';

const screenDimension = Dimensions.get('window'); 
const screenWidth = screenDimension.width;

const filePageStyle = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
  },
  pageContainer: {
    flex: 1
  },
  mediaContainer: {
    backgroundColor: '#000000'
  },
  emptyClaimText: {
    textAlign: 'center',
    fontSize: 20,
    marginLeft: 16,
    marginRight: 16
  },
  scrollContainer: {
    flex: 1
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    marginTop: 20,
    marginLeft: 20,
    marginRight: 20,
    marginBottom: 12
  },
  channelName: {
    fontSize: 20,
    fontWeight: 'bold',
    marginLeft: 20,
    marginRight: 20,
    marginBottom: 20,
    color: '#9b9b9b'
  },
  description: {
    fontSize: 16,
    marginLeft: 20,
    marginRight: 20,
    marginBottom: 20,
    color: '#999999'
  },
  thumbnail: {
    width: screenWidth,
    height: 200
  }
});

export default filePageStyle;
