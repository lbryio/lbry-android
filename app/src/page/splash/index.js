import { connect } from 'react-redux';
import { doBalanceSubscribe } from 'lbry-redux';
import { doDeleteCompleteBlobs } from '../../redux/actions/file';
import SplashScreen from './view';

const perform = dispatch => ({
    balanceSubscribe: () => dispatch(doBalanceSubscribe()),
    deleteCompleteBlobs: () => dispatch(doDeleteCompleteBlobs())
});

export default connect(null, perform)(SplashScreen);