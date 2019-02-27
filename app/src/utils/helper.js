import { NavigationActions, StackActions } from 'react-navigation';
import { buildURI, isURIValid } from 'lbry-redux';
import { doPopDrawerStack, doPushDrawerStack } from 'redux/actions/drawer';
import { DrawerRoutes } from 'constants';
import Constants from 'constants';

function getRouteForSpecialUri(uri) {
  let targetRoute;
  const page = uri.substring(8).trim(); // 'lbry://?'.length == 8

  switch (page) {
    case Constants.PAGE_REWARDS: targetRoute = 'Rewards'; break;
    case Constants.PAGE_SETTINGS: targetRoute = 'Settings'; break;
    case Constants.PAGE_TRENDING: targetRoute = 'TrendingStack'; break;
    case Constants.PAGE_WALLET: targetRoute = 'WalletStack'; break;
    default: targetRoute = 'DiscoverStack'; break;
  }

  return targetRoute;
}

export function dispatchNavigateToUri(dispatch, nav, uri, isNavigatingBack) {
  if (uri.startsWith('lbry://?')) {
    dispatch(NavigationActions.navigate({ routeName: getRouteForSpecialUri(uri) }));
    return;
  }

  let uriVars = {};
  if (uri.indexOf('?') > -1) {
    uriVarsStr = uri.substring(uri.indexOf('?') + 1);
    uri = uri.substring(0, uri.indexOf('?'));
    uriVars = parseUriVars(uriVarsStr);
  }

  const params = { uri, uriVars };

  if (!isNavigatingBack) {
    dispatch(doPushDrawerStack(uri));
  }

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

export function formatBytes(bytes, decimalPoints = 0) {
  if (!bytes) {
    return '0 KB';
  }

  if (bytes < 1048576) { // < 1MB
    const value = (bytes / 1024.0).toFixed(decimalPoints);
    return `${value} KB`;
  }

  if (bytes < 1073741824) { // < 1GB
    const value = (bytes / (1024.0 * 1024.0)).toFixed(decimalPoints);
    return `${value} MB`;
  }

  const value = (bytes / (1024.0 * 1024.0 * 1024.0)).toFixed(decimalPoints);
  return `${value} GB`;
}

function parseUriVars(vars) {
  const uriVars = {};
  const parts = vars.split('&');
  for (let i = 0; i < parts.length; i++) {
    const str = parts[i];
    if (str.indexOf('=') > -1) {
      const key = str.substring(0, str.indexOf('='));
      const value = str.substring(str.indexOf('=') + 1);
      uriVars[key] = value;
    } else {
      uriVars[str] = null;
    }
  }

  return uriVars;
}

export function navigateToUri(navigation, uri, additionalParams, isNavigatingBack) {
  if (!navigation) {
    return;
  }

  if (uri === navigation.state.key) {
    return;
  }

  if (uri.startsWith('lbry://?')) {
    navigation.navigate({ routeName: getRouteForSpecialUri(uri) });
    return;
  }

  let uriVars = {};
  if (uri.indexOf('?') > -1) {
    uriVarsStr = uri.substring(uri.indexOf('?') + 1);
    uri = uri.substring(0, uri.indexOf('?'));
    uriVars = parseUriVars(uriVarsStr);
  }

  const { store } = window;
  const params = Object.assign({ uri, uriVars }, additionalParams);
  if ('File' === navigation.state.routeName) {
    const stackAction = StackActions.replace({ routeName: 'File', newKey: uri, params });
    navigation.dispatch(stackAction);
    if (store && store.dispatch && !isNavigatingBack) {
      store.dispatch(doPushDrawerStack(uri));
    }
    return;
  }

  navigation.navigate({ routeName: 'File', key: uri, params });
  if (store && store.dispatch && !isNavigatingBack) {
    store.dispatch(doPushDrawerStack(uri));
  }
}

export function navigateBack(navigation, drawerStack, popDrawerStack) {
  if (popDrawerStack) {
    popDrawerStack();
  }

  const target = drawerStack[drawerStack.length > 1 ? drawerStack.length - 2 : 0];
  navigation.goBack(navigation.state.key);
  if (DrawerRoutes.indexOf(target) === -1 && isURIValid(target)) {
    navigateToUri(navigation, target, null, true);
  } else {
    navigation.navigate({ routeName: target  });
  }
}

export function dispatchNavigateBack(dispatch, nav, drawerStack) {
  dispatch(doPopDrawerStack());

  const target = drawerStack[drawerStack.length > 1 ? drawerStack.length - 2 : 0];
  dispatch(NavigationActions.back());

  if (DrawerRoutes.indexOf(target) === -1 && isURIValid(target)) {
    dispatchNavigateToUri(dispatch, nav, target, true);
  } else {
    const navigateAction = NavigationActions.navigate({ routeName: drawerStack[drawerStack.length > 1 ? drawerStack.length - 2 : 0] });
    dispatch(navigateAction);
  }
}

export function uriFromFileInfo(fileInfo) {
  const { name: claimName, claim_name: claimNameDownloaded, claim_id: claimId } = fileInfo;
  const uriParams = {};
  uriParams.contentName = claimName || claimNameDownloaded;
  uriParams.claimId = claimId;
  return buildURI(uriParams);
}
