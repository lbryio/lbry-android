import { connect } from 'react-redux';
import { SETTINGS } from 'lbry-redux';
import { doPushDrawerStack, doPopDrawerStack, doSetPlayerVisible } from 'redux/actions/drawer';
import { doSetClientSetting } from 'redux/actions/settings';
import { selectCurrentRoute, selectDrawerStack } from 'redux/selectors/drawer';
import { makeSelectClientSetting } from 'redux/selectors/settings';
import Constants from 'constants';
import SettingsPage from './view';

const select = state => ({
  backgroundPlayEnabled: makeSelectClientSetting(SETTINGS.BACKGROUND_PLAY_ENABLED)(state),
  currentRoute: selectCurrentRoute(state),
  drawerStack: selectDrawerStack(state),
  keepDaemonRunning: makeSelectClientSetting(SETTINGS.KEEP_DAEMON_RUNNING)(state),
  showNsfw: makeSelectClientSetting(SETTINGS.SHOW_NSFW)(state),
});

const perform = dispatch => ({
  pushDrawerStack: () => dispatch(doPushDrawerStack(Constants.DRAWER_ROUTE_SETTINGS)),
  popDrawerStack: () => dispatch(doPopDrawerStack()),
  setClientSetting: (key, value) => dispatch(doSetClientSetting(key, value)),
  setPlayerVisible: () => dispatch(doSetPlayerVisible(false)),
});

export default connect(
  select,
  perform
)(SettingsPage);
