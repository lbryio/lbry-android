import React from 'react';
import { Text, TouchableOpacity } from 'react-native';

export default class Link extends React.PureComponent {
  render() {
    const {
      onPress,
      style,
      text
    } = this.props;
    
    let styles = [];
    if (style.length) {
      styles = styles.concat(style);
    } else {
      styles.push(style);
    }
    
    return (
      <TouchableOpacity onPress={onPress}>
        <Text style={styles}>{text}</Text>
      </TouchableOpacity>
    );
  }
};
