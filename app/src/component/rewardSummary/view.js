import React from 'react';
import { AsyncStorage, NativeModules, Text, TouchableOpacity } from 'react-native';
import Button from '../../component/button';
import rewardStyle from '../../styles/reward';

class RewardSummary extends React.Component {
  static itemKey = 'rewardSummaryDismissed';

  state = {
    actionsLeft: 0,
    dismissed: false
  };

  componentDidMount() {
    this.props.fetchRewards();

    AsyncStorage.getItem(RewardSummary.itemKey).then(isDismissed => {
      if ('true' === isDismissed) {
        this.setState({ dismissed: true });
      }

      const { user } = this.props;
      let actionsLeft = 0;
      if (!user || !user.has_verified_email) {
        actionsLeft++;
      }

      if (!user || !user.is_identity_verified) {
        actionsLeft++;
      }

      this.setState({ actionsLeft });
    });
  }

  onDismissPressed = () => {
    AsyncStorage.setItem(RewardSummary.itemKey, 'true');
    this.setState({ dismissed: true });
    this.props.notify({
      message: 'You can always claim your rewards from the Rewards page.',
      displayType: ['toast']
    });
  }

  render() {
    const { fetching, navigation, unclaimedRewardAmount, user } = this.props;

    if (this.state.dismissed ||
        (user && user.is_reward_approved) ||
        this.state.actionsLeft === 0 ||
        unclaimedRewardAmount === 0) {
      return null;
    }

    return (
      <TouchableOpacity style={rewardStyle.summaryContainer} onPress={() => {
        navigation.navigate('Rewards');
      }}>
        <Text style={rewardStyle.summaryText}>
          You have {unclaimedRewardAmount} LBC in unclaimed rewards. You have {this.state.actionsLeft} action{this.state.actionsLeft === 1 ? '' : 's'} left to claim your first reward. Tap here to continue.
        </Text>
        <Button style={rewardStyle.dismissButton} theme={"light"} text={"Dismiss"} onPress={this.onDismissPressed} />
      </TouchableOpacity>
    );
  }
}

export default RewardSummary;
