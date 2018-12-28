import React from 'react';
import { parseURI } from 'lbry-redux';
import { NativeModules, Text, View, TouchableOpacity } from 'react-native';
import Button from '../button';

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

    const subscriptionHandler = isSubscribed ? doChannelUnsubscribe : doChannelSubscribe;
    const subscriptionLabel = isSubscribed ? __('Unsubscribe') : __('Subscribe');
    const { claimName } = parseURI(uri);

    return (
      <Button
        style={styles}
        theme={"light"}
        icon={"heart"}
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
