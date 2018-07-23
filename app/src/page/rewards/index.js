import { connect } from 'react-redux';
import {
  doRewardList,
  selectFetchingRewards,
  selectUnclaimedRewards,
  selectClaimedRewards,
  selectUser
} from 'lbryinc';
import RewardsPage from './view';

const select = state => ({
  fetching: selectFetchingRewards(state),
  rewards: selectUnclaimedRewards(state),
  /*claimed: selectClaimedRewards(state),*/
  user: selectUser(state),
});

const perform = dispatch => ({
  fetchRewards: () => dispatch(doRewardList()),
});

export default connect(select, perform)(RewardsPage);