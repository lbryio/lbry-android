import { connect } from 'react-redux';
import { selectBalance } from 'lbry-redux';
import FloatingWalletBalance from './view';
import { doRewardList, selectUnclaimedRewardValue, selectFetchingRewards, selectUser } from 'lbryinc';

const select = state => ({
  balance: selectBalance(state),
  unclaimedRewardAmount: selectUnclaimedRewardValue(state),
});

export default connect(select, null)(FloatingWalletBalance);
