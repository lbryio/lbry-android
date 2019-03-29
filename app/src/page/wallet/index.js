import { connect } from 'react-redux';
import { doSetClientSetting } from 'redux/actions/settings';
import { makeSelectClientSetting } from 'redux/selectors/settings';
import { doPushDrawerStack } from 'redux/actions/drawer';
import { doGetSync, selectUser } from 'lbryinc';
import Constants from 'constants';
import WalletPage from './view';

const select = state => ({
  user: selectUser(state),
  understandsRisks: makeSelectClientSetting(Constants.SETTING_ALPHA_UNDERSTANDS_RISKS)(state),
});

const perform = dispatch => ({
  getSync: password => dispatch(doGetSync(password)),
  setClientSetting: (key, value) => dispatch(doSetClientSetting(key, value)),
  pushDrawerStack: () => dispatch(doPushDrawerStack(Constants.DRAWER_ROUTE_WALLET))
});

export default connect(select, perform)(WalletPage);
