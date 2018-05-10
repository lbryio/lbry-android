import React from 'react';
import { Text, TouchableOpacity } from 'react-native';
import buttonStyle from '../../styles/button';
import Icon from 'react-native-vector-icons/FontAwesome';

export default class Button extends React.PureComponent {
  render() {
    const {
      disabled,
      style,
      text,
      icon,
      onPress
    } = this.props;
    
    let styles = [buttonStyle.button, buttonStyle.row];
    if (style) {
      if (style.length) {
        styles = styles.concat(style);
      } else {
        styles.push(style);
      }
    }
    
    if (disabled) {
      styles.push(buttonStyle.disabled);
    }
    
    const textStyles = [buttonStyle.text];
    if (icon && icon.trim().length > 0) {
      textStyles.push(buttonStyle.textWithIcon);
    }
    
    return (
      <TouchableOpacity disabled={disabled} style={styles} onPress={onPress}>
        {icon && <Icon name={icon} size={18} color='#ffffff' class={buttonStyle.icon} /> }
        {text && (text.trim().length > 0) && <Text style={textStyles}>{text}</Text>}
      </TouchableOpacity>
    );
  }
};
