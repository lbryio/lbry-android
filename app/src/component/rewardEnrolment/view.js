import React from 'react';
import { NativeModules, Text, TouchableOpacity, View } from 'react-native';
import AsyncStorage from '@react-native-community/async-storage';
import Button from 'component/button';
import Constants from 'constants';
import Link from 'component/link';
import Colors from 'styles/colors';
import Icon from 'react-native-vector-icons/FontAwesome5';
import rewardStyle from 'styles/reward';

class RewardEnrolment extends React.Component {
  componentDidMount() {
    this.props.fetchRewards();
  }

  onNotInterestedPressed = () => {
    const { navigation, setClientSetting } = this.props;
    setClientSetting(Constants.SETTING_REWARDS_NOT_INTERESTED, true);
    navigation.navigate({ routeName: 'DiscoverStack' });
  }

  onEnrollPressed = () => {
    const { navigation } = this.props;
    navigation.navigate({ routeName: 'Verification' })
  }

  render() {
    const { fetching, navigation, unclaimedRewardAmount, user } = this.props;

    return (
      <View style={rewardStyle.enrollContainer} onPress>
        <View style={rewardStyle.summaryRow}>
          <Icon name="award" size={36} color={Colors.White} />
          <Text style={rewardStyle.summaryText}>
            {unclaimedRewardAmount} unclaimed credits
          </Text>
        </View>

        <View style={rewardStyle.onboarding}>
          <Text style={rewardStyle.enrollDescText}>LBRY credits allow you to purchase content, publish content, and influence the network. You can start earning credits by watching videos on LBRY.</Text>
        </View>

        <View style={rewardStyle.buttonRow}>
          <Link style={rewardStyle.notInterestedLink} text={"Not interested"} onPress={this.onNotInterestedPressed} />
          <Button style={rewardStyle.enrollButton} theme={"light"} text={"Enroll"} onPress={this.onEnrollPressed} />
        </View>

      </View>
    );
  }
}

export default RewardEnrolment;
