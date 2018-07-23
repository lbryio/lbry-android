// @flow
import React from 'react';
import { Text, View } from 'react-native';
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
    const { reward } = props;
    const claimed = !!reward.transaction_id;

    return (
      <View style={[rewardStyle.card, rewardStyle.row]}>
        <View style={rewardStyle.leftCol}>

        </View>
        <View style={rewardStyle.midCol}>
          <View style={rewardStyle.rewardTitle}>{reward.reward_title}</View>
          <View style={rewardStyle.rewardDescription}>{reward.reward_description}</View>
        </View>
        <View style={rewardStyle.rightCol}>
          <View style={rewardStyle.rewardAmount}>{reward.reward_amount}</View>
        </View>
      </View>
    );
  }
};

export default RewardCard;
