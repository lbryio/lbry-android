import React from 'react';
import { Text, TouchableOpacity } from 'react-native';
import Colors from 'styles/colors';
import Icon from 'react-native-vector-icons/FontAwesome5';
import publishStyle from 'styles/publish';

class PublishRewadsDriver extends React.PureComponent<Props> {
  render() {
    const { navigation } = this.props;

    return (
      <TouchableOpacity style={publishStyle.rewardDriverCard} onPress={() => navigation.navigate('Rewards')}>
        <Icon name="award" size={16} style={publishStyle.rewardIcon} />
        <Text style={publishStyle.rewardDriverText}>Earn some credits to be able to publish your content.</Text>
      </TouchableOpacity>
    );
  }
}

export default PublishRewadsDriver;
