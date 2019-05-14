import React from 'react';
import { Lbry } from 'lbry-redux';
import { ActivityIndicator, View, Text, TextInput } from 'react-native';
import AsyncStorage from '@react-native-community/async-storage';
import Button from 'component/button';
import Link from 'component/link';
import Colors from 'styles/colors';
import Constants from 'constants';
import firstRunStyle from 'styles/firstRun';
import rewardStyle from 'styles/reward';

class ManualVerifyPage extends React.PureComponent {
  render() {
    return (
      <View style={firstRunStyle.container}>
        <Text style={rewardStyle.verificationTitle}>Manual Reward Verification</Text>
        <Text style={firstRunStyle.paragraph}>You need to be manually verified before you can start claiming rewards. Please request to be verified on the <Link style={rewardStyle.underlinedTextLink} href="https://discordapp.com/invite/Z3bERWA" text="LBRY Discord server" />.</Text>
      </View>
    );
  }
}

export default ManualVerifyPage;
