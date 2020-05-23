package io.lbry.browser.tasks.wallet;

import android.os.AsyncTask;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import io.lbry.browser.exceptions.LbryioRequestException;
import io.lbry.browser.exceptions.LbryioResponseException;
import io.lbry.browser.utils.Helper;
import io.lbry.browser.utils.Lbryio;

public class SyncSetTask extends AsyncTask<Void, Void, String> {
    private Exception error;
    private String oldHash;
    private String newHash;
    private String data;
    private SyncTaskHandler handler;

    public SyncSetTask(String oldHash, String newHash, String data, SyncTaskHandler handler) {
        this.oldHash = oldHash;
        this.newHash = newHash;
        this.data = data;
        this.handler = handler;
    }

    protected String doInBackground(Void... params) {
        try {
            Map<String, String> options = new HashMap<>();
            options.put("old_hash", oldHash);
            options.put("new_hash", newHash);
            options.put("data", data);
            JSONObject response = (JSONObject) Lbryio.parseResponse(
                    Lbryio.call("sync", "set", options, Helper.METHOD_POST, null));
            String hash = Helper.getJSONString("hash", null, response);
            return hash;
        } catch (LbryioRequestException | LbryioResponseException | ClassCastException ex) {
            error = ex;
            return null;
        }
    }
    protected void onPostExecute(String hash) {
        if (handler != null) {
            if (!Helper.isNullOrEmpty(hash)) {
                handler.onSyncSetSuccess(hash);
            } else {
                handler.onSyncSetError(error);
            }
        }
    }
}
