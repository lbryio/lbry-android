import Constants from 'constants';

export const doPushDrawerStack = (routeName) => (dispatch) => dispatch({
    type: Constants.ACTION_PUSH_DRAWER_STACK,
    data: routeName
});

export const doPopDrawerStack = () => (dispatch) => dispatch({
    type: Constants.ACTION_POP_DRAWER_STACK
});
