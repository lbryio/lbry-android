import React from 'react';
import { Lbry } from 'lbry-redux';
import { NativeModules, Text, View, ScrollView } from 'react-native';
import PageHeader from '../../component/pageHeader';
import aboutStyle from '../../styles/about';

class AboutPage extends React.PureComponent {
  state = {
    appVersion: null,
    lbryId: null,
    versionInfo: null
  };
  
  componentDidMount() {
    if (NativeModules.VersionInfo) {
      NativeModules.VersionInfo.getAppVersion().then(version => {
        this.setState({appVersion: version});  
      });
    }
    Lbry.version().then(info => {
      this.setState({
        versionInfo: info,
      });
    });
    Lbry.status({ session_status: true }).then(info => {
      this.setState({
        lbryId: info.lbry_id,
      });
    });
  }
  
  render() {
    const loading = 'Loading...';
    const ver = this.state.versionInfo ? this.state.versionInfo : null;
    
    return (
      <View>
        <PageHeader title={"About LBRY"}
          onBackPressed={() => { this.props.navigation.goBack(); }} />
        <ScrollView style={aboutStyle.scrollContainer}>
          <Text style={aboutStyle.releaseInfoTitle}>Release information</Text>
          <View style={aboutStyle.row}>
            <View style={aboutStyle.col}><Text style={aboutStyle.text}>App version</Text></View>
            <View style={aboutStyle.col}><Text selectable={true} style={aboutStyle.valueText}>{this.state.appVersion}</Text></View>
          </View>
          
          <View style={aboutStyle.row}>
            <View style={aboutStyle.col}><Text style={aboutStyle.text}>Daemon (lbrynet)</Text></View>
            <View style={aboutStyle.col}><Text selectable={true} style={aboutStyle.valueText}>{ver ? ver.lbrynet_version : loading }</Text></View>
          </View>
          
          <View style={aboutStyle.row}>
            <View style={aboutStyle.col}><Text style={aboutStyle.text}>Wallet (lbryum)</Text></View>
            <View style={aboutStyle.col}><Text selectable={true} style={aboutStyle.valueText}>{ver ? ver.lbryum_version : loading }</Text></View>
          </View>
        
          <View style={aboutStyle.row}>
            <View style={aboutStyle.col}><Text style={aboutStyle.text}>Platform</Text></View>
            <View style={aboutStyle.col}><Text selectable={true} style={aboutStyle.valueText}>{ver ? ver.platform : loading }</Text></View>
          </View>
          
          <View style={aboutStyle.row}>
            <View style={aboutStyle.col}>
              <Text style={aboutStyle.text}>Installation ID</Text>
              <Text selectable={true} style={aboutStyle.lineValueText}>{this.state.lbryId ? this.state.lbryId : loading}</Text>
            </View>
          </View>
        </ScrollView>
      </View>
    );
  }
}

export default AboutPage;
