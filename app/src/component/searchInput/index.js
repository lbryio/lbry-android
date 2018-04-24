import { connect } from 'react-redux';
import { NativeModules } from 'react-native';
import { doSearch, doUpdateSearchQuery }  from 'lbry-redux';
import SearchInput from './view';

const perform = dispatch => ({
  search: search => {
    if (NativeModules.Mixpanel) {
      NativeModules.Mixpanel.track('Search', { Query: search });
    }
    return dispatch(doSearch(search));
  },
  updateSearchQuery: query => dispatch(doUpdateSearchQuery(query, false))
});

export default connect(null, perform)(SearchInput);
