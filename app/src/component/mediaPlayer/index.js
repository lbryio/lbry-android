import { connect } from 'react-redux';
import { SETTINGS } from 'lbry-redux';
import { makeSelectClientSetting } from '../../redux/selectors/settings';
import MediaPlayer from './view';

const select = state => ({
    backgroundPlayEnabled: makeSelectClientSetting(SETTINGS.BACKGROUND_PLAY_ENABLED)(state),
});

export default connect(select, null)(MediaPlayer);
