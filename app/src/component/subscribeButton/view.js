import React from 'react';
import { parseURI } from 'lbry-redux';
import { NativeModules, Text, View, TouchableOpacity } from 'react-native';
import Button from '../button';
import Colors from '../../styles/colors';

class SubscribeButton extends React.PureComponent {
  render() {
    const {
      uri,
      isSubscribed,
      doChannelSubscribe,
      doChannelUnsubscribe,
      style
    } = this.props;

    let styles = [];
    if (style) {
      if (style.length) {
        styles = styles.concat(style);
      } else {
        styles.push(style);
      }
    }

    const iconColor = isSubscribed ? null : Colors.Red;
    const subscriptionHandler = isSubscribed ? doChannelUnsubscribe : doChannelSubscribe;
    const subscriptionLabel = isSubscribed ? __('Unsubscribe') : __('Subscribe');
    const { claimName } = parseURI(uri);

    return (
      <Button
        style={styles}
        theme={"light"}
        icon={"heart"}
        iconColor={iconColor}
        solid={isSubscribed ? false : true}
        text={subscriptionLabel}
        onPress={() => {
          subscriptionHandler({
            channelName: claimName,
            uri,
          });
        }} />
    );
  }
}

export default SubscribeButton;
