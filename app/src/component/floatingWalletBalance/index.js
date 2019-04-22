import { connect } from 'react-redux';
import { selectTotalBalance } from 'lbry-redux';
import { selectUnclaimedRewardValue } from 'lbryinc';
import FloatingWalletBalance from './view';

const select = state => ({
  balance: selectTotalBalance(state),
  unclaimedRewardAmount: selectUnclaimedRewardValue(state),
});

export default connect(select, null)(FloatingWalletBalance);
