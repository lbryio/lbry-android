import { connect } from 'react-redux';
import { doFetchTrendingUris, selectTrendingUris, selectFetchingTrendingUris } from 'lbry-redux';
import TrendingPage from './view';

const select = state => ({
  trendingUris: selectTrendingUris(state),
  fetchingTrendingUris: selectFetchingTrendingUris(state),
});

const perform = dispatch => ({
  fetchTrendingUris: () => dispatch(doFetchTrendingUris()),
});

export default connect(select, perform)(TrendingPage);