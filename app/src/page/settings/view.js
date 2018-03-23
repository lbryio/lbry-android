import React from 'react';
import { SETTINGS } from 'lbry-redux';
import { Text, View, ScrollView, Switch } from 'react-native';
import settingsStyle from '../../styles/settings';

class SettingsPage extends React.PureComponent {
  static navigationOptions = {
    title: 'Settings'
  }
  
  render() {
    const {
      keepDaemonRunning,
      showNsfw,
      setClientSetting
    } = this.props;
       
    return (
      <View>
      <Text style={settingsStyle.title}>Settings</Text>
        <ScrollView style={settingsStyle.scrollContainer}>
          <View style={settingsStyle.row}>
            <View style={settingsStyle.switchText}>
              <Text style={settingsStyle.label}>Keep the daemon background service running when the app is suspended.</Text>
              <Text style={settingsStyle.description}>Enable this option for quicker app launch and to keep the synchronisation with the blockchain up to date.</Text>
            </View>
            <View style={settingsStyle.switchContainer}>
              <Switch value={keepDaemonRunning} onValueChange={(value) => setClientSetting(SETTINGS.KEEP_DAEMON_RUNNING, value)} />
            </View>
          </View>
        </ScrollView>
      </View>
    );
  }
}

export default SettingsPage;
