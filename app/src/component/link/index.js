import { connect } from 'react-redux';
import { doToast } from 'lbry-redux';
import Link from './view';

const perform = dispatch => ({
  notify: (data) => dispatch(doToast(data))
});

export default connect(null, perform)(Link);
