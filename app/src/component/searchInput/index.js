import { connect } from 'react-redux';
import { doSearch, doUpdateSearchQuery }  from 'lbry-redux';
import SearchInput from './view';

const perform = dispatch => ({
  search: search => dispatch(doSearch(search)),
  updateSearchQuery: query => dispatch(doUpdateSearchQuery(query, false))
});

export default connect(null, perform)(SearchInput);
