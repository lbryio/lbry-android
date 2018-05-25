import { connect } from 'react-redux';
import {
  doFetchClaimsByChannel,
  doFetchClaimCountByChannel,
  makeSelectClaimForUri,
  makeSelectClaimsInChannelForPage,
  makeSelectFetchingChannelClaims,
} from 'lbry-redux';
import ChannelPage from './view';

const select = (state, props) => ({
  claim: makeSelectClaimForUri(props.uri)(state),
  claimsInChannel: makeSelectClaimsInChannelForPage(props.uri, props.page || 1)(state),
  fetching: makeSelectFetchingChannelClaims(props.uri)(state),
});

const perform = dispatch => ({
  fetchClaims: (uri, page) => dispatch(doFetchClaimsByChannel(uri, page)),
  fetchClaimCount: uri => dispatch(doFetchClaimCountByChannel(uri)),
});

export default connect(select, perform)(ChannelPage);
