// @flow
import React from 'react';
import { ActivityIndicator, Keyboard, Text, TextInput, TouchableOpacity, View } from 'react-native';
import Colors from '../../styles/colors';
import Icon from 'react-native-vector-icons/FontAwesome5';
import Button from '../button';
import Link from '../link';
import rewardStyle from '../../styles/reward';

class CustomRewardCard extends React.PureComponent<Props> {
  state = {
    claimStarted: false,
    rewardCode: ''
  };

  componentWillReceiveProps(nextProps) {
    const { error, rewardIsPending } = nextProps;
    const { clearError, notify } = this.props;
    if (this.state.claimStarted && !rewardIsPending) {
      if (error && error.trim().length > 0) {
        notify({ message: error });
      } else {
        notify({ message: 'Reward successfully claimed!' });
        this.setState({ rewardCode: '' });
      }
      this.setState({ claimStarted: false });
    }
  }

  onClaimPress = () => {
    const { canClaim, notify, showVerification, submitRewardCode } = this.props;
    const { rewardCode } = this.state;

    Keyboard.dismiss();

    if (!canClaim) {
      if (showVerification) {
        showVerification();
      }
      notify({ message: 'Unfortunately, you are not eligible to claim this reward at this time.' });
      return;
    }

    if (!rewardCode || rewardCode.trim().length === 0) {
      notify({ message: 'Please enter a reward code to claim.' });
      return;
    }

    this.setState({ claimStarted: true }, () => {
      submitRewardCode(rewardCode);
    });
  }

  render() {
    const { canClaim, rewardIsPending } = this.props;

    return (
      <View style={[rewardStyle.rewardCard, rewardStyle.row]} >
        <View style={rewardStyle.leftCol}>
          {rewardIsPending && <ActivityIndicator size="small" color={Colors.LbryGreen} />}
        </View>
        <View style={rewardStyle.midCol}>
          <Text style={rewardStyle.rewardTitle}>Custom Code</Text>
          <Text style={rewardStyle.rewardDescription}>Are you a supermodel or rockstar that received a custom reward code? Claim it here.</Text>

          <View>
            <TextInput style={rewardStyle.customCodeInput}
                       placeholder={"0123abc"}
                       onChangeText={text => this.setState({ rewardCode: text })}
                       value={this.state.rewardCode} />
            <Button style={rewardStyle.redeemButton}
                    text={"Redeem"}
                    disabled={(!this.state.rewardCode || this.state.rewardCode.trim().length === 0 || rewardIsPending)}
                    onPress={() => {
                      if (!rewardIsPending) { this.onClaimPress(); }
                    }} />
          </View>
        </View>
        <View style={rewardStyle.rightCol}>
          <Text style={rewardStyle.rewardAmount}>?</Text>
          <Text style={rewardStyle.rewardCurrency}>LBC</Text>
        </View>
      </View>
    );
  }
};

export default CustomRewardCard;
