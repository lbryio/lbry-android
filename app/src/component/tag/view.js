import React from 'react';
import { Text, TouchableOpacity, View } from 'react-native';
import tagStyle from 'styles/tag';
import Colors from 'styles/colors';
import Icon from 'react-native-vector-icons/FontAwesome5';

export default class Tag extends React.PureComponent {
  onPressDefault = () => {
    const { name, navigation, type, onAddPress, onRemovePress } = this.props;
    if ('add' === type) {
      if (onAddPress) {
        onAddPress(name);
      }
      return;
    }
    if ('remove' === type) {
      if (onRemovePress) {
        onRemovePress(name);
      }
      return;
    }

    if (navigation) {
      // navigate to tag page
    }
  };

  render() {
    const { name, onPress, style, type } = this.props;

    let styles = [];
    if (style) {
      if (style.length) {
        styles = styles.concat(style);
      } else {
        styles.push(style);
      }
    }

    styles.push({
      backgroundColor: Colors.TagGreen,
      borderRadius: 8,
      marginBottom: 4,
    });

    return (
      <TouchableOpacity style={styles} onPress={onPress || this.onPressDefault}>
        <View style={tagStyle.content}>
          <Text style={tagStyle.text}>{name}</Text>
          {type && <Icon style={tagStyle.icon} name={type === 'add' ? 'plus' : 'times'} size={8} />}
        </View>
      </TouchableOpacity>
    );
  }
}
