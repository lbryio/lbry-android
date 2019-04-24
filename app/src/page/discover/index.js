import { connect } from 'react-redux';
import {
  doFileList,
  selectBalance,
  selectFileInfosDownloaded,
} from 'lbry-redux';
import {
  doFetchFeaturedUris,
  doFetchRewardedContent,
  doFetchMySubscriptions,
  doRemoveUnreadSubscriptions,
  selectEnabledChannelNotifications,
  selectFeaturedUris,
  selectFetchingFeaturedUris,
  selectSubscriptionClaims,
  selectUnreadSubscriptions,
} from 'lbryinc';
import { doSetClientSetting } from 'redux/actions/settings';
import { makeSelectClientSetting } from 'redux/selectors/settings';
import Constants from 'constants';
import DiscoverPage from './view';

const select = state => ({
  allSubscriptions: selectSubscriptionClaims(state),
  balance: selectBalance(state),
  enabledChannelNotifications: selectEnabledChannelNotifications(state),
  featuredUris: selectFeaturedUris(state),
  fetchingFeaturedUris: selectFetchingFeaturedUris(state),
  fileInfos: selectFileInfosDownloaded(state),
  ratingReminderDisabled: makeSelectClientSetting(Constants.SETTING_RATING_REMINDER_DISABLED)(state),
  ratingReminderLastShown: makeSelectClientSetting(Constants.SETTING_RATING_REMINDER_LAST_SHOWN)(state),
  unreadSubscriptions: selectUnreadSubscriptions(state),
});

const perform = dispatch => ({
  fetchFeaturedUris: () => dispatch(doFetchFeaturedUris()),
  fetchRewardedContent: () => dispatch(doFetchRewardedContent()),
  fetchSubscriptions: () => dispatch(doFetchMySubscriptions()),
  fileList: () => dispatch(doFileList()),
  removeUnreadSubscriptions: () => dispatch(doRemoveUnreadSubscriptions()),
  setClientSetting: (key, value) => dispatch(doSetClientSetting(key, value))
});

export default connect(select, perform)(DiscoverPage);
