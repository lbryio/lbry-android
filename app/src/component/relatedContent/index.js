import { connect } from 'react-redux';
import {
  makeSelectClaimForUri,
  doSearch,
  makeSelectRecommendedContentForUri,
  makeSelectTitleForUri,
  selectIsSearching,
} from 'lbry-redux';
import RelatedContent from './view';

const select = (state, props) => ({
  claim: makeSelectClaimForUri(props.uri)(state),
  recommendedContent: makeSelectRecommendedContentForUri(props.uri)(state),
  title: makeSelectTitleForUri(props.uri)(state),
  isSearching: selectIsSearching(state),
});

const perform = dispatch => ({
  search: query => dispatch(doSearch(query, 20, undefined, true)),
});

export default connect(
  select,
  perform
)(RelatedContent);