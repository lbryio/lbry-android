import { connect } from 'react-redux';
import { doBalanceSubscribe, doNotify } from 'lbry-redux';
import {
  doAuthenticate,
  doFetchRewardedContent,
  doUserEmailToVerify,
  doUserEmailVerify,
  doUserEmailVerifyFailure,
  selectUser,
  selectEmailToVerify
} from 'lbryinc';
import { doDeleteCompleteBlobs } from '../../redux/actions/file';
import SplashScreen from './view';

const select = state => ({
  user: selectUser(state),
  emailToVerify: selectEmailToVerify(state)
});

const perform = dispatch => ({
  authenticate: (appVersion, os) => dispatch(doAuthenticate(appVersion, os)),
  balanceSubscribe: () => dispatch(doBalanceSubscribe()),
  deleteCompleteBlobs: () => dispatch(doDeleteCompleteBlobs()),
  fetchRewardedContent: () => dispatch(doFetchRewardedContent()),
  notify: data => dispatch(doNotify(data)),
  setEmailToVerify: email => dispatch(doUserEmailToVerify(email)),
  verifyUserEmail: (token, recaptcha) => dispatch(doUserEmailVerify(token, recaptcha)),
  verifyUserEmailFailure: error => dispatch(doUserEmailVerifyFailure(error)),
});

export default connect(select, perform)(SplashScreen);
