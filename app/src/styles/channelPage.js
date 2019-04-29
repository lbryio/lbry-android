import { StyleSheet } from 'react-native';
import Colors from './colors';

const channelPageStyle = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    backgroundColor: Colors.PageBackground
  },
  content: {
    flex: 1
  },
  viewContainer: {
    flex: 1,
    marginTop: 60
  },
  fileList: {
    flex: 1,
    paddingTop: 30
  },
  fileListContent: {
    paddingBottom: 16
  },
  title: {
    color: Colors.LbryGreen,
    fontFamily: 'Inter-UI-SemiBold',
    fontSize: 30,
    margin: 16
  },
   busyContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    flexDirection: 'row'
  },
  infoText: {
    fontFamily: 'Inter-UI-Regular',
    fontSize: 20,
    textAlign: 'center',
    marginLeft: 10
  },
  pageButtons: {
    width: '100%',
    flexDirection: 'row',
    justifyContent: 'space-between',
    position: 'absolute',
    bottom: 16,
    paddingLeft: 16,
    paddingRight: 16
  },
  button: {
    backgroundColor: Colors.LbryGreen,
    paddingLeft: 16,
    paddingRight: 16
  },
  nextButton: {
    alignSelf: 'flex-end'
  },
  channelHeader: {
    position: 'absolute',
    left: 120,
    bottom: 4
  },
  channelName: {
    color: Colors.White,
    fontFamily: 'Inter-UI-Regular',
    fontSize: 18
  },
  subscribeButton: {
    alignSelf: 'flex-start',
    backgroundColor: Colors.White,
    paddingLeft: 16,
    paddingRight: 16,
    position: 'absolute',
    right: 8,
    bottom: -88,
    zIndex: 100
  },
  cover: {
    width: '100%',
    height: '20%',
  },
  coverImage: {
    width: '100%',
    height: '100%'
  },
  tabBar: {
    height: 45,
    backgroundColor: Colors.LbryGreen,
    flexDirection: 'row',
    justifyContent: 'flex-end'
  },
  tabTitle: {
    fontFamily: 'Inter-UI-SemiBold',
    fontSize: 14,
    color: Colors.White,
  },
  tab: {
    width: '30%',
    alignItems: 'center',
    justifyContent: 'center'
  },
  activeTabHint: {
    position: 'absolute',
    bottom: 0,
    backgroundColor: Colors.White,
    height: 3,
    width: '100%'
  },
  contentTab: {
    flex: 1
  },
  aboutTab: {
    flex: 1
  },
  aboutScroll: {
    flex: 1,
  },
  aboutItem: {
    marginBottom: 24
  },
  aboutScrollContent: {
    padding: 24
  },
  aboutTitle: {
    fontFamily: 'Inter-UI-SemiBold',
    fontSize: 16,
    lineHeight: 24
  },
  aboutText: {
    fontFamily: 'Inter-UI-Regular',
    fontSize: 16,
    lineHeight: 24
  },
  avatarImageContainer: {
    width: 80,
    height: 80,
    borderRadius: 160,
    position: 'absolute',
    overflow: 'hidden',
    left: 24,
    bottom: -40,
    zIndex: 100
  },
  avatarImage: {
    width: '100%',
    height: '100%',
  }
});

export default channelPageStyle;
