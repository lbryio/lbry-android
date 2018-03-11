import React from 'react';
import { Provider, connect } from 'react-redux';
import DiscoverPage from './page/discover';
import { AppRegistry, StyleSheet, Text, View } from 'react-native';
import { createStore, applyMiddleware, compose, combineReducers } from 'redux';
import {
  StackNavigator, addNavigationHelpers
} from 'react-navigation';
import { AppNavigator } from './component/AppNavigator';
import AppWithNavigationState from './component/AppNavigator';
import { persistStore, autoRehydrate } from 'redux-persist';
import thunk from 'redux-thunk';
import {
  Lbry,
  claimsReducer,
  costInfoReducer,
  fileInfoReducer,
  searchReducer,
  walletReducer
} from 'lbry-redux';
import { reactNavigationMiddleware } from './utils/redux';

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
const navAction = router.getActionForPathAndParams('Splash');
const initialNavState = router.getStateForAction(navAction);
const navigatorReducer = (state = initialNavState, action) => {
  const nextState = AppNavigator.router.getStateForAction(action, state);
  return nextState || state;
};

const reducers = combineReducers({
  claims: claimsReducer,
  costInfo: costInfoReducer,
  fileInfo: fileInfoReducer,
  search: searchReducer,
  wallet: walletReducer,
  nav: navigatorReducer
});

const bulkThunk = createBulkThunkMiddleware();
const middleware = [thunk, bulkThunk, reactNavigationMiddleware];

// eslint-disable-next-line no-underscore-dangle
const composeEnhancers = compose;

const store = createStore(
  enableBatching(reducers),
  {}, // initial state,
  composeEnhancers(
    /*autoRehydrate({
      log: app.env === 'development',
    }),*/
    applyMiddleware(...middleware)
  )
);


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
