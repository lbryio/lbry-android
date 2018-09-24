import { connect } from 'react-redux';
import { doNotify } from 'lbry-redux';
import { doRewardList, selectUnclaimedRewardValue, selectFetchingRewards, selectUser } from 'lbryinc';
import RewardSummary from './view';

const select = state => ({
  unclaimedRewardAmount: selectUnclaimedRewardValue(state),
  fetching: selectFetchingRewards(state),
  user: selectUser(state)
});

const perform = dispatch => ({
  fetchRewards: () => dispatch(doRewardList()),
  notify: data => dispatch(doNotify(data))
});

export default connect(select, perform)(RewardSummary);
