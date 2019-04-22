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
        <Text style={firstRunStyle.paragraph}>LBRY is a community-controlled content platform where you can find and publish videos, music, books, and more.</Text>
      </View>
    );
  }
}

export default WelcomePage;
