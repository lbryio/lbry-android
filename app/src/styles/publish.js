import { StyleSheet } from 'react-native';
import Colors from './colors';

const publishStyle = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: Colors.PageBackground,
  },
  gallerySelector: {
    flex: 1,
    marginTop: 62,
    paddingTop: 2,
    backgroundColor: Colors.DarkGrey
  },
  galleryGrid: {
    flex: 1
  },
  galleryGridImage: {
    width: 134,
    height: 90
  },
  inputText: {
    fontFamily: 'Inter-UI-Regular',
    fontSize: 16
  },
  card: {
    backgroundColor: Colors.White,
    marginTop: 16,
    marginLeft: 16,
    marginRight: 16,
    padding: 16,
  },
  actionButtons: {
    margin: 16,
    flex: 1,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between'
  },
  publishButton: {
    backgroundColor: Colors.LbryGreen,
    alignSelf: 'flex-end'
  },
  cardTitle: {
    fontFamily: 'Inter-UI-Regular',
    fontSize: 20,
    marginBottom: 8
  },
  actionsView: {
    flexDirection: 'row',
    width: '100%',
    height: 240,
  },
  record: {
    backgroundColor: Colors.Black,
    flex: 0.5,
    justifyContent: 'center',
    alignItems: 'center'
  },
  subActions: {
    flex: 0.5,
    borderLeftWidth: 2,
    borderLeftColor: Colors.DarkerGrey
  },
  actionText: {
    color: Colors.White,
    fontFamily: 'Inter-UI-Regular',
    fontSize: 14,
    marginTop: 8
  },
  photo: {
    backgroundColor: Colors.Black,
    height: 120,
    justifyContent: 'center',
    alignItems: 'center'
  },
  upload: {
    backgroundColor: Colors.Black,
    height: 120,
    borderTopWidth: 2,
    borderTopColor: Colors.DarkerGrey,
    justifyContent: 'center',
    alignItems: 'center'
  },
  publishDetails: {
    marginTop: 60
  },
  mainThumbnailContainer: {
    backgroundColor: Colors.Black,
    width: '100%',
    height: 240
  },
  mainThumbnail: {
    height: 240
  },
  inputRow: {
    flex: 1,
    flexDirection: 'row',
    alignItems: 'center'
  },
  priceInput: {
    width: 80,
    fontFamily: 'Inter-UI-Regular',
    fontSize: 16
  },
  currency: {
    fontFamily: 'Inter-UI-Regular'
  },
  cardRow: {
    flex: 1,
    flexDirection: 'row',
    alignItems: 'center'
  },
  switchRow: {
    flex: 1,
    flexDirection: 'row',
    alignItems: 'center',
    height: 52
  },
  switchText: {
    marginLeft: 6,
    fontSize: 16
  },
  channelPicker: {
    height: 52,
    width: 160
  },
  loadingView: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center'
  }
});

export default publishStyle;
