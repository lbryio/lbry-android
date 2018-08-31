import { connect } from 'react-redux';
import {
  doFileList,
  selectFileInfosDownloaded,
  selectMyClaimsWithoutChannels,
  selectIsFetchingFileList,
} from 'lbry-redux';
import DownloadsPage from './view';

const select = (state) => ({
  fileInfos: selectFileInfosDownloaded(state),
  fetching: selectIsFetchingFileList(state),
  claims: selectMyClaimsWithoutChannels(state),
});

const perform = dispatch => ({
  fileList: () => dispatch(doFileList()),
});

export default connect(select, perform)(DownloadsPage);
