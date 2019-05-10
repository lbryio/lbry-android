import { connect } from 'react-redux';
import { selectBalance } from 'lbry-redux';
import { selectUnclaimedRewardValue } from 'lbryinc';
import FloatingWalletBalance from './view';

const select = state => ({
  balance: selectBalance(state),
  unclaimedRewardAmount: selectUnclaimedRewardValue(state),
});

export default connect(select, null)(FloatingWalletBalance);
