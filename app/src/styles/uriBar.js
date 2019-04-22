import { StyleSheet } from 'react-native';
import { TITLE_OFFSET } from 'styles/pageHeader';
import Colors from './colors';

const uriBarStyle = StyleSheet.create({
  uriContainer: {
    flexDirection: 'row',
    backgroundColor: Colors.White,
    padding: 8,
    alignSelf: 'flex-start',
    height: 60,
    width: '100%',
    shadowColor: Colors.Black,
    shadowOpacity: 0.1,
    shadowRadius: StyleSheet.hairlineWidth,
    shadowOffset: {
      height: StyleSheet.hairlineWidth,
    },
    elevation: 4
  },
  uriText: {
    backgroundColor: Colors.White,
    borderWidth: 1,
    borderColor: Colors.LightGrey,
    padding: 8,
    borderRadius: 4,
    fontFamily: 'Inter-UI-Regular',
    fontSize: 16,
    flex: 17
  },
  overlay: {
    position: 'absolute',
    backgroundColor: 'transparent',
    top: 0,
    width: '100%',
    zIndex: 200,
    elevation: 16
  },
  inFocus: {
    height: '100%'
  },
  suggestions: {
    backgroundColor: 'white',
    flex: 1
  },
  item: {
    flexDirection: 'row',
    alignItems: 'center',
    padding: 12,
    paddingTop: 8,
    paddingBottom: 8
  },
  itemContent: {
    marginLeft: 12,
    marginRight: 12,
    flex: 1
  },
  itemText: {
    fontFamily: 'Inter-UI-Regular',
    fontSize: 16,
  },
  itemDesc: {
    fontFamily: 'Inter-UI-Regular',
    fontSize: 14,
    color: Colors.UriDescBlue
  },
  drawerMenuButton: {
    height: '100%',
    justifyContent: 'center',
    flex: 3
  },
});

export default uriBarStyle;
