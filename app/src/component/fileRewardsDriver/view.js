import React from 'react';
import { Text, TouchableOpacity } from 'react-native';
import Icon from 'react-native-vector-icons/FontAwesome5';
import filePageStyle from 'styles/filePage';

class FileRewardsDriver extends React.PureComponent<Props> {
  render() {
    const { navigation } = this.props;

    return (
      <TouchableOpacity style={filePageStyle.rewardDriverCard} onPress={() => navigation.navigate('Rewards')}>
        <Icon name="award" size={16} style={filePageStyle.rewardIcon} />
        <Text style={filePageStyle.rewardDriverText}>Earn some credits to access this content.</Text>
      </TouchableOpacity>
    );
  }
}

export default FileRewardsDriver;
