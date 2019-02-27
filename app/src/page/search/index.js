import { connect } from 'react-redux';
import {
  doSearch,
  makeSelectSearchUris,
  selectIsSearching,
  selectSearchValue
} from 'lbry-redux';
import { doPushDrawerStack } from 'redux/actions/drawer';
import Constants from 'constants';
import SearchPage from './view';

const select = (state) => ({
  isSearching: selectIsSearching(state),
  query: selectSearchValue(state),
  uris: makeSelectSearchUris(selectSearchValue(state))(state)
});

const perform = dispatch => ({
  search: (query) => dispatch(doSearch(query, 25)),
  pushDrawerStack: () => dispatch(doPushDrawerStack(Constants.DRAWER_ROUTE_SEARCH)),
});

export default connect(select, perform)(SearchPage);
