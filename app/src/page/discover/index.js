import { connect } from 'react-redux';
import {
  doFetchFeaturedUris,
  selectBalance,
  selectFeaturedUris,
  selectFetchingFeaturedUris,
} from 'lbry-redux';
import {
  doFetchRewardedContent,
  doFetchMySubscriptions,
  doRemoveUnreadSubscriptions,
  selectEnabledChannelNotifications,
  selectSubscriptionClaims,
  selectUnreadSubscriptions,
} from 'lbryinc';
import DiscoverPage from './view';

const select = state => ({
  allSubscriptions: selectSubscriptionClaims(state),
  balance: selectBalance(state),
  enabledChannelNotifications: selectEnabledChannelNotifications(state),
  featuredUris: selectFeaturedUris(state),
  fetchingFeaturedUris: selectFetchingFeaturedUris(state),
  unreadSubscriptions: selectUnreadSubscriptions(state),
});

const perform = dispatch => ({
  fetchFeaturedUris: () => dispatch(doFetchFeaturedUris()),
  fetchRewardedContent: () => dispatch(doFetchRewardedContent()),
  fetchSubscriptions: () => dispatch(doFetchMySubscriptions()),
  removeUnreadSubscriptions: () => dispatch(doRemoveUnreadSubscriptions()),
});

export default connect(select, perform)(DiscoverPage);