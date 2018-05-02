import React from 'react';
import { Text, TouchableOpacity } from 'react-native';
import buttonStyle from '../../styles/button';
import Icon from 'react-native-vector-icons/FontAwesome';

export default class Button extends React.PureComponent<Props> {
  render() {
    const {
      disabled,
      style,
      text,
      icon,
      onPress
    } = this.props;
    
    let styles = [buttonStyle.button, buttonStyle.row];
    if (style.length) {
      styles = styles.concat(style);
    } else {
      styles.push(style);
    }
    
    return (
      <TouchableOpacity disabled={disabled} style={styles} onPress={onPress}>
        {icon && <Icon name={icon} size={18} color='#ffffff' class={buttonStyle.icon} /> }
        {text && (text.trim().length > 0) && <Text style={buttonStyle.text}>{text}</Text>}
      </TouchableOpacity>
    );
  }
};
