import { connect } from 'react-redux';
import { selectTotalBalance } from 'lbry-redux';
import WalletBalance from './view';

const select = state => ({
  balance: selectTotalBalance(state),
});

export default connect(select, null)(WalletBalance);
