import React from 'react';
import { Text, TouchableOpacity } from 'react-native';
import discoverStyle from '../../styles/discover';

class NsfwOverlay extends React.PureComponent {
  render() {
    return (
      <TouchableOpacity style={discoverStyle.overlay} activeOpacity={0.95} onPress={this.props.onPress}>
        <Text style={discoverStyle.overlayText}>This content is Not Safe For Work. To view adult content, please change your Settings.</Text>
      </TouchableOpacity>
    )
  }
}

export default NsfwOverlay;
