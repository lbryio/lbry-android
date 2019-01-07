import React from 'react';
import { Text, TouchableOpacity } from 'react-native';
import buttonStyle from '../../styles/button';
import Colors from '../../styles/colors';
import Icon from 'react-native-vector-icons/FontAwesome5';

export default class Button extends React.PureComponent {
  render() {
    const {
      disabled,
      style,
      text,
      icon,
      iconColor,
      solid,
      theme,
      onPress,
      onLayout
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

    if (theme === 'light') {
      textStyles.push(buttonStyle.textDark);
    } else {
      // Dark background, default
      textStyles.push(buttonStyle.textLight);
    }

    let renderIcon = (<Icon name={icon} size={18} color={iconColor ? iconColor : ('light' === theme ? Colors.DarkGrey : Colors.White)} />);
    if (solid) {
      renderIcon = (<Icon name={icon} size={18} color={iconColor ? iconColor : ('light' === theme ? Colors.DarkGrey : Colors.White)} solid />);
    }

    return (
      <TouchableOpacity disabled={disabled} style={styles} onPress={onPress} onLayout={onLayout}>
        {icon && renderIcon}
        {text && (text.trim().length > 0) && <Text style={textStyles}>{text}</Text>}
      </TouchableOpacity>
    );
  }
};
