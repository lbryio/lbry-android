import React from 'react';
import { Lbry } from 'lbry-redux';
import {
  ActivityIndicator,
  AsyncStorage,
  Linking,
  NativeModules,
  Platform,
  Text,
  TextInput,
  View
} from 'react-native';
import Colors from 'styles/colors';
import Constants from 'constants';
import firstRunStyle from 'styles/firstRun';

class SkipAccountPage extends React.PureComponent {
  render() {
    const { onSkipAccountViewLayout } = this.props;

    const content = (
      <View onLayout={onSkipAccountViewLayout}>
        <Text style={firstRunStyle.title}>Are you sure?</Text>
        <Text style={firstRunStyle.paragraph}>If you do not provide an email address, you will not be eligible for free LBC from LBRY, Inc.</Text>
        <Text style={firstRunStyle.paragraph}>Additionally, all of your earnings and settings will be stored locally on this device. Uninstalling the app will delete all of your content and credits permanently.</Text>
      </View>
    );

    return (
      <View style={firstRunStyle.container}>
        {content}
      </View>
    );
  }
}

export default SkipAccountPage;
