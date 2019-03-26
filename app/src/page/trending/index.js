import { connect } from 'react-redux';
import { doFetchTrendingUris, selectTrendingUris, selectFetchingTrendingUris } from 'lbryinc';
import { doPushDrawerStack } from 'redux/actions/drawer';
import Constants from 'constants';
import TrendingPage from './view';

const select = state => ({
  trendingUris: selectTrendingUris(state),
  fetchingTrendingUris: selectFetchingTrendingUris(state),
});

const perform = dispatch => ({
  fetchTrendingUris: () => dispatch(doFetchTrendingUris()),
  pushDrawerStack: () => dispatch(doPushDrawerStack(Constants.DRAWER_ROUTE_TRENDING))
});

export default connect(select, perform)(TrendingPage);