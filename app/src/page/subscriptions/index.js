import { connect } from 'react-redux';
import {
  doFetchMySubscriptions,
  doSetViewMode,
  doFetchRecommendedSubscriptions,
  doCompleteFirstRun,
  doShowSuggestedSubs,
  selectSubscriptionClaims,
  selectSubscriptions,
  selectSubscriptionsBeingFetched,
  selectIsFetchingSubscriptions,
  selectUnreadSubscriptions,
  selectViewMode,
  selectFirstRunCompleted,
  selectShowSuggestedSubs
} from 'lbryinc';
import SubscriptionsPage from './view';

const select = state => ({
  loading:
    selectIsFetchingSubscriptions(state) ||
    Boolean(Object.keys(selectSubscriptionsBeingFetched(state)).length),
  subscribedChannels: selectSubscriptions(state),
  allSubscriptions: selectSubscriptionClaims(state),
  unreadSubscriptions: selectUnreadSubscriptions(state),
  viewMode: selectViewMode(state),
  firstRunCompleted: selectFirstRunCompleted(state),
  showSuggestedSubs: selectShowSuggestedSubs(state),
});

export default connect(
  select,
  {
    doFetchMySubscriptions,
    doSetViewMode,
    doFetchRecommendedSubscriptions,
    doCompleteFirstRun,
    doShowSuggestedSubs,
  }
)(SubscriptionsPage);
