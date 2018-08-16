import { connect } from 'react-redux';
import { doBalanceSubscribe, doNotify } from 'lbry-redux';
import {
  doAuthenticate,
  doUserEmailToVerify,
  doUserEmailVerify,
  doUserEmailVerifyFailure,
  selectUser,
  selectEmailToVerify
} from 'lbryinc';
import SplashScreen from './view';

const select = state => ({
  user: selectUser(state),
  emailToVerify: selectEmailToVerify(state)
});

const perform = dispatch => ({
    authenticate: (appVersion, deviceId) => dispatch(doAuthenticate(appVersion, deviceId)),
    balanceSubscribe: () => dispatch(doBalanceSubscribe()),
    notify: data => dispatch(doNotify(data)),
    setEmailToVerify: email => dispatch(doUserEmailToVerify(email)),
    verifyUserEmail: (token, recaptcha) => dispatch(doUserEmailVerify(token, recaptcha)),
    verifyUserEmailFailure: error => dispatch(doUserEmailVerifyFailure(error)),
});

export default connect(select, perform)(SplashScreen);
