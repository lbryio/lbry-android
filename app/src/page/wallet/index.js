import { connect } from 'react-redux';
import { doSetClientSetting } from '../../redux/actions/settings';
import { makeSelectClientSetting } from '../../redux/selectors/settings';
import WalletPage from './view';

const select = state => ({
  understandsRisks: makeSelectClientSetting("alphaUnderstandsRisks")(state),
});

const perform = dispatch => ({
  setClientSetting: (key, value) => dispatch(doSetClientSetting(key, value)),
});

export default connect(select, perform)(WalletPage);
