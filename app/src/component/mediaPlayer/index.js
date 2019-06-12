import { connect } from 'react-redux';
import { SETTINGS, savePosition } from 'lbry-redux';
import { makeSelectClientSetting } from 'redux/selectors/settings';
import { doSetPlayerVisible } from 'redux/actions/drawer';
import { selectIsPlayerVisible } from 'redux/selectors/drawer';
import MediaPlayer from './view';

const select = state => ({
  backgroundPlayEnabled: makeSelectClientSetting(SETTINGS.BACKGROUND_PLAY_ENABLED)(state),
  isPlayerVisible: selectIsPlayerVisible(state),
});

const perform = dispatch => ({
  savePosition: (claimId, outpoint, position) => dispatch(savePosition(claimId, outpoint, position)),
  setPlayerVisible: () => dispatch(doSetPlayerVisible(true)),
});

export default connect(
  select,
  perform
)(MediaPlayer);
