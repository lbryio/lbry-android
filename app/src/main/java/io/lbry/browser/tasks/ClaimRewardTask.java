package io.lbry.browser.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;

import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import io.lbry.browser.R;
import io.lbry.browser.exceptions.ApiCallException;
import io.lbry.browser.exceptions.LbryioRequestException;
import io.lbry.browser.exceptions.LbryioResponseException;
import io.lbry.browser.utils.Helper;
import io.lbry.browser.utils.Lbry;
import io.lbry.browser.utils.Lbryio;

public class ClaimRewardTask extends AsyncTask<Void, Void, String> {

    private Context context;
    private String type;
    private String claimCode;
    private View progressView;
    private double amountClaimed;
    private ClaimRewardHandler handler;
    private Exception error;

    public ClaimRewardTask(String type, String claimCode, View progressView, Context context, ClaimRewardHandler handler) {
        this.type = type;
        this.claimCode = claimCode;
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
            // Get a new wallet address for the reward
            String address = (String) Lbry.genericApiCall(Lbry.METHOD_ADDRESS_UNUSED);
            Map<String, String> options = new HashMap<>();
            options.put("reward_type", type);
            options.put("wallet_address", address);
            if (!Helper.isNullOrEmpty(claimCode)) {
                options.put("claim_code", claimCode);

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
        } catch (ApiCallException | LbryioRequestException | LbryioResponseException ex) {
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

    public interface ClaimRewardHandler {
        void onSuccess(double amountClaimed, String message);
        void onError(Exception error);
    }
}
