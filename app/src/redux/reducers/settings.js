import { AsyncStorage } from 'react-native';
import { ACTIONS, SETTINGS } from 'lbry-redux';

getAsyncStorageItem = key => {
  return AsyncStorage.getItem(key).then(value => {
    if (['true', 'false'].indexOf(value) > -1) {
      return value === 'true';
    }
    return value;
  });
};

const reducers = {};
const defaultState = {
  clientSettings: {
    backgroundPlayEnabled: getAsyncStorageItem(SETTINGS.BACKGROUND_PLAY_ENABLED),
    keepDaemonRunning: getAsyncStorageItem(SETTINGS.KEEP_DAEMON_RUNNING),
    showNsfw: getAsyncStorageItem(SETTINGS.SHOW_NSFW)
  }
};

reducers[ACTIONS.CLIENT_SETTING_CHANGED] = (state, action) => {
  const { key, value } = action.data;
  const clientSettings = Object.assign({}, state.clientSettings);

  clientSettings[key] = value;
  AsyncStorage.setItem(key, String(value));

  return Object.assign({}, state, {
    clientSettings,
  });
};

export default function reducer(state = defaultState, action) {
  const handler = reducers[action.type];
  if (handler) return handler(state, action);
  return state;
}
