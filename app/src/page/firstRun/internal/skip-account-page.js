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
import Icon from 'react-native-vector-icons/FontAwesome5';
import firstRunStyle from 'styles/firstRun';

class SkipAccountPage extends React.PureComponent {
  state = {
    confirmed: false
  };

  render() {
    const { onSkipAccountViewLayout, onSkipSwitchChanged } = this.props;

    const content = (
      <View onLayout={onSkipAccountViewLayout}>
        <View style={firstRunStyle.row}>
          <Icon name="exclamation-triangle" style={firstRunStyle.titleIcon} size={32} color={Colors.White} />
          <Text style={firstRunStyle.title}>Are you sure?</Text>
        </View>
        <Text style={firstRunStyle.paragraph}>Without an account, you will not receive rewards, sync and backup services, or security updates.</Text>

        <View style={[firstRunStyle.row, firstRunStyle.confirmContainer]}>
          <View style={firstRunStyle.rowSwitch}>
            <Switch value={this.state.confirmed} onValueChange={value => { this.setState({ confirmed: value }); onSkipSwitchChanged(value); }} />
          </View>
          <Text style={firstRunStyle.rowParagraph}>I understand that by uninstalling LBRY I will lose any balances or published content with no recovery option.</Text>
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
