import { connect } from 'react-redux';
import { doFetchTransactions, selectTransactionItems, selectIsFetchingTransactions } from 'lbry-redux';
import { doPushDrawerStack, doSetPlayerVisible } from 'redux/actions/drawer';
import { selectCurrentRoute } from 'redux/selectors/drawer';
import Constants from 'constants';
import TransactionHistoryPage from './view';

const select = state => ({
  currentRoute: selectCurrentRoute(state),
  fetchingTransactions: selectIsFetchingTransactions(state),
  transactions: selectTransactionItems(state),
});

const perform = dispatch => ({
  fetchTransactions: () => dispatch(doFetchTransactions()),
  pushDrawerStack: () => dispatch(doPushDrawerStack(Constants.DRAWER_ROUTE_TRANSACTION_HISTORY)),
  setPlayerVisible: () => dispatch(doSetPlayerVisible(false)),
});

export default connect(
  select,
  perform
)(TransactionHistoryPage);
