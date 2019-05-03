import React from 'react';
import { NativeModules, Text, TouchableOpacity, View } from 'react-native';
import AsyncStorage from '@react-native-community/async-storage';
import Button from 'component/button';
import Colors from 'styles/colors';
import Icon from 'react-native-vector-icons/FontAwesome5';
import rewardStyle from 'styles/reward';

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
    });
  }

  handleSummaryPressed = () => {
    const { showVerification } = this.props;
    if (showVerification) {
      showVerification();
    }
  }

  render() {
    const { fetching, navigation, unclaimedRewardAmount, user } = this.props;

    if (!user) {
      return null;
    }

    if (this.state.dismissed ||
        (user && user.is_reward_approved) ||
        this.state.actionsLeft === 0 ||
        unclaimedRewardAmount === 0) {
      return null;
    }

    return (
      <TouchableOpacity style={rewardStyle.summaryContainer} onPress={this.handleSummaryPressed}>
        <View style={rewardStyle.summaryRow}>
          <Icon name="award" size={36} color={Colors.White} />
          <Text style={rewardStyle.summaryText}>
            {unclaimedRewardAmount} unclaimed credits
          </Text>
        </View>
        <Button style={rewardStyle.dismissButton} theme={"light"} text={"Dismiss"} onPress={this.onDismissPressed} />
      </TouchableOpacity>
    );
  }
}

export default RewardSummary;
