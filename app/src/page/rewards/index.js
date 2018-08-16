import { connect } from 'react-redux';
import {
  doRewardList,
  selectFetchingRewards,
  selectUnclaimedRewards,
  selectClaimedRewards,
  selectUser
} from 'lbryinc';
import { doNotify } from 'lbry-redux';
import RewardsPage from './view';

const select = state => ({
  fetching: selectFetchingRewards(state),
  rewards: selectUnclaimedRewards(state),
  claimed: selectClaimedRewards(state),
  user: selectUser(state),
});

const perform = dispatch => ({
  fetchRewards: () => dispatch(doRewardList()),
  notify: data => dispatch(doNotify(data))
});

export default connect(select, perform)(RewardsPage);