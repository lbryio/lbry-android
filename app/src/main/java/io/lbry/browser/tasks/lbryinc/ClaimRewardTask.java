package io.lbry.browser.tasks.lbryinc;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.lbry.browser.R;
import io.lbry.browser.exceptions.ApiCallException;
import io.lbry.browser.exceptions.LbryioRequestException;
import io.lbry.browser.exceptions.LbryioResponseException;
import io.lbry.browser.model.Claim;
import io.lbry.browser.model.lbryinc.Reward;
import io.lbry.browser.utils.Helper;
import io.lbry.browser.utils.Lbry;
import io.lbry.browser.utils.Lbryio;

public class ClaimRewardTask extends AsyncTask<Void, Void, String> {

    private Context context;
    private String type;
    private String rewardCode;
    private View progressView;
    private double amountClaimed;
    private ClaimRewardHandler handler;
    private Exception error;

    public ClaimRewardTask(String type, String rewardCode, View progressView, Context context, ClaimRewardHandler handler) {
        this.type = type;
        this.rewardCode = rewardCode;
        this.progressView = progressView;
        this.context = context;
        this.handler = handler;
    }

    protected void onPreExecute() {
        Helper.setViewVisibility(progressView, View.VISIBLE);
    }

    public String doInBackground(Void... params) {
        String message = null;
        try {
            String txid = null;
            if (Reward.TYPE_FIRST_CHANNEL.equalsIgnoreCase(type)) {
                // fetch a channel
                txid = fetchSingleClaimTxid(Claim.TYPE_CHANNEL);
            } else if (Reward.TYPE_FIRST_PUBLISH.equalsIgnoreCase(type)) {
                // fetch a publish
                txid = fetchSingleClaimTxid(Claim.TYPE_STREAM);
            }

            // Get a new wallet address for the reward
            String address = (String) Lbry.genericApiCall(Lbry.METHOD_ADDRESS_UNUSED);
            Map<String, String> options = new HashMap<>();
            options.put("reward_type", type);
            options.put("wallet_address", address);
            if (!Helper.isNullOrEmpty(rewardCode)) {
                options.put("code", rewardCode);
            }
            if (!Helper.isNullOrEmpty(txid)) {
                options.put("transaction_id", txid);
            }

            JSONObject reward = (JSONObject) Lbryio.parseResponse(
                    Lbryio.call("reward", "claim", options, Helper.METHOD_POST, null));
            amountClaimed = Helper.getJSONDouble("reward_amount", 0, reward);
            String defaultMessage = context != null ?
                    context.getResources().getQuantityString(
                            R.plurals.claim_reward_message,
                            amountClaimed == 1 ? 1 : 2,
                            new DecimalFormat(Helper.LBC_CURRENCY_FORMAT_PATTERN).format(amountClaimed)) : "";
            message = Helper.getJSONString("reward_notification", defaultMessage, reward);
        } catch (ApiCallException | JSONException | LbryioRequestException | LbryioResponseException ex) {
            error = ex;
        }

        return message;
    }

    protected void onPostExecute(String message) {
        Helper.setViewVisibility(progressView, View.INVISIBLE);
        if (handler != null) {
            if (message != null) {
                handler.onSuccess(amountClaimed, message);
            } else {
                handler.onError(error);
            }
        }
    }

    private String fetchSingleClaimTxid(String claimType) throws ApiCallException, JSONException {
        Map<String, Object> options = new HashMap<>();
        options.put("claim_type", claimType);
        options.put("page", 1);
        options.put("page_size", 1);
        options.put("resolve", true);

        JSONObject result = (JSONObject) Lbry.genericApiCall(Lbry.METHOD_CLAIM_LIST, options);
        JSONArray items = result.getJSONArray("items");
        if (items.length() > 0) {
            Claim claim = Claim.fromJSONObject(items.getJSONObject(0));
            return claim.getTxid();
        }

        return null;
    }

    public interface ClaimRewardHandler {
        void onSuccess(double amountClaimed, String message);
        void onError(Exception error);
    }
}
