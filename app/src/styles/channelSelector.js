import { StyleSheet } from 'react-native';
import Colors from './colors';

const channelSelectorStyle = StyleSheet.create({
  container: {
    flex: 1,
  },
  channelPicker: {
    fontFamily: 'Inter-UI-Regular',
    fontSize: 16,
    height: 52,
    width: '100%',
  },
  bidRow: {
    flex: 1,
    flexDirection: 'row',
    alignItems: 'center',
  },
  label: {
    fontFamily: 'Inter-UI-Regular',
    fontSize: 16,
  },
  channelNameInput: {
    fontFamily: 'Inter-UI-Regular',
    fontSize: 16,
    paddingLeft: 20,
  },
  bidAmountInput: {
    fontFamily: 'Inter-UI-Regular',
    fontSize: 16,
    marginLeft: 16,
    textAlign: 'right',
    width: 80,
  },
  helpText: {
    fontFamily: 'Inter-UI-Regular',
    fontSize: 12,
  },
  createChannelContainer: {
    flex: 1,
    marginLeft: 8,
    marginRight: 8,
  },
  channelAt: {
    position: 'absolute',
    left: 4,
    top: 13,
    fontFamily: 'Inter-UI-Regular',
    fontSize: 16,
  },
  buttonContainer: {
    flex: 1,
    marginTop: 16,
    justifyContent: 'flex-end',
  },
  buttons: {
    flex: 1,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'flex-end',
  },
  cancelLink: {
    marginRight: 16,
  },
  createButton: {
    backgroundColor: Colors.NextLbryGreen,
  },
});

export default channelSelectorStyle;
