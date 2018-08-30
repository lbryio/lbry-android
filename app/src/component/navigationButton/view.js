import React from 'react';
import Icon from 'react-native-vector-icons/FontAwesome5';
import { TouchableOpacity } from 'react-native';


class NavigationButton extends React.PureComponent {
  render() {
    const { iconStyle, name, onPress, size, style } = this.props;

    return (
      <TouchableOpacity onPress={onPress} style={style}>
        <Icon name={name} size={size} style={iconStyle} />
      </TouchableOpacity>
    );
  }
};

export default NavigationButton;
