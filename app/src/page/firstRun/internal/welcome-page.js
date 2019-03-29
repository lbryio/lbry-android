import React from 'react';
import { Lbry } from 'lbry-redux';
import { View, Text, Linking } from 'react-native';
import Colors from 'styles/colors';
import firstRunStyle from 'styles/firstRun';

class WelcomePage extends React.PureComponent {
  render() {
    return (
      <View style={firstRunStyle.container}>
        <Text style={firstRunStyle.title}>Welcome to LBRY.</Text>
        <Text style={firstRunStyle.paragraph}>LBRY is a decentralized peer-to-peer content sharing platform where
        you can upload and download videos, music, ebooks and other forms of digital content.</Text>
        <Text style={firstRunStyle.paragraph}>We make use of a blockchain which needs to be synchronized before
        you can use the app. Synchronization may take a while because this is the first app launch.</Text>
      </View>
    );
  }
}

export default WelcomePage;
