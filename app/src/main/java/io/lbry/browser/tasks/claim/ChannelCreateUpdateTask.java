package io.lbry.browser.tasks.claim;

import android.os.AsyncTask;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import io.lbry.browser.exceptions.ApiCallException;
import io.lbry.browser.model.Claim;
import io.lbry.browser.tasks.claim.ClaimResultHandler;
import io.lbry.browser.utils.Helper;
import io.lbry.browser.utils.Lbry;

public class ChannelCreateUpdateTask extends AsyncTask<Void, Void, Claim> {
    private Claim claim;
    private BigDecimal deposit;
    private boolean update;
    private Exception error;
    private ClaimResultHandler handler;
    private View progressView;

    public ChannelCreateUpdateTask(Claim claim, BigDecimal deposit, boolean update, View progressView, ClaimResultHandler handler) {
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
    protected Claim doInBackground(Void... params) {
        Map<String, Object> options = new HashMap<>();
        if (!update) {
            options.put("name", claim.getName());
        } else {
            options.put("claim_id", claim.getClaimId());
        }
        options.put("bid", new DecimalFormat(Helper.SDK_AMOUNT_FORMAT, new DecimalFormatSymbols(Locale.US)).format(deposit.doubleValue()));
        if (!Helper.isNullOrEmpty(claim.getTitle())) {
            options.put("title", claim.getTitle());
        }
        if (!Helper.isNullOrEmpty(claim.getCoverUrl())) {
            options.put("cover_url", claim.getCoverUrl());
        }
        if (!Helper.isNullOrEmpty(claim.getThumbnailUrl())) {
            options.put("thumbnail_url", claim.getThumbnailUrl());
        }
        if (!Helper.isNullOrEmpty(claim.getDescription())) {
            options.put("description", claim.getDescription());
        }
        if (!Helper.isNullOrEmpty(claim.getWebsiteUrl())) {
            options.put("website_url", claim.getWebsiteUrl());
        }
        if (!Helper.isNullOrEmpty(claim.getEmail())) {
            options.put("email", claim.getEmail());
        }
        if (claim.getTags() != null && claim.getTags().size() > 0) {
            options.put("tags", claim.getTags());
        }
        options.put("blocking", true);

        Claim claimResult = null;
        String method = !update ? Lbry.METHOD_CHANNEL_CREATE : Lbry.METHOD_CHANNEL_UPDATE;
        try {
            JSONObject result = (JSONObject) Lbry.genericApiCall(method, options);
            if (result.has("outputs")) {
                JSONArray outputs = result.getJSONArray("outputs");
                for (int i = 0; i < outputs.length(); i++) {
                    JSONObject output = outputs.getJSONObject(i);
                    if (output.has("claim_id") && output.has("claim_op")) {
                        claimResult = Claim.claimFromOutput(output);
                        break;
                    }
                }
            }
        } catch (ApiCallException | ClassCastException | JSONException ex) {
            error = ex;
        }

        return claimResult;
    }

    protected void onPostExecute(Claim result) {
        Helper.setViewVisibility(progressView, View.GONE);
        if (handler != null) {
            if (result != null) {
                handler.onSuccess(result);
            } else {
                handler.onError(error);
            }
        }
    }
}
