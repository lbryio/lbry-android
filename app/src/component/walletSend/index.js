import { connect } from 'react-redux';
import {
  doNotify,
  doSendDraftTransaction,
  selectDraftTransaction,
  selectDraftTransactionError,
  selectBalance
} from 'lbry-redux';
import WalletSend from './view';

const perform = dispatch => ({
  sendToAddress: (address, amount) => dispatch(doSendDraftTransaction(address, amount)),
  notify: (data) => dispatch(doNotify(data))
});

const select = state => ({
  balance: selectBalance(state),
  draftTransaction: selectDraftTransaction(state),
  transactionError: selectDraftTransactionError(state),
});

export default connect(select, perform)(WalletSend);
