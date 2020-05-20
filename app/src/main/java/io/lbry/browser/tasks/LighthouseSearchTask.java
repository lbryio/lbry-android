package io.lbry.browser.tasks;

import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;

import java.util.List;

import io.lbry.browser.exceptions.LbryRequestException;
import io.lbry.browser.exceptions.LbryResponseException;
import io.lbry.browser.model.Claim;
import io.lbry.browser.tasks.claim.ClaimSearchResultHandler;
import io.lbry.browser.utils.Helper;
import io.lbry.browser.utils.Lighthouse;

public class LighthouseSearchTask extends AsyncTask<Void, Void, List<Claim>> {
    private String rawQuery;
    private int size;
    private int from;
    private boolean nsfw;
    private String relatedTo;
    private ClaimSearchResultHandler handler;
    private ProgressBar progressBar;
    private Exception error;

    public LighthouseSearchTask(String rawQuery, int size, int from, boolean nsfw, String relatedTo, ProgressBar progressBar, ClaimSearchResultHandler handler) {
        this.rawQuery = rawQuery;
        this.size = size;
        this.from = from;
        this.nsfw = nsfw;
        this.relatedTo = relatedTo;
        this.progressBar = progressBar;
        this.handler = handler;
    }
    protected void onPreExecute() {
        Helper.setViewVisibility(progressBar, View.VISIBLE);
    }
    protected List<Claim> doInBackground(Void... params) {
        try {
            return Lighthouse.search(rawQuery, size, from, nsfw, relatedTo);
        } catch (LbryRequestException | LbryResponseException ex) {
            error = ex;
            return null;
        }
    }
    protected void onPostExecute(List<Claim> claims) {
        Helper.setViewVisibility(progressBar, View.GONE);
        if (handler != null) {
            if (claims != null) {
                handler.onSuccess(claims, claims.size() < size);
            } else {
                handler.onError(error);
            }
        }
    }
}
