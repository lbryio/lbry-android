package io.lbry.browser.tasks.claim;

import android.os.AsyncTask;
import android.view.View;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.lbry.browser.exceptions.ApiCallException;
import io.lbry.browser.tasks.GenericTaskHandler;
import io.lbry.browser.utils.Helper;
import io.lbry.browser.utils.Lbry;

public class AbandonChannelTask extends AsyncTask<Void, Void, Boolean> {
    private List<String> claimIds;
    private List<String> successfulClaimIds;
    private List<String> failedClaimIds;
    private List<Exception> failedExceptions;
    private View progressView;
    private AbandonHandler handler;

    public AbandonChannelTask(List<String> claimIds, View progressView, AbandonHandler handler) {
        this.claimIds = claimIds;
        this.progressView = progressView;
        this.handler = handler;
    }

    protected void onPreExecute() {
        Helper.setViewVisibility(progressView, View.VISIBLE);
    }

    public Boolean doInBackground(Void... params) {
        successfulClaimIds = new ArrayList<>();
        failedClaimIds = new ArrayList<>();
        failedExceptions = new ArrayList<>();

        for (String claimId : claimIds) {
            try {
                Map<String, Object> options = new HashMap<>();
                options.put("claim_id", claimId);
                options.put("blocking", true);
                JSONObject result = (JSONObject) Lbry.genericApiCall(Lbry.METHOD_CHANNEL_ABANDON, options);
                successfulClaimIds.add(claimId);
            } catch (ApiCallException ex) {
                failedClaimIds.add(claimId);
                failedExceptions.add(ex);
            }
        }

        return true;
    }

    protected void onPostExecute(Boolean result) {
        Helper.setViewVisibility(progressView, View.GONE);
        if (handler != null) {
            handler.onComplete(successfulClaimIds, failedClaimIds, failedExceptions);
        }
    }
}
