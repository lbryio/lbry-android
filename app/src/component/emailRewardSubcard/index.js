import { connect } from 'react-redux';
import {
  doUserEmailNew,
  doUserResendVerificationEmail,
  selectEmailNewErrorMessage,
  selectEmailNewIsPending,
  selectEmailToVerify,
} from 'lbryinc';
import { doToast } from 'lbry-redux';
import EmailRewardSubcard from './view';

const select = state => ({
  emailToVerify: selectEmailToVerify(state),
  emailNewErrorMessage: selectEmailNewErrorMessage(state),
  emailNewPending: selectEmailNewIsPending(state)
});

const perform = dispatch => ({
  addUserEmail: email => dispatch(doUserEmailNew(email)),
  notify: data => dispatch(doToast(data)),
  resendVerificationEmail: email => dispatch(doUserResendVerificationEmail(email))
});

export default connect(select, perform)(EmailRewardSubcard);
