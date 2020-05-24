package io.lbry.browser.tasks.claim;

import android.os.AsyncTask;
import android.view.View;

import java.util.List;
import java.util.Map;

import io.lbry.browser.exceptions.ApiCallException;
import io.lbry.browser.model.Claim;
import io.lbry.browser.utils.Helper;
import io.lbry.browser.utils.Lbry;

public class ClaimSearchTask extends AsyncTask<Void, Void, List<Claim>> {
    private Map<String, Object> options;
    private String connectionString;
    private ClaimSearchResultHandler handler;
    private View progressView;
    private ApiCallException error;

    public ClaimSearchTask(Map<String, Object> options, String connectionString, View progressView, ClaimSearchResultHandler handler) {
        this.options = options;
        this.connectionString = connectionString;
        this.progressView = progressView;
        this.handler = handler;
    }
    protected void onPreExecute() {
        Helper.setViewVisibility(progressView, View.VISIBLE);
    }
    protected List<Claim> doInBackground(Void... params) {
        try {
            return Lbry.claimSearch(options, connectionString);
        } catch (ApiCallException ex) {
            error = ex;
            return null;
        }
    }
    protected void onPostExecute(List<Claim> claims) {
        Helper.setViewVisibility(progressView, View.GONE);
        if (handler != null) {
            if (claims != null) {
                handler.onSuccess(Helper.filterInvalidReposts(claims), claims.size() < Helper.parseInt(options.get("page_size"), 0));
            } else {
                handler.onError(error);
            }
        }
    }

}
