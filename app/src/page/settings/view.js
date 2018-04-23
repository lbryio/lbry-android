import React from 'react';
import { SETTINGS } from 'lbry-redux';
import { Text, View, ScrollView, Switch } from 'react-native';
import PageHeader from '../../component/pageHeader';
import settingsStyle from '../../styles/settings';

class SettingsPage extends React.PureComponent {
  static navigationOptions = {
    title: 'Settings'
  }
  
  render() {
    const {
      backgroundPlayEnabled,
      keepDaemonRunning,
      showNsfw,
      setClientSetting
    } = this.props;
       
    return (
      <View>
        <PageHeader title={"Settings"}
          onBackPressed={() => { this.props.navigation.goBack(); }} />
        <ScrollView style={settingsStyle.scrollContainer}>
          <View style={settingsStyle.row}>
            <View style={settingsStyle.switchText}>
              <Text style={settingsStyle.label}>Enable background media playback</Text>
              <Text style={settingsStyle.description}>Enable this option to play audio or video in the background when the app is suspended.</Text>
            </View>
            <View style={settingsStyle.switchContainer}>
              <Switch value={backgroundPlayEnabled} onValueChange={(value) => setClientSetting(SETTINGS.BACKGROUND_PLAY_ENABLED, value)} />
            </View>
          </View>
          
          <View style={settingsStyle.row}>
            <View style={settingsStyle.switchText}>
              <Text style={settingsStyle.label}>Show NSFW content</Text>
            </View>
            <View style={settingsStyle.switchContainer}>
              <Switch value={showNsfw} onValueChange={(value) => setClientSetting(SETTINGS.SHOW_NSFW, value)} />
            </View>
          </View>
          
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
