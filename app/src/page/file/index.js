import { connect } from 'react-redux';
import {
  doFetchFileInfo,
  doFetchCostInfoForUri,
  doResolveUri,
  doToast,
  makeSelectIsUriResolving,
  makeSelectCostInfoForUri,
  makeSelectFileInfoForUri,
  makeSelectClaimForUri,
  makeSelectContentTypeForUri,
  makeSelectMetadataForUri,
  selectBlackListedOutpoints,
} from 'lbry-redux';
import { selectRewardContentClaimIds } from 'lbryinc';
import { doDeleteFile, doStopDownloadingFile } from '../../redux/actions/file';
import FilePage from './view';

const select = (state, props) => {
  const selectProps = { uri: props.navigation.state.params.uri };
  return {
    blackListedOutpoints: selectBlackListedOutpoints(state),
    claim: makeSelectClaimForUri(selectProps.uri)(state),
    isResolvingUri: makeSelectIsUriResolving(selectProps.uri)(state),
    contentType: makeSelectContentTypeForUri(selectProps.uri)(state),
    costInfo: makeSelectCostInfoForUri(selectProps.uri)(state),
    metadata: makeSelectMetadataForUri(selectProps.uri)(state),
    //obscureNsfw: !selectShowNsfw(state),
    //tab: makeSelectCurrentParam('tab')(state),
    fileInfo: makeSelectFileInfoForUri(selectProps.uri)(state),
    rewardedContentClaimIds: selectRewardContentClaimIds(state, selectProps),
  };
};

const perform = dispatch => ({
  deleteFile: (fileInfo, deleteFromDevice, abandonClaim) => {
    dispatch(doDeleteFile(fileInfo, deleteFromDevice, abandonClaim));
  },
  fetchFileInfo: uri => dispatch(doFetchFileInfo(uri)),
  fetchCostInfo: uri => dispatch(doFetchCostInfoForUri(uri)),
  notify: data => dispatch(doToast(data)),
  resolveUri: uri => dispatch(doResolveUri(uri)),
  stopDownload: (uri, fileInfo) => dispatch(doStopDownloadingFile(uri, fileInfo)),
});

export default connect(select, perform)(FilePage);
