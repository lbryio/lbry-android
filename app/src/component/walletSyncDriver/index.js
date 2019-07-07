import { connect } from 'react-redux';
import { doSetClientSetting } from 'redux/actions/settings';
import { makeSelectClientSetting } from 'redux/selectors/settings';
import { doToast } from 'lbry-redux';
import { selectUserEmail } from 'lbryinc';
import Constants from 'constants';
import WalletSyncDriver from './view';

const select = state => ({
  deviceWalletSynced: makeSelectClientSetting(Constants.SETTING_DEVICE_WALLET_SYNCED)(state),
  userEmail: selectUserEmail(state),
});

const perform = dispatch => ({
  notify: data => dispatch(doToast(data)),
  setClientSetting: (key, value) => dispatch(doSetClientSetting(key, value)),
});

export default connect(
  select,
  perform
)(WalletSyncDriver);
