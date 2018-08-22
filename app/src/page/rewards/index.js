import { connect } from 'react-redux';
import {
  doRewardList,
  selectEmailVerifyErrorMessage,
  selectEmailVerifyIsPending,
  selectFetchingRewards,
  selectUnclaimedRewards,
  selectClaimedRewards,
  selectUser
} from 'lbryinc';
import { doNotify } from 'lbry-redux';
import RewardsPage from './view';

const select = state => ({
  emailVerifyErrorMessage: selectEmailVerifyErrorMessage(state),
  emailVerifyPending: selectEmailVerifyIsPending(state),
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