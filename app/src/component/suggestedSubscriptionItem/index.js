import { connect } from 'react-redux';
import {
  makeSelectFetchingChannelClaims,
  makeSelectClaimsInChannelForPage,
  doFetchClaimsByChannel,
  doResolveUris,
} from 'lbry-redux';
import { selectShowNsfw } from 'redux/selectors/settings';
import SuggestedSubscriptionItem from './view';

const select = (state, props) => ({
  claims: makeSelectClaimsInChannelForPage(props.categoryLink)(state),
  fetching: makeSelectFetchingChannelClaims(props.categoryLink)(state),
  obscureNsfw: !selectShowNsfw(state),
});

const perform = dispatch => ({
  fetchChannel: channel => dispatch(doFetchClaimsByChannel(channel)),
  resolveUris: uris => dispatch(doResolveUris(uris, true)),
});

export default connect(
  select,
  perform
)(SuggestedSubscriptionItem);
