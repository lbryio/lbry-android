import { StyleSheet } from 'react-native';
import Colors from './colors';

const tagStyle = StyleSheet.create({
  tag: {
    marginRight: 4,
    marginBottom: 4,
  },
  tagSearchInput: {
    fontFamily: 'Inter-UI-Regular',
    fontSize: 16,
  },
  content: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    paddingLeft: 8,
    paddingTop: 4,
    paddingBottom: 4,
  },
  icon: {
    marginRight: 8,
  },
  text: {
    fontFamily: 'Inter-UI-Regular',
    fontSize: 14,
    marginRight: 8,
  },
  tagResultsList: {
    flexDirection: 'row',
    flexWrap: 'wrap',
  },
});

export default tagStyle;
