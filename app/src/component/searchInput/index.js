import { connect } from 'react-redux';
import { NativeModules } from 'react-native';
import { doSearch, doUpdateSearchQuery }  from 'lbry-redux';
import SearchInput from './view';

const perform = dispatch => ({
  search: search => {
    if (NativeModules.Firebase) {
      NativeModules.Firebase.track('search', { query: search });
    }
    return dispatch(doSearch(search));
  },
  updateSearchQuery: query => dispatch(doUpdateSearchQuery(query, false))
});

export default connect(null, perform)(SearchInput);
