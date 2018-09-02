import { NavigationActions, StackActions } from 'react-navigation';

export function navigateToUri(navigation, uri, additionalParams) {
    if (!navigation) {
        return;
    }

    if (uri === navigation.state.key) {
        return;
    }

    const params = Object.assign({ uri }, additionalParams);
    if ('File' === navigation.state.routeName) {
        const stackAction = StackActions.replace({ routeName: 'File', newKey: uri, params });
        navigation.dispatch(stackAction);
        return;
    }

    navigation.navigate({ routeName: 'File', key: uri, params });
}

export function dispatchNavigateToUri(dispatch, nav, uri) {
    const params = { uri };
    if (nav && nav.routes && nav.routes.length > 0 && 'Main' === nav.routes[0].routeName) {
        const mainRoute = nav.routes[0];
        const discoverRoute = mainRoute.routes[0];
        if (discoverRoute.index > 0 && 'File' === discoverRoute.routes[discoverRoute.index].routeName) {
            const fileRoute = discoverRoute.routes[discoverRoute.index];
            // Currently on a file page, so we can ignore (if the URI is the same) or replace (different URIs)
            if (uri !== fileRoute.params.uri) {
                const stackAction = StackActions.replace({ routeName: 'File', newKey: uri, params });
                dispatch(stackAction);
                return;
            }
        }
    }

    const navigateAction = NavigationActions.navigate({ routeName: 'File', key: uri, params });
    dispatch(navigateAction);
}