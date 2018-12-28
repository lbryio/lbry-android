import { connect } from 'react-redux';
import { doBalanceSubscribe, doBlackListedOutpointsSubscribe, doToast } from 'lbry-redux';
import {
  doAuthenticate,
  doCheckSubscriptionsInit,
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
  blacklistedOutpointsSubscribe: () => dispatch(doBlackListedOutpointsSubscribe()),
  checkSubscriptionsInit: () => dispatch(doCheckSubscriptionsInit()),
  deleteCompleteBlobs: () => dispatch(doDeleteCompleteBlobs()),
  fetchRewardedContent: () => dispatch(doFetchRewardedContent()),
  notify: data => dispatch(doToast(data)),
  setEmailToVerify: email => dispatch(doUserEmailToVerify(email)),
  verifyUserEmail: (token, recaptcha) => dispatch(doUserEmailVerify(token, recaptcha)),
  verifyUserEmailFailure: error => dispatch(doUserEmailVerifyFailure(error)),
});

export default connect(select, perform)(SplashScreen);
