import { connect } from 'react-redux';
import { doToast } from 'lbry-redux';
import {
  doGenerateAuthToken,
  doUserEmailNew,
  selectAuthToken,
  selectEmailNewErrorMessage,
  selectEmailNewIsPending,
  selectEmailToVerify,
  selectIsAuthenticating
} from 'lbryinc';
import FirstRun from './view';

const select = (state) => ({
  authenticating: selectIsAuthenticating(state),
  authToken: selectAuthToken(state),
  emailToVerify: selectEmailToVerify(state),
  emailNewErrorMessage: selectEmailNewErrorMessage(state),
  emailNewPending: selectEmailNewIsPending(state),
});

const perform = dispatch => ({
  addUserEmail: email => dispatch(doUserEmailNew(email)),
  generateAuthToken: installationId => dispatch(doGenerateAuthToken(installationId)),
  notify: data => dispatch(doToast(data))
});

export default connect(select, perform)(FirstRun);
