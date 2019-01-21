import React from 'react';
import { parseURI } from 'lbry-redux';
import { NativeModules, Text, View, TouchableOpacity } from 'react-native';
import Button from 'component/button';
import Colors from 'styles/colors';

class SubscribeNotificationButton extends React.PureComponent {
  render() {
    const {
      uri,
      name,
      doChannelSubscriptionEnableNotifications,
      doChannelSubscriptionDisableNotifications,
      doToast,
      enabledChannelNotifications,
      isSubscribed,
      style
    } = this.props;

    if (!isSubscribed) {
        return null;
    }

    let styles = [];
    if (style) {
      if (style.length) {
        styles = styles.concat(style);
      } else {
        styles.push(style);
      }
    }

    const shouldNotify = enabledChannelNotifications.indexOf(name) > -1;
    const { claimName } = parseURI(uri);

    return (
      <Button
        style={styles}
        theme={"light"}
        icon={shouldNotify ? "bell-slash" : "bell"}
        solid={true}
        onPress={() => {
          if (shouldNotify) {
            doChannelSubscriptionDisableNotifications(name);
            doToast({ message: 'You will not receive notifications for new content.' });
          } else {
            doChannelSubscriptionEnableNotifications(name);
            doToast({ message: 'You will receive all notifications for new content.' });
          }
        }} />
    );
  }
}

export default SubscribeNotificationButton;
