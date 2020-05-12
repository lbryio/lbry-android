package io.lbry.browser.tasks.lbryinc;

import android.os.AsyncTask;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

import io.lbry.browser.exceptions.LbryioRequestException;
import io.lbry.browser.exceptions.LbryioResponseException;
import io.lbry.browser.model.Claim;
import io.lbry.browser.tasks.GenericTaskHandler;
import io.lbry.browser.utils.Lbryio;
import okhttp3.Response;

public class LogFileViewTask extends AsyncTask<Void, Void, Boolean> {
    private String uri;
    private Claim claim;
    private Exception error;
    private GenericTaskHandler handler;
    private long timeToStart;

    public LogFileViewTask(String uri, Claim claim, long timeToStart, GenericTaskHandler handler) {
        this.uri = uri;
        this.claim = claim;
        this.timeToStart = timeToStart;
        this.handler = handler;
    }
    protected Boolean doInBackground(Void... params) {
        try {
            Map<String, String> options = new HashMap<>();
            options.put("uri", uri);
            options.put("claim_id", claim.getClaimId());
            options.put("outpoint", String.format("%s:%d", claim.getTxid(), claim.getNout()));
            if (timeToStart > 0) {
                options.put("time_to_start", String.valueOf(timeToStart));
            }
            Lbryio.call("file", "view", options,  null).close();
        } catch (LbryioRequestException | LbryioResponseException ex) {
            error = ex;
            return false;
        }
        return true;
    }
    protected void onPostExecute(Boolean result) {
        if (handler != null) {
            if (result) {
                handler.onSuccess();
            } else {
                handler.onError(error);
            }
        }
    }
}
