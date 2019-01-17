import { connect } from 'react-redux';
import {
  doFetchFeaturedUris,
  selectBalance,
  selectFeaturedUris,
  selectFetchingFeaturedUris,
} from 'lbry-redux';
import { doFetchRewardedContent, selectSubscriptions, selectUnreadSubscriptions } from 'lbryinc';
import DiscoverPage from './view';

const select = state => ({
  balance: selectBalance(state),
  featuredUris: selectFeaturedUris(state),
  fetchingFeaturedUris: selectFetchingFeaturedUris(state),
  subscribedChannels: selectSubscriptions(state),
  unreadSubscriptions: selectUnreadSubscriptions(state)
});

const perform = dispatch => ({
  fetchFeaturedUris: () => dispatch(doFetchFeaturedUris()),
  fetchRewardedContent: () => dispatch(doFetchRewardedContent()),
});

export default connect(select, perform)(DiscoverPage);