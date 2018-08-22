import { connect } from 'react-redux';
import { doUpdateSearchQuery, selectSearchState as selectSearch, selectSearchSuggestions } from 'lbry-redux';
import UriBar from './view';

const select = state => {
  const { ...searchState } = selectSearch(state);
  
  return {
    ...searchState,
    suggestions: selectSearchSuggestions(state)
  };
};

const perform = dispatch => ({
  updateSearchQuery: query => dispatch(doUpdateSearchQuery(query)),
});

export default connect(select, perform)(UriBar);
