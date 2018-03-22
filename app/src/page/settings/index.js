import { connect } from 'react-redux';
import { SETTINGS } from 'lbry-redux';
import { doSetClientSetting } from '../../redux/actions/settings';
import { makeSelectClientSetting } from '../../redux/selectors/settings';
import SettingsPage from './view';

const select = state => ({
  keepDaemonRunning: makeSelectClientSetting(SETTINGS.KEEP_DAEMON_RUNNING)(state),
  showNsfw: makeSelectClientSetting(SETTINGS.SHOW_NSFW)(state),
});

const perform = dispatch => ({
  setClientSetting: (key, value) => dispatch(doSetClientSetting(key, value)),
});

export default connect(select, perform)(SettingsPage);
