// @flow
import React from 'react';
import { Text, TouchableOpacity, View } from 'react-native';
import Icon from 'react-native-vector-icons/FontAwesome5';
import rewardStyle from '../../styles/reward';

type Props = {
  reward: {
    id: string,
    reward_title: string,
    reward_amount: number,
    transaction_id: string,
    created_at: string,
    reward_description: string,
    reward_type: string,
  },
};

class RewardCard extends React.PureComponent<Props> {
  render() {
    const { canClaim, onClaimPress, reward } = this.props;
    const claimed = !!reward.transaction_id;

    return (
      <View style={[rewardStyle.card, rewardStyle.row]}>
        <View style={rewardStyle.leftCol}>
          <TouchableOpacity onPress={() => { if (!claimed && onClaimPress) { onClaimPress(); } }}>
            <Icon name={claimed ? "check-circle" : "circle"}
                  style={claimed ? rewardStyle.claimed : (canClaim ? rewardStyle.unclaimed : rewardStyle.disabled)}
                  size={20} />
          </TouchableOpacity>
        </View>
        <View style={rewardStyle.midCol}>
          <Text style={rewardStyle.rewardTitle}>{reward.reward_title}</Text>
          <Text style={rewardStyle.rewardDescription}>{reward.reward_description}</Text>
        </View>
        <View style={rewardStyle.rightCol}>
          <Text style={rewardStyle.rewardAmount}>{reward.reward_amount}</Text>
          <Text style={rewardStyle.rewardCurrency}>LBC</Text>
        </View>
      </View>
    );
  }
};

export default RewardCard;
