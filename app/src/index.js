import React from 'react';
import { Provider, connect } from 'react-redux';
import {
  AppRegistry,
  AppState,
  AsyncStorage,
  Text,
  View,
  NativeModules
} from 'react-native';
import {
  Lbry,
  claimsReducer,
  costInfoReducer,
  fileInfoReducer,
  notificationsReducer,
  searchReducer,
  walletReducer
} from 'lbry-redux';
import { authReducer, rewardsReducer, userReducer } from 'lbryinc';
import { createStore, applyMiddleware, compose, combineReducers } from 'redux';
import { createLogger } from 'redux-logger';
import { StackNavigator, addNavigationHelpers } from 'react-navigation';
import { AppNavigator } from './component/AppNavigator';
import { persistStore, autoRehydrate } from 'redux-persist';
import { reactNavigationMiddleware } from './utils/redux';
import AppWithNavigationState from './component/AppNavigator';
import FilesystemStorage from 'redux-persist-filesystem-storage';
import createCompressor from 'redux-persist-transform-compress';
import createFilter from 'redux-persist-transform-filter';
import moment from 'moment';
import settingsReducer from './redux/reducers/settings';
import thunk from 'redux-thunk';

function isFunction(object) {
  return typeof object === 'function';
}

function isNotFunction(object) {
  return !isFunction(object);
}

function createBulkThunkMiddleware() {
  return ({ dispatch, getState }) => next => action => {
    if (action.type === 'BATCH_ACTIONS') {
      action.actions.filter(isFunction).map(actionFn => actionFn(dispatch, getState));
    }
    return next(action);
  };
}

function enableBatching(reducer) {
  return function batchingReducer(state, action) {
    switch (action.type) {
      case 'BATCH_ACTIONS':
        return action.actions.filter(isNotFunction).reduce(batchingReducer, state);
      default:
        return reducer(state, action);
    }
  };
}

const router = AppNavigator.router;
const navAction = router.getActionForPathAndParams('FirstRun');
const initialNavState = router.getStateForAction(navAction);
const navigatorReducer = (state = initialNavState, action) => {
  const nextState = AppNavigator.router.getStateForAction(action, state);
  return nextState || state;
};

const reducers = combineReducers({
  auth: authReducer,
  claims: claimsReducer,
  costInfo: costInfoReducer,
  fileInfo: fileInfoReducer,
  nav: navigatorReducer,
  notifications: notificationsReducer,
  rewards: rewardsReducer,
  settings: settingsReducer,
  search: searchReducer,
  user: userReducer,
  wallet: walletReducer
});

const bulkThunk = createBulkThunkMiddleware();
const logger = createLogger({ collapsed: true });
const middleware = [thunk, bulkThunk, reactNavigationMiddleware];

// eslint-disable-next-line no-underscore-dangle
const composeEnhancers = compose;

const store = createStore(
  enableBatching(reducers),
  {}, // initial state,
  composeEnhancers(
    autoRehydrate(),
    applyMiddleware(...middleware)
  )
);
window.store = store;

const compressor = createCompressor();
const authFilter = createFilter('auth', ['authToken']);
const saveClaimsFilter = createFilter('claims', ['byId', 'claimsByUri']);
const subscriptionsFilter = createFilter('subscriptions', ['subscriptions']);
const settingsFilter = createFilter('settings', ['clientSettings']);
const walletFilter = createFilter('wallet', ['receiveAddress']);

const persistOptions = {
  whitelist: ['auth', 'claims', 'subscriptions', 'settings', 'wallet'],
  // Order is important. Needs to be compressed last or other transforms can't
  // read the data
  transforms: [authFilter, saveClaimsFilter, subscriptionsFilter, settingsFilter, walletFilter, compressor],
  debounce: 10000,
  storage: FilesystemStorage
};

persistStore(store, persistOptions, err => {
  if (err) {
    console.log('Unable to load saved SETTINGS');
  }
});

// TODO: Find i18n module that is compatible with react-native
global.__ = (str) => str;

class LBRYApp extends React.Component {
  render() {
    return (
      <Provider store={store}>
        <AppWithNavigationState />
      </Provider>
    );
  }
}

AppRegistry.registerComponent('LBRYApp', () => LBRYApp);

export default LBRYApp;
