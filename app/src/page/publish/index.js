import { connect } from 'react-redux';
import { doToast } from 'lbry-redux';
import { doPushDrawerStack, doSetPlayerVisible } from 'redux/actions/drawer';
import Constants from 'constants';
import PublishPage from './view';

const perform = dispatch => ({
  notify: data => dispatch(doToast(data)),
  pushDrawerStack: () => dispatch(doPushDrawerStack(Constants.DRAWER_ROUTE_PUBLISH)),
  setPlayerVisible: () => dispatch(doSetPlayerVisible(false)),
});

export default connect(
  null,
  perform
)(PublishPage);
