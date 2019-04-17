import React from 'react';
import { Lbry } from 'lbry-redux';
import {
  ActivityIndicator,
  AsyncStorage,
  Linking,
  NativeModules,
  Platform,
  Switch,
  Text,
  TextInput,
  View
} from 'react-native';
import Colors from 'styles/colors';
import Constants from 'constants';
import firstRunStyle from 'styles/firstRun';

class SkipAccountPage extends React.PureComponent {
  render() {
    const { onSkipAccountViewLayout, onSkipSwitchChanged } = this.props;

    const content = (
      <View onLayout={onSkipAccountViewLayout}>
        <Text style={firstRunStyle.title}>Are you sure?</Text>
        <Text style={firstRunStyle.paragraph}>By not creating an account, you will not receive free credits, sync or backup services, or security updates.</Text>

        <View style={firstRunStyle.row}>
          <Switch value={false} onValueChange={value => onSkipSwitchChanged(value)} />
          <Text style={firstRunStyle.paragraph}>I understand that if I uninstall LBRY I can lose all access to any balances or published content with no recovery option.</Text>
        </View>
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
