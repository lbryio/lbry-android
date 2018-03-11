import { connect } from 'react-redux';
import { doFetchFeaturedUris, selectFeaturedUris, selectFetchingFeaturedUris } from 'lbry-redux';
import DiscoverPage from './view';

const select = state => ({
  featuredUris: selectFeaturedUris(state),
  fetchingFeaturedUris: selectFetchingFeaturedUris(state),
});

const perform = dispatch => ({
  fetchFeaturedUris: () => dispatch(doFetchFeaturedUris()),
});

export default connect(select, perform)(DiscoverPage);