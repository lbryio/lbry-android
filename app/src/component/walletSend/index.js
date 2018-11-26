import { connect } from 'react-redux';
import {
  doToast,
  doSendDraftTransaction,
  selectDraftTransaction,
  selectDraftTransactionError,
  selectBalance
} from 'lbry-redux';
import WalletSend from './view';

const select = state => ({
  balance: selectBalance(state),
  draftTransaction: selectDraftTransaction(state),
  transactionError: selectDraftTransactionError(state),
});

const perform = dispatch => ({
  sendToAddress: (address, amount) => dispatch(doSendDraftTransaction(address, amount)),
  notify: (data) => dispatch(doToast(data))
});

export default connect(select, perform)(WalletSend);
