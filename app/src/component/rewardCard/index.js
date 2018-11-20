import { connect } from 'react-redux';
import { doToast } from 'lbry-redux';
import {
  doClaimRewardType,
  doClaimRewardClearError,
  makeSelectClaimRewardError,
  makeSelectIsRewardClaimPending,
} from 'lbryinc';
import RewardCard from './view';

const makeSelect = () => {
  const selectIsPending = makeSelectIsRewardClaimPending();
  const selectError = makeSelectClaimRewardError();

  const select = (state, props) => ({
    errorMessage: selectError(state, props),
    isPending: selectIsPending(state, props),
  });

  return select;
};

const perform = dispatch => ({
  claimReward: reward => dispatch(doClaimRewardType(reward.reward_type, true)),
  clearError: reward => dispatch(doClaimRewardClearError(reward)),
  notify: data => dispatch(doToast(data))
});

export default connect(makeSelect, perform)(RewardCard);
