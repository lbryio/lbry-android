import { connect } from 'react-redux';
import { selectSuggestedChannels, selectIsFetchingSuggested } from 'lbryinc';
import SuggestedSubscriptions from './view';

const select = state => ({
  suggested: selectSuggestedChannels(state),
  loading: selectIsFetchingSuggested(state),
});

export default connect(
  select,
  null
)(SuggestedSubscriptions);