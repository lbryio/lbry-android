import React from 'react';
import { NativeModules, Text, TouchableOpacity } from 'react-native';
import rewardStyle from '../../styles/reward';

class RewardSummary extends React.Component {
  state = {
    actionsLeft: 0
  };

  componentDidMount() {
    this.props.fetchRewards();

    const { user } = this.props;
    let actionsLeft = 0;
    if (!user || !user.has_verified_email) {
      actionsLeft++;
    }

    if (!user || !user.is_identity_verified) {
      actionsLeft++;
    }

    this.setState({ actionsLeft });
  }

  render() {
    const { fetching, navigation, unclaimedRewardAmount, user } = this.props;

    if (this.state.actionsLeft === 0 || unclaimedRewardAmount === 0) {
      return null;
    }

    return (
      <TouchableOpacity style={rewardStyle.summaryContainer} onPress={() => {
        navigation.navigate('Rewards');
      }}>
        <Text style={rewardStyle.summaryText}>
          You have {unclaimedRewardAmount} LBC in unclaimed rewards. You have {this.state.actionsLeft} action{this.state.actionsLeft === 1 ? '' : 's'} left to claim your first reward. Tap here to continue.
        </Text>
      </TouchableOpacity>
    );
  }
}

export default RewardSummary;
