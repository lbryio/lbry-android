import { connect } from 'react-redux';
import {
  doUserEmailNew,
  selectEmailNewErrorMessage,
  selectEmailNewIsPending,
  selectEmailToVerify
} from 'lbryinc';
import { doNotify } from 'lbry-redux';
import EmailRewardSubcard from './view';

const select = state => ({
  emailToVerify: selectEmailToVerify(state),
  emailNewErrorMessage: selectEmailNewErrorMessage(state),
  emailNewPending: selectEmailNewIsPending(state)
});

const perform = dispatch => ({
  addUserEmail: email => dispatch(doUserEmailNew(email)),
  notify: data => dispatch(doNotify(data))
});

export default connect(select, perform)(EmailRewardSubcard);