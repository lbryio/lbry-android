import { connect } from 'react-redux';
import { doSetClientSetting } from 'redux/actions/settings';
import { makeSelectClientSetting } from 'redux/selectors/settings';
import { doPushDrawerStack, doSetPlayerVisible } from 'redux/actions/drawer';
import { selectCurrentRoute } from 'redux/selectors/drawer';
import { selectBalance } from 'lbry-redux';
import { doCheckSync, doGetSync, selectUser, selectHasSyncedWallet } from 'lbryinc';
import Constants from 'constants';
import WalletPage from './view';

const select = state => ({
  currentRoute: selectCurrentRoute(state),
  backupDismissed: makeSelectClientSetting(Constants.SETTING_BACKUP_DISMISSED)(state),
  balance: selectBalance(state),
  deviceWalletSynced: makeSelectClientSetting(Constants.SETTING_DEVICE_WALLET_SYNCED)(state),
  hasSyncedWallet: selectHasSyncedWallet(state),
  rewardsNotInterested: makeSelectClientSetting(Constants.SETTING_REWARDS_NOT_INTERESTED)(state),
  understandsRisks: makeSelectClientSetting(Constants.SETTING_ALPHA_UNDERSTANDS_RISKS)(state),
  user: selectUser(state),
});

const perform = dispatch => ({
  checkSync: () => dispatch(doCheckSync()),
  getSync: password => dispatch(doGetSync(password)),
  setClientSetting: (key, value) => dispatch(doSetClientSetting(key, value)),
  pushDrawerStack: () => dispatch(doPushDrawerStack(Constants.DRAWER_ROUTE_WALLET)),
  setPlayerVisible: () => dispatch(doSetPlayerVisible(false)),
});

export default connect(
  select,
  perform
)(WalletPage);
