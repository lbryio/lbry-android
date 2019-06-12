import { connect } from 'react-redux';
import { doBalanceSubscribe, doUpdateBlockHeight, doToast } from 'lbry-redux';
import {
  doAuthenticate,
  doBlackListedOutpointsSubscribe,
  doCheckSubscriptionsInit,
  doFetchMySubscriptions,
  doFetchRewardedContent,
  doGetSync,
  doUserEmailToVerify,
  doUserEmailVerify,
  doUserEmailVerifyFailure,
  selectUser,
  selectEmailToVerify,
} from 'lbryinc';
import { doSetClientSetting } from 'redux/actions/settings';
import SplashScreen from './view';

const select = state => ({
  user: selectUser(state),
  emailToVerify: selectEmailToVerify(state),
});

const perform = dispatch => ({
  authenticate: (appVersion, os) => dispatch(doAuthenticate(appVersion, os)),
  balanceSubscribe: () => dispatch(doBalanceSubscribe()),
  blacklistedOutpointsSubscribe: () => dispatch(doBlackListedOutpointsSubscribe()),
  checkSubscriptionsInit: () => dispatch(doCheckSubscriptionsInit()),
  fetchRewardedContent: () => dispatch(doFetchRewardedContent()),
  fetchSubscriptions: callback => dispatch(doFetchMySubscriptions(callback)),
  getSync: password => dispatch(doGetSync(password)),
  notify: data => dispatch(doToast(data)),
  setClientSetting: (key, value) => dispatch(doSetClientSetting(key, value)),
  setEmailToVerify: email => dispatch(doUserEmailToVerify(email)),
  updateBlockHeight: () => dispatch(doUpdateBlockHeight()),
  verifyUserEmail: (token, recaptcha) => dispatch(doUserEmailVerify(token, recaptcha)),
  verifyUserEmailFailure: error => dispatch(doUserEmailVerifyFailure(error)),
});

export default connect(
  select,
  perform
)(SplashScreen);
