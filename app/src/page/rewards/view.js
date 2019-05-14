import React from 'react';
import { Lbry } from 'lbry-redux';
import {
  ActivityIndicator,
  NativeModules,
  ScrollView,
  Text,
  View
} from 'react-native';
import Colors from 'styles/colors';
import Link from 'component/link';
import CustomRewardCard from 'component/customRewardCard';
import PhoneNumberRewardSubcard from 'component/phoneNumberRewardSubcard';
import EmailRewardSubcard from 'component/emailRewardSubcard';
import PageHeader from 'component/pageHeader';
import RewardCard from 'component/rewardCard';
import RewardEnrolment from 'component/rewardEnrolment';
import RewardSummary from 'component/rewardSummary';
import UriBar from 'component/uriBar';
import rewardStyle from 'styles/reward';

class RewardsPage extends React.PureComponent {
  state = {
    isEmailVerified: false,
    isIdentityVerified: false,
    isRewardApproved: false,
    verifyRequestStarted: false,
    revealVerification: true,
    firstRewardClaimed: false
  };

  scrollView = null;

  componentDidMount() {
    const { fetchRewards, pushDrawerStack, navigation, user } = this.props;

    pushDrawerStack();
    fetchRewards();

    this.setState({
      isEmailVerified: (user && user.primary_email && user.has_verified_email),
      isIdentityVerified: (user && user.is_identity_verified),
      isRewardApproved: (user && user.is_reward_approved)
    });
  }

  componentWillReceiveProps(nextProps) {
    const { emailVerifyErrorMessage, emailVerifyPending, rewards, user } = nextProps;
    const { claimReward } = this.props;
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

    if (rewards && rewards.length && this.state.isRewardApproved && !this.state.firstRewardClaimed) {
      // claim new_user and new_mobile rewards
      for (let i = 0; i < rewards.length; i++) {
        const { reward_type: type } = rewards[i];
        if ('new_user' === type || 'new_mobile' === type) {
          claimReward(rewards[i]);
        }
      }
      this.setState({ firstRewardClaimed: true });
    }
  }

  renderVerification() {
    if (this.state.isRewardApproved) {
      return null;
    }

    if (this.state.isEmailVerified && this.state.isIdentityVerified && !this.state.isRewardApproved) {
      return (
        <View style={[rewardStyle.card, rewardStyle.verification]}>
          <Text style={rewardStyle.title}>Manual Reward Verification</Text>
          <Text style={rewardStyle.text}>
            You need to be manually verified before you can start claiming rewards. Please request to be verified on the <Link style={rewardStyle.greenLink} href="https://discordapp.com/invite/Z3bERWA" text="LBRY Discord server" />.
          </Text>
        </View>
      );
    }

    return null;
  }

  renderUnclaimedRewards() {
    const { claimed, fetching, rewards, user } = this.props;
    const unclaimedRewards = (rewards && rewards.length) ? rewards : [];

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
    }

    const isNotEligible = !user || !user.primary_email || !user.has_verified_email || !user.is_reward_approved;
    return (
      <View>
        {unclaimedRewards.map(reward => <RewardCard key={reward.reward_type}
                                          showVerification={this.showVerification}
                                          canClaim={!isNotEligible}
                                          reward={reward}
                                          reward_type={reward.reward_type} />)}
        <CustomRewardCard canClaim={!isNotEligible} showVerification={this.showVerification} />
      </View>
    );
  }

  renderClaimedRewards() {
    const { claimed } = this.props;
    if (claimed && claimed.length) {
      const reversed = claimed.reverse();
      return (
        <View>
          {reversed.map(reward => <RewardCard key={reward.transaction_id} reward={reward} />)}
        </View>
      );
    }
  }

  showVerification = () => {
    this.setState({ revealVerification: true }, () => {
      if (this.scrollView) {
        this.scrollView.scrollTo({ x: 0, y: 0, animated: true });
      }
    });
  }

  render() {
    const { user, navigation } = this.props;

    return (
      <View style={rewardStyle.container}>
        <UriBar navigation={navigation} />
        {(!this.state.isEmailVerified || !this.state.isIdentityVerified || !this.state.isRewardApproved) &&
          <RewardEnrolment navigation={navigation} />}

        {(this.state.isEmailVerified && this.state.isIdentityVerified && this.state.isRewardApproved) &&
          <ScrollView
            ref={ref => this.scrollView = ref}
            keyboardShouldPersistTaps={'handled'}
            style={rewardStyle.scrollContainer}
            contentContainerStyle={rewardStyle.scrollContentContainer}>
            {this.renderUnclaimedRewards()}
            {this.renderClaimedRewards()}
          </ScrollView>}
      </View>
    );
  }
}

export default RewardsPage;
