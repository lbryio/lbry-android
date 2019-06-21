import React from 'react';
import { Lbry } from 'lbry-redux';
import { NativeModules, Platform, Text, TextInput, View } from 'react-native';
import AsyncStorage from '@react-native-community/async-storage';
import Colors from 'styles/colors';
import Constants from 'constants';
import firstRunStyle from 'styles/firstRun';

class EmailCollectPage extends React.PureComponent {
  state = {
    email: null,
    placeholder: 'you@example.com',
    verifying: true,
  };

  componentWillReceiveProps(nextProps) {
    const { showNextPage } = this.props;
    const { user } = nextProps;

    if (this.state.verifying) {
      if (user && user.primary_email && user.has_verified_email) {
        if (showNextPage) {
          showNextPage();
        }
      } else {
        this.setState({ verifying: false });
      }
    }
  }

  handleChangeText = text => {
    // save the value to the state email
    const { onEmailChanged } = this.props;
    this.setState({ email: text });
    AsyncStorage.setItem(Constants.KEY_FIRST_RUN_EMAIL, text);
    AsyncStorage.setItem(Constants.KEY_EMAIL_VERIFY_PENDING, 'true');
    if (onEmailChanged) {
      onEmailChanged(text);
    }
  };

  render() {
    const { onEmailViewLayout } = this.props;

    const content = (
      <View onLayout={() => onEmailViewLayout('collect')}>
        <Text style={firstRunStyle.title}>Setup account</Text>
        <TextInput
          style={firstRunStyle.emailInput}
          placeholder={this.state.placeholder}
          underlineColorAndroid="transparent"
          selectionColor={Colors.NextLbryGreen}
          value={this.state.email}
          onChangeText={text => this.handleChangeText(text)}
          onFocus={() => {
            if (!this.state.email || this.state.email.length === 0) {
              this.setState({ placeholder: '' });
            }
          }}
          onBlur={() => {
            if (!this.state.email || this.state.email.length === 0) {
              this.setState({ placeholder: 'you@example.com' });
            }
          }}
        />
        <Text style={firstRunStyle.paragraph}>
          An account will allow you to earn rewards and keep your account and settings synced.
        </Text>
        <Text style={firstRunStyle.infoParagraph}>
          This information is disclosed only to LBRY, Inc. and not to the LBRY network.
        </Text>
      </View>
    );

    return <View style={firstRunStyle.container}>{content}</View>;
  }
}

export default EmailCollectPage;
