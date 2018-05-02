import { connect } from 'react-redux';
import { doBalanceSubscribe } from 'lbry-redux';
import SplashScreen from './view';

const perform = dispatch => ({
    balanceSubscribe: () => dispatch(doBalanceSubscribe())
});

export default connect(null, perform)(SplashScreen);