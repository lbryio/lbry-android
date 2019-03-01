import { Platform, StyleSheet } from 'react-native';

const STATUSBAR_HEIGHT = Platform.OS === 'ios' ? 20 : 0;
export const TITLE_OFFSET = Platform.OS === 'ios' ? 70 : 56;

let platformContainerStyles;
if (Platform.OS === 'ios') {
  platformContainerStyles = {
    borderBottomWidth: StyleSheet.hairlineWidth,
    borderBottomColor: '#A7A7AA',
  };
} else {
  platformContainerStyles = {
    shadowColor: 'black',
    shadowOpacity: 0.1,
    shadowRadius: StyleSheet.hairlineWidth,
    shadowOffset: {
      height: StyleSheet.hairlineWidth,
    },
    elevation: 4,
  };
}

const pageHeaderStyle = StyleSheet.create({
  container: {
    backgroundColor: Platform.OS === 'ios' ? '#F7F7F7' : '#FFF',
    ...platformContainerStyles,
  },
  transparentContainer: {
    position: 'absolute',
    top: 0,
    left: 0,
    right: 0,
    ...platformContainerStyles,
  },
  backIcon: {
    marginLeft: 16,
    marginRight: 16
  },
  header: {
    ...StyleSheet.absoluteFillObject,
    flexDirection: 'row',
  },
  titleText: {
    fontFamily: 'Inter-UI-Regular',
    fontSize: Platform.OS === 'ios' ? 17 : 20,
    fontWeight: Platform.OS === 'ios' ? '700' : '500',
    color: 'rgba(0, 0, 0, .9)',
    textAlign: Platform.OS === 'ios' ? 'center' : 'left',
    marginHorizontal: 16,
  },
  title: {
    bottom: 0,
    top: 0,
    left: TITLE_OFFSET,
    right: TITLE_OFFSET,
    position: 'absolute',
    alignItems: 'center',
    flexDirection: 'row',
    justifyContent: Platform.OS === 'ios' ? 'center' : 'flex-start',
  },
  left: {
    left: 0,
    bottom: 0,
    top: 0,
    height: '100%',
    position: 'absolute',
    alignItems: 'center',
    flexDirection: 'row',
  },
  right: {
    right: 0,
    bottom: 0,
    top: 0,
    position: 'absolute',
    flexDirection: 'row',
    alignItems: 'center',
  },
  flexOne: {
    flex: 1,
  }
});

export default pageHeaderStyle;
