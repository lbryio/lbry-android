import { connect } from 'react-redux';
import { doRewardList, selectUnclaimedRewardValue, selectFetchingRewards, selectUser } from 'lbryinc';
import RewardSummary from './view';

const select = state => ({
  unclaimedRewardAmount: selectUnclaimedRewardValue(state),
  fetching: selectFetchingRewards(state),
  user: selectUser(state)
});

const perform = dispatch => ({
  fetchRewards: () => dispatch(doRewardList()),
});

export default connect(select, perform)(RewardSummary);
