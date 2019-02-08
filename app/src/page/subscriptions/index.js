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
import { doPushDrawerStack } from 'redux/actions/drawer';
import Constants from 'constants';
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

const perform = dispatch => ({
  doFetchMySubscriptions: () => dispatch(doFetchMySubscriptions()),
  doFetchRecommendedSubscriptions: () => dispatch(doFetchRecommendedSubscriptions()),
  pushDrawerStack: () => dispatch(doPushDrawerStack(Constants.DRAWER_ROUTE_SUBSCRIPTIONS))
});

export default connect(select, perform)(SubscriptionsPage);
