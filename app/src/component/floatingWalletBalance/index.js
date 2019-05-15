import { connect } from 'react-redux';
import { makeSelectClientSetting } from 'redux/selectors/settings';
import { selectBalance } from 'lbry-redux';
import { selectUnclaimedRewardValue } from 'lbryinc';
import Constants from 'constants';
import FloatingWalletBalance from './view';

const select = state => ({
  balance: selectBalance(state),
  unclaimedRewardAmount: selectUnclaimedRewardValue(state),
  rewardsNotInterested: makeSelectClientSetting(Constants.SETTING_REWARDS_NOT_INTERESTED)(state),
});

export default connect(select, null)(FloatingWalletBalance);
