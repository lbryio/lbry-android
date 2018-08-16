import React from 'react';
import { Lbry } from 'lbry-redux';
import { ActivityIndicator, NativeModules, ScrollView, Text, View } from 'react-native';
import Colors from '../../styles/colors';
import Link from '../../component/link';
import PageHeader from '../../component/pageHeader';
import RewardCard from '../../component/rewardCard';
import rewardStyle from '../../styles/reward';

class RewardsPage extends React.PureComponent {
  componentDidMount() {
    this.props.fetchRewards();
  }

  renderVerification() {
    const { user } = this.props;
    if (user && !user.is_reward_approved) {
      if (!user.primary_email || !user.has_verified_email || !user.is_identity_verified) {
        return (
          <View style={[rewardStyle.card, rewardStyle.verification]}>
            <Text style={rewardStyle.title}>Humans Only</Text>
            <Text style={rewardStyle.text}>Rewards are for human beings only. You'll have to prove you're one of us before you can claim any rewards.</Text>
          </View>
        );
      }

      return (
        <View style={[rewardStyle.card, rewardStyle.verification]}>
          <Text style={rewardStyle.text}>This account must undergo review.</Text>
        </View>
      );
    }

    return null;
  }

  onClaimRewardPress = () => {
    const { notify, user } = this.props;
    console.log(user);
    const isNotEligible = !user || !user.primary_email || !user.has_verified_email || !user.is_reward_approved;
    if (isNotEligible) {
      notify({ message: 'Unfortunately, you are not eligible to claim this reward at this time.', displayType: ['toast'] });
      return;
    }
  }

  renderUnclaimedRewards() {
    const { fetching, rewards, user, claimed } = this.props;

    if (fetching) {
      return (
        <View style={rewardStyle.busyContainer}>
          <ActivityIndicator size="large" color={Colors.LbryGreen} />
          <Text style={rewardStyle.infoText}>Fetching rewards...</Text>
        </View>
      );
    } else if (user === null) {
      return (
        <View style={rewardStyle.busyContainer}>
          <Text style={rewardStyle.infoText}>This application is unable to earn rewards due to an authentication failure.</Text>
        </View>
      );
    } else if (!rewards || rewards.length <= 0) {
      return (
        <View style={rewardStyle.busyContainer}>
          <Text style={rewardStyle.infoText}>
            {(claimed && claimed.length) ? "You have claimed all available rewards! We're regularly adding more so be sure to check back later." :
              "There are no rewards available at this time, please check back later."}
          </Text>
        </View>
      );
    }

    const isNotEligible = !user || !user.primary_email || !user.has_verified_email || !user.is_reward_approved;
    return (
      <View>
        {rewards.map(reward => <RewardCard key={reward.reward_type}
                                           canClaim={!isNotEligible}
                                           reward={reward}
                                           onClaimPress={this.onClaimRewardPress} />)}
      </View>
    );
  }

  renderClaimedRewards() {
    const { claimed } = this.props;
    if (claimed && claimed.length) {
      return (
        <View>
          {claimed.map(reward => <RewardCard key={reward.reward_type} reward={reward} />)}
        </View>
      );
    }
  }

  render() {
    return (
      <View style={rewardStyle.container}>
        {this.renderVerification()}
        <View style={rewardStyle.rewardsContainer}>
          <ScrollView style={rewardStyle.scrollContainer} contentContainerStyle={rewardStyle.scrollContentContainer}>
            {this.renderUnclaimedRewards()}
            {this.renderClaimedRewards()}
          </ScrollView>
        </View>
      </View>
    );
  }
}

export default RewardsPage;
