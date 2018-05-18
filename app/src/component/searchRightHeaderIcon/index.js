import { connect } from 'react-redux';
import SearchRightHeaderIcon from './view';
import { ACTIONS } from 'lbry-redux';
const perform = dispatch => ({
  clearQuery: () => dispatch({
    type: ACTIONS.HISTORY_NAVIGATE
  })
});

export default connect(null, perform)(SearchRightHeaderIcon);
