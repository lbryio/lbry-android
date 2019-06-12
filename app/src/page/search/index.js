import { connect } from 'react-redux';
import {
  doSearch,
  doUpdateSearchQuery,
  makeSelectSearchUris,
  selectIsSearching,
  selectSearchValue,
  makeSelectQueryWithOptions,
  selectSearchUrisByQuery,
} from 'lbry-redux';
import { doPushDrawerStack, doSetPlayerVisible } from 'redux/actions/drawer';
import { selectCurrentRoute } from 'redux/selectors/drawer';
import Constants from 'constants';
import SearchPage from './view';

const select = state => ({
  currentRoute: selectCurrentRoute(state),
  isSearching: selectIsSearching(state),
  query: selectSearchValue(state),
  uris: makeSelectSearchUris(makeSelectQueryWithOptions(null, 25)(state))(state),
  urisByQuery: selectSearchUrisByQuery(state),
});

const perform = dispatch => ({
  search: query => dispatch(doSearch(query, 25)),
  updateSearchQuery: query => dispatch(doUpdateSearchQuery(query)),
  pushDrawerStack: () => dispatch(doPushDrawerStack(Constants.DRAWER_ROUTE_SEARCH)),
  setPlayerVisible: () => dispatch(doSetPlayerVisible(false)),
});

export default connect(
  select,
  perform
)(SearchPage);
