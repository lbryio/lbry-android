import { connect } from 'react-redux';
import {
  doFetchClaimsByChannel,
  makeSelectClaimForUri,
  makeSelectClaimsInChannelForCurrentPageState,
  makeSelectFetchingChannelClaims,
  makeSelectTotalPagesForChannel
} from 'lbry-redux';
import { doPopDrawerStack } from 'redux/actions/drawer';
import { selectDrawerStack } from 'redux/selectors/drawer';
import ChannelPage from './view';

const select = (state, props) => ({
  claim: makeSelectClaimForUri(props.uri)(state),
  claimsInChannel: makeSelectClaimsInChannelForCurrentPageState(props.uri)(state),
  drawerStack: selectDrawerStack(state),
  fetching: makeSelectFetchingChannelClaims(props.uri)(state),
  totalPages: makeSelectTotalPagesForChannel(props.uri, 10)(state), // Update to use a default PAGE_SIZE constant
});

const perform = dispatch => ({
  fetchClaims: (uri, page) => dispatch(doFetchClaimsByChannel(uri, page)),
  popDrawerStack: () => dispatch(doPopDrawerStack())
});

export default connect(select, perform)(ChannelPage);
