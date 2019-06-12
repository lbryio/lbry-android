import { connect } from 'react-redux';
import {
  doClaimRewardType,
  doRewardList,
  selectEmailVerifyErrorMessage,
  selectEmailVerifyIsPending,
  selectFetchingRewards,
  selectUnclaimedRewards,
  selectClaimedRewards,
  selectUser,
} from 'lbryinc';
import { doToast } from 'lbry-redux';
import { doPushDrawerStack, doSetPlayerVisible } from 'redux/actions/drawer';
import { selectCurrentRoute } from 'redux/selectors/drawer';
import Constants from 'constants';
import RewardsPage from './view';

const select = state => ({
  claimed: selectClaimedRewards(state),
  currentRoute: selectCurrentRoute(state),
  emailVerifyErrorMessage: selectEmailVerifyErrorMessage(state),
  emailVerifyPending: selectEmailVerifyIsPending(state),
  fetching: selectFetchingRewards(state),
  rewards: selectUnclaimedRewards(state),
  user: selectUser(state),
});

const perform = dispatch => ({
  claimReward: reward => dispatch(doClaimRewardType(reward.reward_type, true)),
  fetchRewards: () => dispatch(doRewardList()),
  notify: data => dispatch(doToast(data)),
  pushDrawerStack: () => dispatch(doPushDrawerStack(Constants.DRAWER_ROUTE_REWARDS)),
  setPlayerVisible: () => dispatch(doSetPlayerVisible(false)),
});

export default connect(
  select,
  perform
)(RewardsPage);
