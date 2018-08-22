import { connect } from 'react-redux';
import {
  doFetchFeaturedUris,
  selectBalance,
  selectFeaturedUris,
  selectFetchingFeaturedUris
} from 'lbry-redux';
import DiscoverPage from './view';

const select = state => ({
  balance: selectBalance(state),
  featuredUris: selectFeaturedUris(state),
  fetchingFeaturedUris: selectFetchingFeaturedUris(state),
});

const perform = dispatch => ({
  fetchFeaturedUris: () => dispatch(doFetchFeaturedUris()),
});

export default connect(select, perform)(DiscoverPage);