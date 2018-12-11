import { connect } from 'react-redux';
import { doToast } from 'lbry-redux';
import {
  doClaimRewardType,
  doClaimRewardClearError,
  makeSelectClaimRewardError,
  makeSelectIsRewardClaimPending,
  rewards as REWARD_TYPES
} from 'lbryinc';
import CustomRewardCard from './view';

const select = state => ({
  rewardIsPending: makeSelectIsRewardClaimPending()(state, {
    reward_type: REWARD_TYPES.TYPE_REWARD_CODE,
  }),
  error: makeSelectClaimRewardError()(state, { reward_type: REWARD_TYPES.TYPE_REWARD_CODE }),
});

const perform = dispatch => ({
  claimReward: reward => dispatch(doClaimRewardType(reward.reward_type, true)),
  clearError: reward => dispatch(doClaimRewardClearError(reward)),
  notify: data => dispatch(doToast(data)),
  submitRewardCode: code => dispatch(doClaimRewardType(REWARD_TYPES.TYPE_REWARD_CODE, { params: { code } }))
});

export default connect(select, perform)(CustomRewardCard);
