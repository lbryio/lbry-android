import { connect } from 'react-redux';
import {
  doFetchTransactions,
  selectTransactionItems,
  selectIsFetchingTransactions,
} from 'lbry-redux';
import { doPushDrawerStack } from 'redux/actions/drawer';
import Constants from 'constants';
import TransactionHistoryPage from './view';

const select = state => ({
  fetchingTransactions: selectIsFetchingTransactions(state),
  transactions: selectTransactionItems(state),
});

const perform = dispatch => ({
  fetchTransactions: () => dispatch(doFetchTransactions()),
  pushDrawerStack: () => dispatch(doPushDrawerStack(Constants.DRAWER_ROUTE_TRANSACTION_HISTORY)),
});

export default connect(select, perform)(TransactionHistoryPage);
