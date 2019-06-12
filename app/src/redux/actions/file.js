import { ACTIONS, Lbry } from 'lbry-redux';
import { doClaimEligiblePurchaseRewards } from 'lbryinc';
import { Alert, NativeModules } from 'react-native';

export function doStartDownload(uri, outpoint, fileInfo) {
  return (dispatch, getState) => {
    const state = getState();

    if (!outpoint) {
      throw new Error('outpoint is required to begin a download');
    }

    const { downloadingByOutpoint = {} } = state.fileInfo;

    if (downloadingByOutpoint[outpoint]) return;

    dispatch({
      type: ACTIONS.DOWNLOADING_STARTED,
      data: {
        uri,
        outpoint,
        fileInfo,
      },
    });

    dispatch(doClaimEligiblePurchaseRewards());
  };
}

export function doUpdateDownload(uri, outpoint, fileInfo, progress) {
  return dispatch => {
    dispatch({
      type: ACTIONS.DOWNLOADING_PROGRESSED,
      data: {
        uri,
        outpoint,
        fileInfo,
        progress,
      },
    });
  };
}

export function doCompleteDownload(uri, outpoint, fileInfo) {
  return dispatch => {
    if (fileInfo.completed) {
      dispatch({
        type: ACTIONS.DOWNLOADING_COMPLETED,
        data: {
          uri,
          outpoint,
          fileInfo,
        },
      });
    }
  };
}

export function doStopDownloadingFile(uri, fileInfo) {
  return dispatch => {
    let params = { status: 'stop' };
    if (fileInfo.sd_hash) {
      params.sd_hash = fileInfo.sd_hash;
    }
    if (fileInfo.stream_hash) {
      params.stream_hash = fileInfo.stream_hash;
    }

    Lbry.file_set_status(params).then(() => {
      dispatch({
        type: ACTIONS.DOWNLOADING_CANCELED,
        data: { uri, outpoint: fileInfo.outpoint },
      });

      // Should also delete the file after the user stops downloading
      dispatch(doDeleteFile(fileInfo.outpoint, uri));
    });
  };
}

export function doSetPlayingUri(uri) {
  return dispatch => {
    dispatch({
      type: ACTIONS.SET_PLAYING_URI,
      data: { uri },
    });
  };
}

export function doDeleteFile(outpoint, deleteFromComputer, abandonClaim) {
  return (dispatch, getState) => {
    Lbry.file_delete({
      outpoint,
      delete_from_download_dir: deleteFromComputer,
    });

    // If the file is for a claim we published then also abandon the claim
    /*const myClaimsOutpoints = selectMyClaimsOutpoints(state);
    if (abandonClaim && myClaimsOutpoints.indexOf(outpoint) !== -1) {
      const byOutpoint = selectFileInfosByOutpoint(state);
      const fileInfo = byOutpoint[outpoint];

      if (fileInfo) {
        const txid = fileInfo.outpoint.slice(0, -2);
        const nout = Number(fileInfo.outpoint.slice(-1));

        dispatch(doAbandonClaim(txid, nout));
      }
    }*/

    dispatch({
      type: ACTIONS.FILE_DELETE,
      data: {
        outpoint,
      },
    });
  };
}
