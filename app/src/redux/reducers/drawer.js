import Constants from 'constants';

const reducers = {};
const defaultState = {
  stack: [ Constants.DRAWER_ROUTE_DISCOVER ] // Discover is always the first drawer route
};

reducers[Constants.ACTION_PUSH_DRAWER_STACK] = (state, action) => {
  const routeName = action.data;
  const newStack = state.stack.slice();

  if (routeName !== newStack[newStack.length - 1]) {
    newStack.push(routeName);
  }

  return {
    ...state,
    stack: newStack
  };
};

reducers[Constants.ACTION_POP_DRAWER_STACK] = (state, action) => {
  // We don't want to pop the Discover route, since it's always expected to be the first
  const newStack = (state.stack.length === 1) ? state.stack.slice() : state.stack.slice(0, state.stack.length - 1);

  return {
    ...state,
    stack: newStack
  }
};

export default function reducer(state = defaultState, action) {
  const handler = reducers[action.type];
  if (handler) return handler(state, action);
  return state;
}