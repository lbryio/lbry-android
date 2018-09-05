import { connect } from 'react-redux';
import {
  doSearch,
  makeSelectSearchUris,
  selectIsSearching,
  selectSearchValue
} from 'lbry-redux';
import SearchPage from './view';

const select = (state) => ({
  isSearching: selectIsSearching(state),
  query: selectSearchValue(state),
  uris: makeSelectSearchUris(selectSearchValue(state))(state)
});

const perform = dispatch => ({
  search: (query) => dispatch(doSearch(query, 25))
});

export default connect(select, perform)(SearchPage);
