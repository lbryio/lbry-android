import { connect } from 'react-redux';
import { doToast } from 'lbry-redux';
import {
  doAuthenticate,
  doCheckSync,
  doSyncApply,
  doUserEmailNew,
  doUserResendVerificationEmail,
  selectAuthToken,
  selectEmailNewErrorMessage,
  selectEmailNewIsPending,
  selectEmailToVerify,
  selectAuthenticationIsPending,
  selectHasSyncedWallet,
  selectGetSyncIsPending,
  selectSyncApplyIsPending,
  selectSyncApplyErrorMessage,
  selectSyncData,
  selectSyncHash,
  selectUser,
} from 'lbryinc';
import FirstRun from './view';

const select = (state) => ({
  authenticating: selectAuthenticationIsPending(state),
  authToken: selectAuthToken(state),
  emailToVerify: selectEmailToVerify(state),
  emailNewErrorMessage: selectEmailNewErrorMessage(state),
  emailNewPending: selectEmailNewIsPending(state),
  hasSyncedWallet: selectHasSyncedWallet(state),
  getSyncIsPending: selectGetSyncIsPending(state),
  syncApplyErrorMessage: selectSyncApplyErrorMessage(state),
  syncApplyIsPending: selectSyncApplyIsPending(state),
  syncHash: selectSyncHash(state),
  syncData: selectSyncData(state),
  user: selectUser(state),
});

const perform = dispatch => ({
  addUserEmail: email => dispatch(doUserEmailNew(email)),
  authenticate: (appVersion, os) => dispatch(doAuthenticate(appVersion, os)),
  syncApply: (hash, data, password) => dispatch(doSyncApply(hash, data, password)),
  checkSync: () => dispatch(doCheckSync()),
  notify: data => dispatch(doToast(data)),
  resendVerificationEmail: email => dispatch(doUserResendVerificationEmail(email))
});

export default connect(select, perform)(FirstRun);
