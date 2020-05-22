package io.lbry.browser.tasks.claim;

import android.os.AsyncTask;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import io.lbry.browser.exceptions.ApiCallException;
import io.lbry.browser.model.Claim;
import io.lbry.browser.utils.Helper;
import io.lbry.browser.utils.Lbry;

public class PublishClaimTask extends AsyncTask<Void, Void, Claim> {
    private Claim claim;
    private String filePath;
    private View progressView;
    private ClaimResultHandler handler;
    private Exception error;
    public PublishClaimTask(Claim claim, String filePath, View progressView, ClaimResultHandler handler) {
        this.claim = claim;
        this.filePath = filePath;
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
        Claim.StreamMetadata metadata = (Claim.StreamMetadata) claim.getValue();
        DecimalFormat amountFormat = new DecimalFormat(Helper.SDK_AMOUNT_FORMAT, new DecimalFormatSymbols(Locale.US));

        Map<String, Object> options = new HashMap<>();
        options.put("blocking", true);
        options.put("name", claim.getName());
        options.put("bid", amountFormat.format(new BigDecimal(claim.getAmount()).doubleValue()));
        options.put("title", Helper.isNullOrEmpty(claim.getTitle()) ? "" : claim.getTitle());
        options.put("description", Helper.isNullOrEmpty(claim.getDescription()) ? "" : claim.getDescription());
        options.put("thumbnail_url", Helper.isNullOrEmpty(claim.getThumbnailUrl()) ? "" : claim.getThumbnailUrl());

        if (!Helper.isNullOrEmpty(filePath)) {
            options.put("file_path", filePath);
        }
        if (claim.getTags() != null && claim.getTags().size() > 0) {
            options.put("tags", new ArrayList<>(claim.getTags()));
        }
        if (metadata.getFee() != null) {
            options.put("fee_currency", metadata.getFee().getCurrency());
            options.put("fee_amount", amountFormat.format(new BigDecimal(metadata.getFee().getAmount()).doubleValue()));
        }
        if (claim.getSigningChannel() != null) {
            options.put("channel_id", claim.getSigningChannel().getClaimId());
        }
        if (metadata.getLanguages() != null && metadata.getLanguages().size() > 0) {
            options.put("languages", metadata.getLanguages());
        }
        if (!Helper.isNullOrEmpty(metadata.getLicense())) {
            options.put("license", metadata.getLicense());
        }
        if (!Helper.isNullOrEmpty(metadata.getLicenseUrl())) {
            options.put("license_url", metadata.getLicenseUrl());
        }

        Claim claimResult = null;
        try {
            JSONObject result = (JSONObject) Lbry.genericApiCall(Lbry.METHOD_PUBLISH, options);
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
