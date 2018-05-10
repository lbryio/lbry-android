import { connect } from 'react-redux';
import { doSetClientSetting } from '../../redux/actions/settings';
import { makeSelectClientSetting } from '../../redux/selectors/settings';
import Constants from '../../constants';
import WalletPage from './view';

const select = state => ({
  understandsRisks: makeSelectClientSetting(Constants.SETTING_ALPHA_UNDERSTANDS_RISKS)(state),
});

const perform = dispatch => ({
  setClientSetting: (key, value) => dispatch(doSetClientSetting(key, value)),
});

export default connect(select, perform)(WalletPage);
