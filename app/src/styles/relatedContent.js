import { StyleSheet } from 'react-native';
import Colors from './colors';

const relatedContentStyle = StyleSheet.create({
  container: {
    flex: 1,
    paddingTop: 16,
    paddingBottom: 16,
    paddingLeft: 16,
    paddingRight: 16,
    borderTopColor: Colors.LighterGrey,
    borderTopWidth: 1
  },
  title: {
    fontFamily: 'Inter-UI-Regular',
    fontSize: 18,
  },
  itemList: {
    flex: 1,
  },
  scrollContainer: {
    flex: 1,
    paddingLeft: 16,
    paddingRight: 16,
    marginTop: 16,
    marginBottom: 60
  },
  scrollPadding: {
    marginTop: -16,
    paddingBottom: 16
  },
  loading: {
    marginTop: 16
  }
});

export default relatedContentStyle;
