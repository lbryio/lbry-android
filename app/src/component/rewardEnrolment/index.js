import { connect } from 'react-redux';
import { doToast } from 'lbry-redux';
import { doSetClientSetting } from 'redux/actions/settings';
import { doRewardList, selectUnclaimedRewardValue, selectFetchingRewards, selectUser } from 'lbryinc';
import RewardEnrolment from './view';

const select = state => ({
  unclaimedRewardAmount: selectUnclaimedRewardValue(state),
  fetching: selectFetchingRewards(state),
  user: selectUser(state)
});

const perform = dispatch => ({
  fetchRewards: () => dispatch(doRewardList()),
  notify: data => dispatch(doToast(data)),
  setClientSetting: (key, value) => dispatch(doSetClientSetting(key, value)),
});

export default connect(select, perform)(RewardEnrolment);
