import { connect } from 'react-redux';
import {
  doFetchCostInfoForUri,
  makeSelectFileInfoForUri,
  makeSelectDownloadingForUri,
  makeSelectLoadingForUri,
  makeSelectCostInfoForUri
} from 'lbry-redux';
import { doPurchaseUri, doStartDownload } from '../../redux/actions/file';
import FileDownloadButton from './view';

const select = (state, props) => ({
  fileInfo: makeSelectFileInfoForUri(props.uri)(state),
  downloading: makeSelectDownloadingForUri(props.uri)(state),
  costInfo: makeSelectCostInfoForUri(props.uri)(state),
  loading: makeSelectLoadingForUri(props.uri)(state),
});

const perform = dispatch => ({
  purchaseUri: (uri, failureCallback) => dispatch(doPurchaseUri(uri, null, failureCallback)),
  restartDownload: (uri, outpoint) => dispatch(doStartDownload(uri, outpoint)),
  fetchCostInfo: uri => dispatch(doFetchCostInfoForUri(uri)),
});

export default connect(select, perform)(FileDownloadButton);