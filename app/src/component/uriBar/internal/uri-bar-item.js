// @flow
import React from 'react';
import { SEARCH_TYPES, normalizeURI } from 'lbry-redux';
import { Text, TouchableOpacity, View } from 'react-native';
import Icon from 'react-native-vector-icons/FontAwesome5';
import uriBarStyle from '../../../styles/uriBar';

class UriBarItem extends React.PureComponent {
  render() {
    const { item, onPress } = this.props;
    const { shorthand, type, value } = item;

    let icon;
    switch (type) {
      case SEARCH_TYPES.CHANNEL:
        icon = <Icon name="at" size={18} />
        break;

      case SEARCH_TYPES.SEARCH:
        icon = <Icon name="search" size={18} />
        break;

      case SEARCH_TYPES.FILE:
      default:
        icon = <Icon name="file" size={18} />
        break;
    }

    return (
      <TouchableOpacity style={uriBarStyle.item} onPress={onPress}>
        {icon}
        <View style={uriBarStyle.itemContent}>
          <Text style={uriBarStyle.itemText} numberOfLines={1}>{shorthand || value} - {type === SEARCH_TYPES.SEARCH ? 'Search' : value}</Text>
          <Text style={uriBarStyle.itemDesc} numberOfLines={1}>
            {type === SEARCH_TYPES.SEARCH && `Search for '${value}'`}
            {type === SEARCH_TYPES.CHANNEL && `View the @${shorthand} channel`}
            {type === SEARCH_TYPES.FILE && `View content at ${value}`}
          </Text>
        </View>
      </TouchableOpacity>
    )
  }
}

export default UriBarItem;
