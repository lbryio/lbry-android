import React from 'react';
import { normalizeURI, parseURI } from 'lbry-redux';
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
    const subscriptionLabel = isSubscribed ? null : __('Subscribe');
    const { claimName } = parseURI(uri);

    return (
      <Button
        style={styles}
        theme={"light"}
        icon={isSubscribed ? "heart-broken" : "heart"}
        iconColor={iconColor}
        solid={isSubscribed ? false : true}
        text={subscriptionLabel}
        onPress={() => {
          subscriptionHandler({
            channelName: claimName,
            uri: normalizeURI(uri),
          });
        }} />
    );
  }
}

export default SubscribeButton;
