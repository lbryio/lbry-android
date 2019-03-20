import React from 'react';
import { Lbry } from 'lbry-redux';
import { NativeModules, Text, View, ScrollView } from 'react-native';
import { navigateBack } from 'utils/helper';
import Link from 'component/link';
import PageHeader from 'component/pageHeader';
import aboutStyle from 'styles/about';

class AboutPage extends React.PureComponent {
  state = {
    appVersion: null,
    lbryId: null,
    versionInfo: null
  };

  componentDidMount() {
    this.props.pushDrawerStack();
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
    Lbry.status().then(info => {
      this.setState({
        lbryId: info.installation_id,
      });
    });

    if (!this.props.accessToken) this.props.fetchAccessToken();
  }

  render() {
    const { accessToken, drawerStack, navigation, notify, popDrawerStack, userEmail } = this.props;
    const loading = 'Loading...';
    const ver = this.state.versionInfo ? this.state.versionInfo : null;

    return (
      <View style={aboutStyle.container}>
        <PageHeader title={"About LBRY"}
          onBackPressed={() => navigateBack(navigation, drawerStack, popDrawerStack)} />
        <ScrollView style={aboutStyle.scrollContainer}>
          <Text style={aboutStyle.title}>Content Freedom</Text>
          <Text style={aboutStyle.paragraph}>
            LBRY is a free, open, and community-run digital marketplace. It is a decentralized peer-to-peer
            content distribution platform for creators to upload and share content, and earn LBRY credits
            for their effort. Users will be able to find a wide selection of videos, music, ebooks and other
            digital content they are interested in.
          </Text>
          <View style={aboutStyle.links}>
            <Link style={aboutStyle.link} href="https://lbry.com/faq/what-is-lbry" text="What is LBRY?" />
            <Link style={aboutStyle.link} href="https://lbry.com/faq/android-basics" text="Android App Basics" />
            <Link style={aboutStyle.link} href="https://lbry.com/faq" text="Frequently Asked Questions" />
          </View>
          <Text style={aboutStyle.socialTitle}>Get Social</Text>
          <Text style={aboutStyle.paragraph}>
            You can interact with the LBRY team and members of the community on Discord, Facebook, Instagram, Twitter or Reddit.
          </Text>
          <View style={aboutStyle.links}>
            <Link style={aboutStyle.link} href="https://discordapp.com/invite/Z3bERWA" text="Discord" />
            <Link style={aboutStyle.link} href="https://www.facebook.com/LBRYio" text="Facebook" />
            <Link style={aboutStyle.link} href="https://www.instagram.com/LBRYio/" text="Instagram" />
            <Link style={aboutStyle.link} href="https://twitter.com/LBRYio" text="Twitter" />
            <Link style={aboutStyle.link} href="https://reddit.com/r/lbry" text="Reddit" />
            <Link style={aboutStyle.link} href="https://t.me/lbryofficial" text="Telegram" />
          </View>
          <Text style={aboutStyle.releaseInfoTitle}>App info</Text>
          {userEmail && userEmail.trim().length > 0 &&
          <View style={aboutStyle.verticalRow}>
            <View style={aboutStyle.innerRow}>
              <View style={aboutStyle.col}><Text style={aboutStyle.text}>Connected Email</Text></View>
              <View style={aboutStyle.col}><Text selectable={true} style={aboutStyle.valueText}>{userEmail}</Text></View>
            </View>
            <View>
              <Link
                style={aboutStyle.listLink}
                href={`http://lbry.com/list/edit/${accessToken}`}
                text="Update mailing preferences" />
            </View>
          </View>}

          <View style={aboutStyle.row}>
            <View style={aboutStyle.col}><Text style={aboutStyle.text}>App version</Text></View>
            <View style={aboutStyle.col}><Text selectable={true} style={aboutStyle.valueText}>{this.state.appVersion}</Text></View>
          </View>

          <View style={aboutStyle.row}>
            <View style={aboutStyle.col}><Text style={aboutStyle.text}>Daemon (lbrynet)</Text></View>
            <View style={aboutStyle.col}><Text selectable={true} style={aboutStyle.valueText}>{ver ? ver.lbrynet_version : loading }</Text></View>
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

          <View style={aboutStyle.row}>
            <View style={aboutStyle.col}><Text style={aboutStyle.text}>Logs</Text></View>
            <View style={aboutStyle.col}>
              <Link style={aboutStyle.listLink} text="Send log" onPress={() => {
                if (NativeModules.UtilityModule) {
                  NativeModules.UtilityModule.shareLogFile((error) => {
                    if (error) {
                      notify(error);
                    }
                  });
                }
              }} />
            </View>
          </View>
        </ScrollView>
      </View>
    );
  }
}

export default AboutPage;
