import React from 'react';
import { Lbry } from 'lbry-redux';
import {
  DeviceEventEmitter,
  ActivityIndicator,
  NativeModules,
  ScrollView,
  Text,
  View
} from 'react-native';
import { doInstallNew } from 'lbryinc';
import Colors from '../../styles/colors';
import Link from '../../component/link';
import DeviceIdRewardSubcard from '../../component/deviceIdRewardSubcard';
import EmailRewardSubcard from '../../component/emailRewardSubcard';
import PageHeader from '../../component/pageHeader';
import RewardCard from '../../component/rewardCard';
import rewardStyle from '../../styles/reward';

class RewardsPage extends React.PureComponent {
  state = {
    canAcquireDeviceId: false,
    isEmailVerified: false,
    isRewardApproved: false,
    verifyRequestStarted: false,
  };

  componentDidMount() {
    DeviceEventEmitter.addListener('onPhoneStatePermissionGranted', this.phoneStatePermissionGranted);

    this.props.fetchRewards();

    const { user } = this.props;
    this.setState({
      isEmailVerified: (user && user.primary_email && user.has_verified_email),
      isRewardApproved: (user && user.is_reward_approved)
    });

    if (NativeModules.UtilityModule) {
      const util = NativeModules.UtilityModule;
      util.canAcquireDeviceId().then(canAcquireDeviceId => {
        this.setState({ canAcquireDeviceId });
      });
    }
  }

  componentWillUnmount() {
    DeviceEventEmitter.removeListener('onPhoneStatePermissionGranted', this.phoneStatePermissionGranted);
  }

  componentWillReceiveProps(nextProps) {
    const { emailVerifyErrorMessage, emailVerifyPending } = nextProps;
    if (emailVerifyPending) {
      this.setState({ verifyRequestStarted: true });
    }

    if (this.state.verifyRequestStarted && !emailVerifyPending) {
      const { user } = nextProps;
      this.setState({ verifyRequestStarted: false });
      if (!emailVerifyErrorMessage) {
        this.setState({
          isEmailVerified: true,
          isRewardApproved: (user && user.is_reward_approved)
        });
      }
    }
  }

  renderVerification() {
    if (!this.state.isRewardApproved) {
      return (
        <View style={[rewardStyle.card, rewardStyle.verification]}>
          <Text style={rewardStyle.title}>Humans Only</Text>
          <Text style={rewardStyle.text}>Rewards are for human beings only. You'll have to prove you're one of us before you can claim any rewards.</Text>
          {!this.state.canAcquireDeviceId && <DeviceIdRewardSubcard />}
          {!this.state.isEmailVerified && <EmailRewardSubcard />}
        </View>
      );
    }

    return null;
  }

  phoneStatePermissionGranted = () => {
    const { install, notify } = this.props;
    if (NativeModules.UtilityModule) {
      const util = NativeModules.UtilityModule;

      // Double-check just to be sure
      util.canAcquireDeviceId().then(canAcquireDeviceId => {
        this.setState({ canAcquireDeviceId });
        if (canAcquireDeviceId) {
          util.getDeviceId(false).then(deviceId => {
            NativeModules.VersionInfo.getAppVersion().then(appVersion => {
              doInstallNew(`android-${appVersion}`, deviceId);
            });
          }).catch((error) => {
            notify({ message: error, displayType: ['toast'] });
            this.setState({ canAcquireDeviceId: false });
          });
        }
      });
    }
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
