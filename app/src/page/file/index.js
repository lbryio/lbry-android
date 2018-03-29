import { connect } from 'react-redux';
import {
  doFetchFileInfo,
  makeSelectFileInfoForUri,
  doFetchCostInfoForUri,
  makeSelectClaimForUri,
  makeSelectContentTypeForUri,
  makeSelectMetadataForUri,
  selectRewardContentClaimIds,
  makeSelectCostInfoForUri
} from 'lbry-redux';
import { doDeleteFile } from '../../redux/actions/file';
import FilePage from './view';

const select = (state, props) => {
  const selectProps = { uri: props.navigation.state.params.uri };
  return {
    claim: makeSelectClaimForUri(selectProps.uri)(state),
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
  fetchFileInfo: uri => dispatch(doFetchFileInfo(uri)),
  fetchCostInfo: uri => dispatch(doFetchCostInfoForUri(uri)),
  deleteFile: (fileInfo, deleteFromDevice, abandonClaim) => {
    dispatch(doDeleteFile(fileInfo, deleteFromDevice, abandonClaim));
  },
});

export default connect(select, perform)(FilePage);
