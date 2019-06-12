import { connect } from 'react-redux';
import { doFetchTrendingUris, selectTrendingUris, selectFetchingTrendingUris } from 'lbryinc';
import { doPushDrawerStack, doSetPlayerVisible } from 'redux/actions/drawer';
import { selectCurrentRoute } from 'redux/selectors/drawer';
import Constants from 'constants';
import TrendingPage from './view';

const select = state => ({
  currentRoute: selectCurrentRoute(state),
  trendingUris: selectTrendingUris(state),
  fetchingTrendingUris: selectFetchingTrendingUris(state),
});

const perform = dispatch => ({
  fetchTrendingUris: () => dispatch(doFetchTrendingUris()),
  pushDrawerStack: () => dispatch(doPushDrawerStack(Constants.DRAWER_ROUTE_TRENDING)),
  setPlayerVisible: () => dispatch(doSetPlayerVisible(false)),
});

export default connect(
  select,
  perform
)(TrendingPage);
