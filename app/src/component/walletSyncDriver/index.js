import { connect } from 'react-redux';
import { makeSelectClientSetting } from 'redux/selectors/settings';
import Constants from 'constants';
import WalletSyncDriver from './view';

const select = state => ({
  deviceWalletSynced: makeSelectClientSetting(Constants.SETTING_DEVICE_WALLET_SYNCED)(state),
});

export default connect(select, null)(WalletSyncDriver);
