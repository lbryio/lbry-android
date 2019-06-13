import { connect } from 'react-redux';
import {
  doFileList,
  selectFileInfosDownloaded,
  selectMyClaimsWithoutChannels,
  selectIsFetchingFileList,
} from 'lbry-redux';
import { doPushDrawerStack, doSetPlayerVisible } from 'redux/actions/drawer';
import { selectCurrentRoute } from 'redux/selectors/drawer';
import Constants from 'constants';
import DownloadsPage from './view';

const select = state => ({
  claims: selectMyClaimsWithoutChannels(state),
  currentRoute: selectCurrentRoute(state),
  fileInfos: selectFileInfosDownloaded(state),
  fetching: selectIsFetchingFileList(state),
});

const perform = dispatch => ({
  fileList: () => dispatch(doFileList()),
  pushDrawerStack: () => dispatch(doPushDrawerStack(Constants.DRAWER_ROUTE_MY_LBRY)),
  setPlayerVisible: () => dispatch(doSetPlayerVisible(false)),
});

export default connect(
  select,
  perform
)(DownloadsPage);
