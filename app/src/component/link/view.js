import React from 'react';
import { Linking, Text, TouchableOpacity } from 'react-native';

export default class Link extends React.PureComponent {
  handlePress = () => {
    const { error, href, navigation, notify } = this.props;
    
    if (navigation && href.startsWith('#')) {
      navigation.navigate(href.substring(1));
    } else {
      Linking.openURL(href).catch(err => notify({
        message: error,
        displayType: ['toast']
      }));
    }
  }
  
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
      <TouchableOpacity onPress={onPress ? onPress : this.handlePress}>
        <Text style={styles}>{text}</Text>
      </TouchableOpacity>
    );
  }
};
