import { connect } from 'react-redux';
import { doToast } from 'lbry-redux';
import {
  doAuthenticate,
  doUserEmailNew,
  doUserResendVerificationEmail,
  selectAuthToken,
  selectEmailNewErrorMessage,
  selectEmailNewIsPending,
  selectEmailToVerify,
  selectAuthenticationIsPending,
  selectUser,
} from 'lbryinc';
import FirstRun from './view';

const select = (state) => ({
  authenticating: selectAuthenticationIsPending(state),
  authToken: selectAuthToken(state),
  emailToVerify: selectEmailToVerify(state),
  emailNewErrorMessage: selectEmailNewErrorMessage(state),
  emailNewPending: selectEmailNewIsPending(state),
  user: selectUser(state),
});

const perform = dispatch => ({
  addUserEmail: email => dispatch(doUserEmailNew(email)),
  authenticate: (appVersion, os) => dispatch(doAuthenticate(appVersion, os)),
  notify: data => dispatch(doToast(data)),
  resendVerificationEmail: email => dispatch(doUserResendVerificationEmail(email))
});

export default connect(select, perform)(FirstRun);
