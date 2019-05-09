import { connect } from 'react-redux';
import { doToast } from 'lbry-redux';
import {
  doUserEmailNew,
  doUserEmailToVerify,
  selectEmailNewErrorMessage,
  selectEmailNewIsPending,
  selectEmailToVerify,
  doUserResendVerificationEmail,
} from 'lbryinc';
import Verification from './view';

const select = (state) => ({
  emailToVerify: selectEmailToVerify(state),
  emailNewErrorMessage: selectEmailNewErrorMessage(state),
  emailNewPending: selectEmailNewIsPending(state),
});

const perform = dispatch => ({
  addUserEmail: email => dispatch(doUserEmailNew(email)),
  notify: data => dispatch(doToast(data)),
  setEmailToVerify: email => dispatch(doUserEmailToVerify(email)),
  resendVerificationEmail: email => dispatch(doUserResendVerificationEmail(email))
});

export default connect(select, perform)(Verification);
