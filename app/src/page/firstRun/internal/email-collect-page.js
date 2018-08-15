import React from 'react';
import { Lbry } from 'lbry-redux';
import {
  ActivityIndicator,
  AsyncStorage,
  Linking,
  Text,
  TextInput,
  View
} from 'react-native';
import Colors from '../../../styles/colors';
import firstRunStyle from '../../../styles/firstRun';

class EmailCollectPage extends React.PureComponent {
  constructor() {
    super();
    this.state = {
      email: null
    };
  }

  componentDidMount() {
    // call user/new
    const { generateAuthToken, authenticating, authToken } = this.props;
    if (!authToken && !authenticating) {
      Lbry.status().then(info => {
        generateAuthToken(info.installation_id)
      });
    }

    AsyncStorage.getItem('firstRunEmail').then(email => {
      if (email) {
        this.setState({ email });
      }
    });
  }

  handleChangeText = (text) => {
    // save the value to the state email
    this.setState({ email: text });
    AsyncStorage.setItem('firstRunEmail', text);
  }

  render() {
    let authenticationFailed = false;
    const { authenticating, authToken, onEmailViewLayout, emailToVerify } = this.props;

    let content;
    if (!authToken || authenticating) {
      content = (
        <View>
          <ActivityIndicator size="large" color={Colors.White} style={firstRunStyle.waiting} />
          <Text style={firstRunStyle.paragraph}>Please wait while we get some things ready...</Text>
        </View>
      )
    } else if (authenticationFailed) {
      // Ask the user to try again
    } else {
      content = (
        <View onLayout={onEmailViewLayout}>
          <Text style={firstRunStyle.title}>Rewards.</Text>
          <Text style={firstRunStyle.paragraph}>You can earn LBRY Credits (LBC) rewards by completing various tasks in the app.</Text>
          <Text style={firstRunStyle.paragraph}>Please provide a valid email address below to be able to claim your rewards.</Text>
          <TextInput style={firstRunStyle.emailInput}
              placeholder="you@example.com"
              underlineColorAndroid="transparent"
              value={this.state.email}
              onChangeText={text => this.handleChangeText(text)}
              />
          <Text style={firstRunStyle.infoParagraph}>This information is disclosed only to LBRY, Inc. and not to the LBRY network. It is only required to earn LBRY rewards and may be used to sync usage data across devices.</Text>
        </View>
      )
    }

    return (
      <View style={firstRunStyle.container}>
        {content}
      </View>
    );
  }
}

export default EmailCollectPage;
