import { connect } from 'react-redux';
import { doToast } from 'lbry-redux';
import {
  doAuthenticate,
  doUserEmailNew,
  selectAuthToken,
  selectEmailNewErrorMessage,
  selectEmailNewIsPending,
  selectEmailToVerify,
  selectAuthenticationIsPending
} from 'lbryinc';
import FirstRun from './view';

const select = (state) => ({
  authenticating: selectAuthenticationIsPending(state),
  authToken: selectAuthToken(state),
  emailToVerify: selectEmailToVerify(state),
  emailNewErrorMessage: selectEmailNewErrorMessage(state),
  emailNewPending: selectEmailNewIsPending(state),
});

const perform = dispatch => ({
  addUserEmail: email => dispatch(doUserEmailNew(email)),
  authenticate: (appVersion, os) => dispatch(doAuthenticate(appVersion, os)),
  notify: data => dispatch(doToast(data))
});

export default connect(select, perform)(FirstRun);
