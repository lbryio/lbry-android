import { connect } from 'react-redux';
import { selectUnfollowedTags } from 'lbry-redux';
import TagSearch from './view';

const select = state => ({
  unfollowedTags: selectUnfollowedTags(state),
});

export default connect(
  select,
  null
)(TagSearch);
