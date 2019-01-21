import { connect } from 'react-redux';
import { doToast } from 'lbry-redux';
import { doFetchAccessToken, selectAccessToken, selectUserEmail } from 'lbryinc';
import { doPushDrawerStack, doPopDrawerStack } from 'redux/actions/drawer';
import { selectDrawerStack } from 'redux/selectors/drawer';
import AboutPage from './view';
import Constants from 'constants';

const select = state => ({
  accessToken: selectAccessToken(state),
  userEmail: selectUserEmail(state),
  drawerStack: selectDrawerStack(state),
});

const perform = dispatch => ({
  fetchAccessToken: () => dispatch(doFetchAccessToken()),
  notify: data => dispatch(doToast(data)),
  pushDrawerStack: () => dispatch(doPushDrawerStack(Constants.DRAWER_ROUTE_ABOUT)),
  popDrawerStack: () => dispatch(doPopDrawerStack()),
});

export default connect(select, perform)(AboutPage);