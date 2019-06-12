import { connect } from 'react-redux';
import { doToast } from 'lbry-redux';
import { doFetchAccessToken, selectAccessToken, selectUserEmail } from 'lbryinc';
import { doPushDrawerStack, doPopDrawerStack, doSetPlayerVisible } from 'redux/actions/drawer';
import { selectCurrentRoute, selectDrawerStack } from 'redux/selectors/drawer';
import AboutPage from './view';
import Constants from 'constants';

const select = state => ({
  accessToken: selectAccessToken(state),
  currentRoute: selectCurrentRoute(state),
  drawerStack: selectDrawerStack(state),
  userEmail: selectUserEmail(state),
});

const perform = dispatch => ({
  fetchAccessToken: () => dispatch(doFetchAccessToken()),
  notify: data => dispatch(doToast(data)),
  pushDrawerStack: () => dispatch(doPushDrawerStack(Constants.DRAWER_ROUTE_ABOUT)),
  popDrawerStack: () => dispatch(doPopDrawerStack()),
  setPlayerVisible: () => dispatch(doSetPlayerVisible(false)),
});

export default connect(
  select,
  perform
)(AboutPage);
