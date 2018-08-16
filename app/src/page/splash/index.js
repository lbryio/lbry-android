import { connect } from 'react-redux';
import { doBalanceSubscribe, doNotify } from 'lbry-redux';
import { doAuthenticate, doUserEmailVerify, doUserEmailVerifyFailure, selectUser } from 'lbryinc';
import { doDeleteCompleteBlobs } from '../../redux/actions/file';
import SplashScreen from './view';

const select = state => ({
  user: selectUser(state),
});

const perform = dispatch => ({
    authenticate: (appVersion, deviceId) => dispatch(doAuthenticate(appVersion, deviceId)),
    deleteCompleteBlobs: () => dispatch(doDeleteCompleteBlobs()),
    balanceSubscribe: () => dispatch(doBalanceSubscribe()),
    notify: data => dispatch(doNotify(data)),
    verifyUserEmail: (token, recaptcha) => dispatch(doUserEmailVerify(token, recaptcha)),
    verifyUserEmailFailure: error => dispatch(doUserEmailVerifyFailure(error)),
});

export default connect(select, perform)(SplashScreen);
