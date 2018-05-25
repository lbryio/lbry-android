import {
    ACTIONS,
    Lbry,
    doNotify,
    formatCredits,
    selectBalance,
    makeSelectCostInfoForUri,
    makeSelectFileInfoForUri,
    makeSelectMetadataForUri,
    selectDownloadingByOutpoint,
} from 'lbry-redux';
import { Alert, NativeModules } from 'react-native';

const DOWNLOAD_POLL_INTERVAL = 250;

export function doUpdateLoadStatus(uri, outpoint) {
  return (dispatch, getState) => {
    Lbry.file_list({
      outpoint,
      full_status: true,
    }).then(([fileInfo]) => {
      if (!fileInfo || fileInfo.written_bytes === 0) {
        // download hasn't started yet
        setTimeout(() => {
          dispatch(doUpdateLoadStatus(uri, outpoint));
        }, DOWNLOAD_POLL_INTERVAL);
      } else if (fileInfo.completed) {
        // TODO this isn't going to get called if they reload the client before
        // the download finished
        const { total_bytes: totalBytes, written_bytes: writtenBytes } = fileInfo;
        dispatch({
          type: ACTIONS.DOWNLOADING_COMPLETED,
          data: {
            uri,
            outpoint,
            fileInfo,
          },
        });

        if (NativeModules.LbryDownloadManager) {
          NativeModules.LbryDownloadManager.updateDownload(uri, fileInfo.file_name, 100, writtenBytes, totalBytes);
        }
        
        /*const notif = new window.Notification('LBRY Download Complete', {
          body: fileInfo.metadata.stream.metadata.title,
          silent: false,
        });
        notif.onclick = () => {
          ipcRenderer.send('focusWindow', 'main');
        };*/
      } else {
        // ready to play
        const { total_bytes: totalBytes, written_bytes: writtenBytes } = fileInfo;
        const progress = writtenBytes / totalBytes * 100;

        dispatch({
          type: ACTIONS.DOWNLOADING_PROGRESSED,
          data: {
            uri,
            outpoint,
            fileInfo,
            progress,
          },
        });

        if (NativeModules.LbryDownloadManager) {
          NativeModules.LbryDownloadManager.updateDownload(uri, fileInfo.file_name, progress, writtenBytes, totalBytes);
        }
        
        setTimeout(() => {
          dispatch(doUpdateLoadStatus(uri, outpoint));
        }, DOWNLOAD_POLL_INTERVAL);
      }
    });
  };
}

export function doStartDownload(uri, outpoint) {
  return (dispatch, getState) => {
    const state = getState();

    if (!outpoint) {
      throw new Error('outpoint is required to begin a download');
    }

    const { downloadingByOutpoint = {} } = state.fileInfo;

    if (downloadingByOutpoint[outpoint]) return;

    Lbry.file_list({ outpoint, full_status: true }).then(([fileInfo]) => {
      dispatch({
        type: ACTIONS.DOWNLOADING_STARTED,
        data: {
          uri,
          outpoint,
          fileInfo,
        },
      });
      
      if (NativeModules.LbryDownloadManager) {
        NativeModules.LbryDownloadManager.startDownload(uri, fileInfo.file_name);
      }

      dispatch(doUpdateLoadStatus(uri, outpoint));
    });
  };
}

export function doStopDownloadingFile(uri, fileInfo) {
  return dispatch  => {
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
        data: {}
      });
    });
    
    if (NativeModules.LbryDownloadManager) {
      NativeModules.LbryDownloadManager.stopDownload(uri, fileInfo.file_name);
    }
    
    // Should also delete the file after the user stops downloading
    dispatch(doDeleteFile(fileInfo.outpoint, uri));
  };
}

export function doDownloadFile(uri, streamInfo) {
  return dispatch => {
    dispatch(doStartDownload(uri, streamInfo.outpoint));

    //analytics.apiLog(uri, streamInfo.output, streamInfo.claim_id);

    //dispatch(doClaimEligiblePurchaseRewards());
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

export function doLoadVideo(uri) {
  return dispatch => {
    dispatch({
      type: ACTIONS.LOADING_VIDEO_STARTED,
      data: {
        uri,
      },
    });
    
    Lbry.get({ uri })
      .then(streamInfo => {
        const timeout =
          streamInfo === null || typeof streamInfo !== 'object' || streamInfo.error === 'Timeout';

        if (timeout) {
          dispatch(doSetPlayingUri(null));
          dispatch({
            type: ACTIONS.LOADING_VIDEO_FAILED,
            data: { uri },
          });

          dispatch(doNotify({
            message: `File timeout for uri ${uri}`,
            displayType: ['toast']
          }));
        } else {
          dispatch(doDownloadFile(uri, streamInfo));
        }
      })
      .catch(() => {
        dispatch(doSetPlayingUri(null));
        dispatch({
          type: ACTIONS.LOADING_VIDEO_FAILED,
          data: { uri },
        });
        
        dispatch(doNotify({
          message: `Failed to download ${uri}, please try again. If this problem persists, visit https://lbry.io/faq/support for support.`,
          displayType: ['toast']
        }));
      });
  };
}

export function doPurchaseUri(uri, specificCostInfo) {
  return (dispatch, getState) => {
    const state = getState();
    const balance = selectBalance(state);
    const fileInfo = makeSelectFileInfoForUri(uri)(state);
    const metadata = makeSelectMetadataForUri(uri)(state);
    const title = metadata ? metadata.title : uri;
    const downloadingByOutpoint = selectDownloadingByOutpoint(state);
    const alreadyDownloading = fileInfo && !!downloadingByOutpoint[fileInfo.outpoint];

    function attemptPlay(cost, instantPurchaseMax = null) {
      if (cost > 0 && (!instantPurchaseMax || cost > instantPurchaseMax)) {
        // display alert
        const formattedCost = formatCredits(cost, 2);
        const unit = cost === 1 ? 'credit' : 'credits';
        Alert.alert('Confirm purchase',
          `This will purchase "${title}" for ${formattedCost} ${unit}`,
          [
            { text: 'OK', onPress: () => dispatch(doLoadVideo(uri)) },
            { text: 'Cancel', style: 'cancel' }
          ],
          { cancelable: true });
      } else {
        dispatch(doLoadVideo(uri));
      }
    }

    // we already fully downloaded the file.
    if (fileInfo && fileInfo.completed) {
      // If written_bytes is false that means the user has deleted/moved the
      // file manually on their file system, so we need to dispatch a
      // doLoadVideo action to reconstruct the file from the blobs
      if (!fileInfo.written_bytes) dispatch(doLoadVideo(uri));

      Promise.resolve();
      return;
    }

    // we are already downloading the file
    if (alreadyDownloading) {
      Promise.resolve();
      return;
    }

    const costInfo = makeSelectCostInfoForUri(uri)(state) || specificCostInfo;
    const { cost } = costInfo;

    if (cost > balance) {
      dispatch(doSetPlayingUri(null));
      dispatch(doNotify({
        message: 'Insufficient credits',
        displayType: ['toast']
      }));
      Promise.resolve();
      return;
    }

    attemptPlay(cost);
    /*if (cost === 0 || !makeSelectClientSetting(SETTINGS.INSTANT_PURCHASE_ENABLED)(state)) {
      attemptPlay(cost);
    } else {
      const instantPurchaseMax = makeSelectClientSetting(SETTINGS.INSTANT_PURCHASE_MAX)(state);
      if (instantPurchaseMax.currency === 'LBC') {
        attemptPlay(cost, instantPurchaseMax.amount);
      } else {
        // Need to convert currency of instant purchase maximum before trying to play
        Lbryio.getExchangeRates().then(({ LBC_USD }) => {
          attemptPlay(cost, instantPurchaseMax.amount / LBC_USD);
        });
      }
    }*/
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

    //const totalProgress = selectTotalDownloadProgress(getState());
    //setProgressBar(totalProgress);
  };
}
