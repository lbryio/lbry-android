import { connect } from 'react-redux';
import {
  doResolveUri,
  makeSelectClaimForUri,
  makeSelectMetadataForUri,
  makeSelectFileInfoForUri,
  makeSelectThumbnailForUri,
  makeSelectTitleForUri,
  makeSelectIsUriResolving,
  makeSelectClaimIsNsfw
} from 'lbry-redux';
import { selectRewardContentClaimIds } from 'lbryinc';
import { selectShowNsfw } from 'redux/selectors/settings';
import FileItem from './view';

const select = (state, props) => ({
  claim: makeSelectClaimForUri(props.uri)(state),
  fileInfo: makeSelectFileInfoForUri(props.uri)(state),
  metadata: makeSelectMetadataForUri(props.uri)(state),
  rewardedContentClaimIds: selectRewardContentClaimIds(state, props),
  isResolvingUri: makeSelectIsUriResolving(props.uri)(state),
  obscureNsfw: !selectShowNsfw(state),
  thumbnail: makeSelectThumbnailForUri(props.uri)(state),
  title: makeSelectTitleForUri(props.uri)(state),
  nsfw: makeSelectClaimIsNsfw(props.uri)(state),
});

const perform = dispatch => ({
  resolveUri: uri => dispatch(doResolveUri(uri)),
});

export default connect(select, perform)(FileItem);