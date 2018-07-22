import React from 'react';
import { Lbry } from 'lbry-redux';
import { NativeModules, Text, View, ScrollView } from 'react-native';
import Link from '../../component/link';
import PageHeader from '../../component/pageHeader';
import rewardStyle from '../../styles/reward';

class RewardsPage extends React.PureComponent {
  renderVerification() {
    const { user } = this.props;
    if (user && !user.is_reward_approved) {
      if (!user.primary_email || !user.has_verified_email || !user.is_identity_verified) {
        return (
          <View style={rewardStyle.card}>
            <Text style={rewardStyle.title}>Humans Only</Text>
            <Text style={rewardStyle.text}>Rewards are for human beings only. You'll have to prove you're one of us before you can claim any rewards.</Text>
          </View>
        );
      }

      return (
        <View>
          <Text style={rewardStyle.text}>This account must undergo review.</Text>
        </View>
      );
    }

    return null;
  }

  renderUnclaimedRewards() {
    return null;
  }

  render() {
    return (
      <View>
        <PageHeader title={"Rewards"}
          onBackPressed={() => { this.props.navigation.goBack(); }} />
        <ScrollView style={rewardStyle.scrollContainer}>
          {this.renderVerification()}
          {this.renderUnclaimedRewards()}
        </ScrollView>
      </View>
    );
  }
}

export default RewardsPage;
