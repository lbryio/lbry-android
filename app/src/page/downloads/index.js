import { connect } from 'react-redux';
import {
  doFileList,
  selectFileInfosDownloaded,
  selectMyClaimsWithoutChannels,
  selectIsFetchingFileList,
} from 'lbry-redux';
import { doPushDrawerStack } from 'redux/actions/drawer';
import Constants from 'constants';
import DownloadsPage from './view';

const select = (state) => ({
  fileInfos: selectFileInfosDownloaded(state),
  fetching: selectIsFetchingFileList(state),
  claims: selectMyClaimsWithoutChannels(state),
});

const perform = dispatch => ({
  fileList: () => dispatch(doFileList()),
  pushDrawerStack: () => dispatch(doPushDrawerStack(Constants.DRAWER_ROUTE_MY_LBRY))
});

export default connect(select, perform)(DownloadsPage);
