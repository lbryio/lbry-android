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

    return (
      <TouchableOpacity disabled={disabled} style={styles} onPress={onPress} onLayout={onLayout}>
        {icon && <Icon name={icon} size={18} color={'light' === theme ? Colors.DarkGrey : Colors.White} />}
        {text && (text.trim().length > 0) && <Text style={textStyles}>{text}</Text>}
      </TouchableOpacity>
    );
  }
};
