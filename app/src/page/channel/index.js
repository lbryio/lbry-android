import { connect } from 'react-redux';
import {
  doFetchClaimsByChannel,
  doFetchClaimCountByChannel,
  makeSelectClaimForUri,
  makeSelectClaimsInChannelForCurrentPageState,
  makeSelectFetchingChannelClaims,
  makeSelectTotalPagesForChannel
} from 'lbry-redux';
import ChannelPage from './view';

const select = (state, props) => ({
  claim: makeSelectClaimForUri(props.uri)(state),
  claimsInChannel: makeSelectClaimsInChannelForCurrentPageState(props.uri)(state),
  fetching: makeSelectFetchingChannelClaims(props.uri)(state),
  totalPages: makeSelectTotalPagesForChannel(props.uri, 10)(state), // Update to use a default PAGE_SIZE constant
});

const perform = dispatch => ({
  fetchClaims: (uri, page) => dispatch(doFetchClaimsByChannel(uri, page)),
  fetchClaimCount: uri => dispatch(doFetchClaimCountByChannel(uri)),
});

export default connect(select, perform)(ChannelPage);
