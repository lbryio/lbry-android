package io.lbry.browser.tasks.content;

import android.os.AsyncTask;
import android.view.View;

import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import io.lbry.browser.exceptions.ApiCallException;
import io.lbry.browser.model.Claim;
import io.lbry.browser.tasks.GenericTaskHandler;
import io.lbry.browser.utils.Helper;
import io.lbry.browser.utils.Lbry;

public class ChannelCreateUpdateTask extends AsyncTask<Void, Void, Boolean> {
    private Claim claim;
    private BigDecimal deposit;
    private boolean update;
    private Exception error;
    private GenericTaskHandler handler;
    private View progressView;

    public ChannelCreateUpdateTask(Claim claim, BigDecimal deposit, boolean update, View progressView, GenericTaskHandler handler) {
        this.claim = claim;
        this.deposit = deposit;
        this.update = update;
        this.progressView = progressView;
        this.handler = handler;
    }

    protected void onPreExecute() {
        Helper.setViewVisibility(progressView, View.VISIBLE);
        if (handler != null) {
            handler.beforeStart();
        }
    }
    protected Boolean doInBackground(Void... params) {
        Map<String, Object> options = new HashMap<>();
        if (!update) {
            options.put("name", claim.getName());
        } else {
            options.put("claim_id", claim.getClaimId());
        }
        options.put("bid", new DecimalFormat(Helper.SDK_AMOUNT_FORMAT).format(deposit.doubleValue()));
        options.put("title", claim.getTitle());
        options.put("cover_url", claim.getCoverUrl());
        options.put("thumbnail_url", claim.getThumbnailUrl());
        options.put("description", claim.getDescription());
        options.put("website_url", claim.getWebsiteUrl());
        options.put("email", claim.getEmail());
        options.put("tags", claim.getTags());
        options.put("blocking", true);

        String method = !update ? Lbry.METHOD_CHANNEL_CREATE : Lbry.METHOD_CHANNEL_UPDATE;
        try {
            Lbry.genericApiCall(method, options);
        } catch (ApiCallException | ClassCastException ex) {
            error = ex;
            return false;
        }

        return true;
    }
    protected void onPostExecute(Boolean result) {
        Helper.setViewVisibility(progressView, View.GONE);
        if (handler != null) {
            if (result) {
                handler.onSuccess();
            } else {
                handler.onError(error);
            }
        }
    }
}
