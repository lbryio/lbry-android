package io.lbry.browser.tasks.wallet;

import android.os.AsyncTask;
import android.view.View;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import io.lbry.browser.exceptions.ApiCallException;
import io.lbry.browser.utils.Helper;
import io.lbry.browser.utils.Lbry;

public class SyncApplyTask extends AsyncTask<Void, Void, Boolean> {
    // flag to indicate if this sync_apply is to fetch wallet data or apply data
    private boolean fetch;
    private Exception error;
    private String password;
    private String data;
    private View progressView;
    private SyncTaskHandler handler;

    private String syncHash;
    private String syncData;

    public SyncApplyTask(boolean fetch, String password, SyncTaskHandler handler) {
        this.fetch = fetch;
        this.password = password;
        this.handler = handler;
    }

    public SyncApplyTask(String password, String data, View progressView, SyncTaskHandler handler) {
        this.password = password;
        this.data = data;
        this.progressView = progressView;
        this.handler = handler;
    }

    protected void onPreExecute() {
        Helper.setViewVisibility(progressView, View.VISIBLE);
    }

    public Boolean doInBackground(Void... params) {
        Map<String, Object> options = new HashMap<>();
        options.put("password", Helper.isNullOrEmpty(password) ? "" : password);
        if (!fetch) {
            options.put("data", data);
            options.put("blocking", true);
        }

        try {
            JSONObject response = (JSONObject) Lbry.genericApiCall(Lbry.METHOD_SYNC_APPLY, options);
            syncHash = Helper.getJSONString("hash", null, response);
            syncData = Helper.getJSONString("data", null, response);
        } catch (ApiCallException ex) {
            error = ex;
            return false;
        }

        return true;
    }

    protected void onPostExecute(Boolean result) {
        Helper.setViewVisibility(progressView, View.GONE);
        if (handler != null) {
            if (result) {
                handler.onSyncApplySuccess(syncHash, syncData);
            } else {
                handler.onSyncApplyError(error);
            }
        }
    }
}
