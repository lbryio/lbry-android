import { connect } from 'react-redux';
import {
  doFetchMySubscriptions,
  doSetViewMode,
  doFetchRecommendedSubscriptions,
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
import { doSetClientSetting } from 'redux/actions/settings';
import { makeSelectClientSetting } from 'redux/selectors/settings';
import Constants from 'constants';
import SubscriptionsPage from './view';

const select = state => ({
  loading:
    selectIsFetchingSubscriptions(state) ||
    Boolean(Object.keys(selectSubscriptionsBeingFetched(state)).length),
  subscribedChannels: selectSubscriptions(state),
  subscriptionsViewMode: makeSelectClientSetting(Constants.SETTING_SUBSCRIPTIONS_VIEW_MODE)(state),
  allSubscriptions: selectSubscriptionClaims(state),
  unreadSubscriptions: selectUnreadSubscriptions(state),
  viewMode: selectViewMode(state),
  firstRunCompleted: selectFirstRunCompleted(state),
  showSuggestedSubs: selectShowSuggestedSubs(state),
});

const perform = dispatch => ({
  doFetchMySubscriptions: () => dispatch(doFetchMySubscriptions()),
  doFetchRecommendedSubscriptions: () => dispatch(doFetchRecommendedSubscriptions()),
  doSetViewMode: (viewMode) => dispatch(doSetViewMode(viewMode)),
  pushDrawerStack: () => dispatch(doPushDrawerStack(Constants.DRAWER_ROUTE_SUBSCRIPTIONS)),
  setClientSetting: (key, value) => dispatch(doSetClientSetting(key, value)),
});

export default connect(select, perform)(SubscriptionsPage);
