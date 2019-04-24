import { StyleSheet } from 'react-native';
import {
  screenWidth,
  horizontalMargin,
  mediaWidth,
  mediaHeight
} from 'styles/discover';
import Colors from 'styles/colors';

const subscriptionsStyle = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: Colors.PageBackground
  },
  subContainer: {
    flex: 1,
  },
  suggestedSubsContainer: {
    flex: 1,
    marginTop: 60
  },
  button: {
    alignSelf: 'flex-start',
    backgroundColor: Colors.LbryGreen,
    paddingLeft: 16,
    paddingRight: 16,
    marginLeft: 16,
    marginBottom: 16
  },
  busyContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    flexDirection: 'row',
    padding: 16
  },
  scrollContainer: {
    flex: 1
  },
  scrollPadding: {
    paddingTop: 24
  },
  infoText: {
    fontFamily: 'Inter-UI-Regular',
    fontSize: 16,
    margin: 16
  },
  suggestedContainer: {
    flex: 1,
  },
  contentContainer: {
    flex: 1,
    marginTop: 16
  },
  contentText: {
    fontFamily: 'Inter-UI-Regular',
    fontSize: 16,
    marginLeft: 24,
    marginRight: 24,
    marginBottom: 8
  },
  fileItem: {
    marginLeft: 24,
    marginRight: 24,
    marginBottom: 24
  },
  compactMainFileItem: {
    marginLeft: 24,
    marginRight: 24
  },
  compactItems: {
    flex: 1,
    marginTop: 6,
    marginLeft: 20,
    marginRight: 24,
    marginBottom: 24,
    height: 80,
  },
  compactFileItem: {
    width: (screenWidth - horizontalMargin - (6 * 3)) / 3,
    marginLeft: 6,
    height: '100%'
  },
  compactFileItemMedia: {
    width: (screenWidth - horizontalMargin) / 3,
    height: '100%'
  },
  fileItemMedia: {
    width: mediaWidth,
    height: mediaHeight,
    alignItems: 'center',
    justifyContent: 'center'
  },
  fileItemName: {
    fontFamily: 'Inter-UI-Bold',
    marginTop: 8,
    fontSize: 18
  },
  channelTitle: {
    fontFamily: 'Inter-UI-SemiBold',
    fontSize: 20,
    marginLeft: 24,
    marginTop: 16,
    marginBottom: 16,
    color: Colors.LbryGreen
  },
  titleRow: {
    flex: 1,
    flexDirection: 'row',
    justifyContent: 'space-between'
  },
  subscribeButton: {
    alignSelf: 'flex-start',
    marginRight: 24,
    marginTop: 8,
    backgroundColor: Colors.White,
    paddingLeft: 16,
    paddingRight: 16,
  },
  viewModeRow: {
    flexDirection: 'row',
    marginLeft: 24,
    marginRight: 24,
    marginTop: 84
  },
  viewModeLink: {
    marginRight: 24,
    fontSize: 18,
    color: Colors.LbryGreen
  },
  inactiveMode: {
    fontFamily: 'Inter-UI-Regular'
  },
  activeMode: {
    fontFamily: 'Inter-UI-SemiBold'
  }
});

export default subscriptionsStyle;
