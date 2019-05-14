import React from 'react';
import { Lbry } from 'lbry-redux';
import {
  ActivityIndicator,
  Linking,
  NativeModules,
  Text,
  TouchableOpacity,
  View
} from 'react-native';
import { NavigationActions, StackActions } from 'react-navigation';
import AsyncStorage from '@react-native-community/async-storage';
import Colors from 'styles/colors';
import Constants from 'constants';
import EmailVerifyPage from './internal/email-verify-page';
import ManualVerifyPage from './internal/manual-verify-page';
import PhoneVerifyPage from './internal/phone-verify-page';
import firstRunStyle from 'styles/firstRun';

class VerificationScreen extends React.PureComponent {
  state = {
    currentPage: null,
    emailSubmitted: false,
    isFirstRun: false,
    launchUrl: null,
    showSkip: false,
    skipAccountConfirmed: false,
    showBottomContainer: true,
    walletPassword: null,
    isEmailVerified: false,
    isIdentityVerified: false,
    isRewardApproved: false
  };

  componentDidMount() {
    const { user } = this.props;
    this.checkVerificationStatus(user);
  }

  checkVerificationStatus = (user) => {
    const { navigation } = this.props;

    this.setState({
      isEmailVerified: (user && user.primary_email && user.has_verified_email),
      isIdentityVerified: (user && user.is_identity_verified),
      isRewardApproved: (user && user.is_reward_approved)
    }, () => {
      if (!this.state.isEmailVerified) {
        this.setState({ currentPage: 'emailVerify' });
      }
      if (this.state.isEmailVerified && !this.state.isIdentityVerified) {
        this.setState({ currentPage: 'phoneVerify' });
      }
      if (this.state.isEmailVerified && this.state.isIdentityVerified && !this.state.isRewardApproved) {
        this.setState({ currentPage: 'manualVerify' });
      }

      if (this.state.isEmailVerified && this.state.isIdentityVerified && this.state.isRewardApproved) {
        // verification steps already completed
        // simply navigate back to the rewards page
        navigation.goBack();
      }
    });
  }

  componentWillReceiveProps(nextProps) {
    const { user } = nextProps;
    this.checkVerificationStatus(user);
  }

  onCloseButtonPressed = () => {
    const { navigation } = this.props;
    navigation.goBack();
  }

  render() {
    const {
      addUserEmail,
      emailNewErrorMessage,
      emailNewPending,
      emailToVerify,
      navigation,
      notify,
      addUserPhone,
      phone,
      phoneVerifyIsPending,
      phoneVerifyErrorMessage,
      phoneNewIsPending,
      phoneNewErrorMessage,
      resendVerificationEmail,
      verifyPhone
    } = this.props;

    let page = null;
    switch (this.state.currentPage) {
      case 'emailVerify':
        page = (
          <EmailVerifyPage
            addUserEmail={addUserEmail}
            emailNewErrorMessage={emailNewErrorMessage}
            emailNewPending={emailNewPending}
            emailToVerify={emailToVerify}
            notify={notify}
            resendVerificationEmail={resendVerificationEmail}
          />
        );
        break;
      case 'phoneVerify':
        page = (
          <PhoneVerifyPage
            addUserPhone={addUserPhone}
            phone={phone}
            phoneVerifyIsPending={phoneVerifyIsPending}
            phoneVerifyErrorMessage={phoneVerifyErrorMessage}
            phoneNewIsPending={phoneNewIsPending}
            phoneNewErrorMessage={phoneNewErrorMessage}
            notify={notify}
            verifyPhone={verifyPhone}
          />
        );
        break;
      case 'manualVerify':
        page = (
          <ManualVerifyPage />
        );
    }

    return (
      <View style={firstRunStyle.screenContainer}>
        {page}

        <TouchableOpacity style={firstRunStyle.closeButton} onPress={this.onCloseButtonPressed}>
          <Text style={firstRunStyle.closeButtonText}>x</Text>
        </TouchableOpacity>
      </View>
    );
  }
}

export default VerificationScreen;
