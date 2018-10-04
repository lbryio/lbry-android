import { connect } from 'react-redux';
import { selectUserEmail } from 'lbryinc';
import AboutPage from './view';

const select = state => ({
  userEmail: selectUserEmail(state),
});

export default connect(select, null)(AboutPage);