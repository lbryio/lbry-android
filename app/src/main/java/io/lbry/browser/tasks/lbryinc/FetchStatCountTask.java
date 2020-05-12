package io.lbry.browser.tasks.lbryinc;

import android.os.AsyncTask;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;

import io.lbry.browser.exceptions.LbryioRequestException;
import io.lbry.browser.exceptions.LbryioResponseException;
import io.lbry.browser.utils.Helper;
import io.lbry.browser.utils.Lbryio;

public class FetchStatCountTask extends AsyncTask<Void, Void, Integer> {
    public static final int STAT_VIEW_COUNT = 1;
    public static final int STAT_SUB_COUNT = 2;

    private String claimId;
    private int stat;
    private FetchStatCountHandler handler;
    private View progressView;
    private Exception error;

    public FetchStatCountTask(int stat, String claimId, View progressView, FetchStatCountHandler handler) {
        this.stat = stat;
        this.claimId = claimId;
        this.progressView = progressView;
        this.handler = handler;
    }

    protected void onPreExecute() {
        Helper.setViewVisibility(progressView, View.VISIBLE);
    }

    protected Integer doInBackground(Void... params) {
        int count = -1;
        try {
            if (stat != STAT_VIEW_COUNT && stat != STAT_SUB_COUNT) {
                throw new LbryioRequestException("Invalid stat count specified.");
            }

            JSONArray results = (JSONArray)
                    Lbryio.parseResponse(Lbryio.call(
                            stat == STAT_VIEW_COUNT ? "file" : "subscription",
                            stat == STAT_VIEW_COUNT ? "view_count" : "sub_count",
                            Lbryio.buildSingleParam("claim_id", claimId),
                            Helper.METHOD_GET, null));
            if (results.length() > 0) {
                count = results.getInt(0);
            }
        } catch (ClassCastException | LbryioRequestException | LbryioResponseException | JSONException ex) {
            error = ex;
        }

        return count;
    }

    protected void onPostExecute(Integer count) {
        Helper.setViewVisibility(progressView, View.GONE);
        if (handler != null) {
            if (count > -1) {
                handler.onSuccess(count);
            } else {
                handler.onError(error);
            }
        }
    }

    public interface FetchStatCountHandler {
        void onSuccess(int count);
        void onError(Exception error);
    }
}
