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
import io.lbry.browser.utils.Helper;
import io.lbry.browser.utils.Lbry;

public class StreamRepostTask extends AsyncTask<Void, Void, Claim> {
    private String name;
    private BigDecimal bid;
    private String claimId;
    private String channelId;
    private View progressView;
    private ClaimResultHandler handler;
    private Exception error;

    public StreamRepostTask(String name, BigDecimal bid, String claimId, String channelId, View progressView, ClaimResultHandler handler) {
        this.name = name;
        this.bid = bid;
        this.claimId = claimId;
        this.channelId = channelId;
        this.progressView = progressView;
        this.handler = handler;
    }

    protected Claim doInBackground(Void... params) {
        Claim claimResult = null;
        try {
            Map<String, Object> options = new HashMap<>();
            options.put("name", name);
            options.put("bid", new DecimalFormat(Helper.SDK_AMOUNT_FORMAT, new DecimalFormatSymbols(Locale.US)).format(bid.doubleValue()));
            options.put("claim_id", claimId);
            options.put("channel_id", channelId);

            JSONObject result = (JSONObject) Lbry.genericApiCall(Lbry.METHOD_STREAM_REPOST, options);
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
