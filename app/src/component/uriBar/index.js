import { connect } from 'react-redux';
import {
  doUpdateSearchQuery,
  selectSearchState as selectSearch,
  selectSearchValue,
  selectSearchSuggestions,
} from 'lbry-redux';
import { selectCurrentRoute } from 'redux/selectors/drawer';
import UriBar from './view';

const select = state => {
  const { ...searchState } = selectSearch(state);

  return {
    ...searchState,
    query: selectSearchValue(state),
    currentRoute: selectCurrentRoute(state),
    suggestions: selectSearchSuggestions(state),
  };
};

const perform = dispatch => ({
  updateSearchQuery: query => dispatch(doUpdateSearchQuery(query)),
});

export default connect(
  select,
  perform
)(UriBar);
