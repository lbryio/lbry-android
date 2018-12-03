import { connect } from 'react-redux';
import { doToast } from 'lbry-redux';
import { doFetchAccessToken, selectAccessToken, selectUserEmail } from 'lbryinc';
import AboutPage from './view';

const select = state => ({
  accessToken: selectAccessToken(state),
  userEmail: selectUserEmail(state),
});

const perform = dispatch => ({
  fetchAccessToken: () => dispatch(doFetchAccessToken()),
  notify: data => dispatch(doToast(data)),
});

export default connect(select, perform)(AboutPage);