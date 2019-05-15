import { connect } from 'react-redux';
import { doToast } from 'lbry-redux';
import {
  doUserEmailNew,
  doUserEmailToVerify,
  doUserResendVerificationEmail,
  doUserPhoneNew,
  doUserPhoneVerify,
  selectPhoneNewErrorMessage,
  selectPhoneNewIsPending,
  selectPhoneToVerify,
  selectPhoneVerifyIsPending,
  selectPhoneVerifyErrorMessage,
  selectEmailNewErrorMessage,
  selectEmailNewIsPending,
  selectEmailToVerify,
  selectUser,
} from 'lbryinc';
import Verification from './view';

const select = (state) => ({
  emailToVerify: selectEmailToVerify(state),
  emailNewErrorMessage: selectEmailNewErrorMessage(state),
  emailNewPending: selectEmailNewIsPending(state),
  user: selectUser(state),
  phoneVerifyErrorMessage: selectPhoneVerifyErrorMessage(state),
  phoneVerifyIsPending: selectPhoneVerifyIsPending(state),
  phone: selectPhoneToVerify(state),
  phoneNewErrorMessage: selectPhoneNewErrorMessage(state),
  phoneNewIsPending: selectPhoneNewIsPending(state),
});

const perform = dispatch => ({
  addUserEmail: email => dispatch(doUserEmailNew(email)),
  addUserPhone: (phone, country_code) => dispatch(doUserPhoneNew(phone, country_code)),
  verifyPhone: (verificationCode) => dispatch(doUserPhoneVerify(verificationCode)),
  notify: data => dispatch(doToast(data)),
  setEmailToVerify: email => dispatch(doUserEmailToVerify(email)),
  resendVerificationEmail: email => dispatch(doUserResendVerificationEmail(email))
});

export default connect(select, perform)(Verification);
