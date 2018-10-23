import React from 'react';
import { Lbry } from 'lbry-redux';
import {
  ActivityIndicator,
  NativeModules,
  ScrollView,
  Text,
  View
} from 'react-native';
import Colors from '../../styles/colors';
import Link from '../../component/link';
import PhoneNumberRewardSubcard from '../../component/phoneNumberRewardSubcard';
import EmailRewardSubcard from '../../component/emailRewardSubcard';
import PageHeader from '../../component/pageHeader';
import RewardCard from '../../component/rewardCard';
import rewardStyle from '../../styles/reward';

class RewardsPage extends React.PureComponent {
  state = {
    isEmailVerified: false,
    isIdentityVerified: false,
    isRewardApproved: false,
    verifyRequestStarted: false,
  };

  componentDidMount() {
    this.props.fetchRewards();

    const { user } = this.props;
    this.setState({
      isEmailVerified: (user && user.primary_email && user.has_verified_email),
      isIdentityVerified: (user && user.is_identity_verified),
      isRewardApproved: (user && user.is_reward_approved)
    });
  }

  componentWillReceiveProps(nextProps) {
    const { emailVerifyErrorMessage, emailVerifyPending, user } = nextProps;
    if (emailVerifyPending) {
      this.setState({ verifyRequestStarted: true });
    }

    if (this.state.verifyRequestStarted && !emailVerifyPending) {
      this.setState({ verifyRequestStarted: false });
      if (!emailVerifyErrorMessage) {
        this.setState({ isEmailVerified: true });
      }
    }

    if (user) {
      // update other checks (if new user data has been retrieved)
      this.setState({
        isEmailVerified: (user && user.primary_email && user.has_verified_email),
        isIdentityVerified: (user && user.is_identity_verified),
        isRewardApproved: (user && user.is_reward_approved)
      });
    }
  }

  renderVerification() {
    if (this.state.isRewardApproved) {
      return null;
    }

    if (!this.state.isEmailVerified || !this.state.isIdentityVerified) {
      return (
        <View style={[rewardStyle.card, rewardStyle.verification]}>
          <Text style={rewardStyle.title}>Humans Only</Text>
          <Text style={rewardStyle.text}>Rewards are for human beings only. You'll have to prove you're one of us before you can claim any rewards.</Text>
          {!this.state.isEmailVerified && <EmailRewardSubcard />}
          {!this.state.isIdentityVerified && <PhoneNumberRewardSubcard />}
        </View>
      );
    }

    if (this.state.isEmailVerified && this.state.isIdentityVerified && !this.state.isRewardApproved) {
      return (
        <View style={[rewardStyle.card, rewardStyle.verification]}>
          <Text style={rewardStyle.title}>Manual Reward Verification</Text>
          <Text style={rewardStyle.text}>
            You need to be manually verified before you can start claiming rewards. Please request to be verified on the <Link style={rewardStyle.textLink} href="https://discordapp.com/invite/Z3bERWA" text="LBRY Discord server" />.
          </Text>
        </View>
      );
    }

    return null;
  }

  renderUnclaimedRewards() {
    const { claimed, fetching, rewards, user } = this.props;

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
          <Text style={rewardStyle.infoText}>This app is unable to earn rewards due to an authentication failure.</Text>
        </View>
      );
    } else if (!rewards || rewards.length <= 0) {
      return (
        <View style={rewardStyle.busyContainer}>
          <Text style={rewardStyle.infoText}>
            {(claimed && claimed.length) ?
              "You have claimed all available rewards! We're regularly adding more so be sure to check back later." :
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
                                           reward_type={reward.reward_type} />)}
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
    const { user } = this.props;

    return (
      <View style={rewardStyle.container}>
        <ScrollView
          keyboardShouldPersistTaps={'handled'}
          style={rewardStyle.scrollContainer}
          contentContainerStyle={rewardStyle.scrollContentContainer}>
          {this.renderVerification()}
          {this.renderUnclaimedRewards()}
          {this.renderClaimedRewards()}
        </ScrollView>
      </View>
    );
  }
}

export default RewardsPage;
