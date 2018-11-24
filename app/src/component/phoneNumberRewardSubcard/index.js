import { connect } from 'react-redux';
import { doToast } from 'lbry-redux';
import {
  doUserPhoneNew,
  doUserPhoneVerify,
  selectPhoneNewErrorMessage,
  selectPhoneNewIsPending,
  selectPhoneToVerify,
  selectPhoneVerifyIsPending,
  selectPhoneVerifyErrorMessage
} from 'lbryinc';
import PhoneNumberRewardSubcard from './view';

const select = state => ({
  phoneVerifyErrorMessage: selectPhoneVerifyErrorMessage(state),
  phoneVerifyIsPending: selectPhoneVerifyIsPending(state),
  phone: selectPhoneToVerify(state),
  phoneNewErrorMessage: selectPhoneNewErrorMessage(state),
  phoneNewIsPending: selectPhoneNewIsPending(state),
});

const perform = dispatch => ({
  addUserPhone: (phone, country_code) => dispatch(doUserPhoneNew(phone, country_code)),
  verifyPhone: (verificationCode) => dispatch(doUserPhoneVerify(verificationCode)),
  notify: data => dispatch(doToast(data)),
});

export default connect(select, perform)(PhoneNumberRewardSubcard);
