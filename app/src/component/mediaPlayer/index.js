import { connect } from 'react-redux';
import { SETTINGS, savePosition } from 'lbry-redux';
import { makeSelectClientSetting } from '../../redux/selectors/settings';
import MediaPlayer from './view';

const select = state => ({
  backgroundPlayEnabled: makeSelectClientSetting(SETTINGS.BACKGROUND_PLAY_ENABLED)(state),
});

const perform = dispatch => ({
  savePosition: (claimId, outpoint, position) => dispatch(savePosition(claimId, outpoint, position)),
});

export default connect(select, perform)(MediaPlayer);
