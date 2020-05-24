package io.lbry.browser.tasks.wallet;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.lbry.browser.exceptions.ApiCallException;
import io.lbry.browser.model.WalletBalance;
import io.lbry.browser.tasks.GenericTaskHandler;
import io.lbry.browser.utils.Helper;
import io.lbry.browser.utils.Lbry;

public class UnlockTipsTask extends AsyncTask<Void, Void, Boolean> {

    private GenericTaskHandler handler;
    private Exception error;

    public UnlockTipsTask(GenericTaskHandler handler) {
        this.handler = handler;
    }

    public Boolean doInBackground(Void... params) {

        List<String> txids = new ArrayList<>();
        List<String> claimIds = new ArrayList<>();

        try {
            Map<String, Object> options = new HashMap<>();
            options.put("type", "support");
            options.put("is_not_my_input", true);
            options.put("is_my_output", true);
            JSONObject result = (JSONObject) Lbry.genericApiCall(Lbry.METHOD_TXO_LIST, options);
            if (result.has("items") && !result.isNull("items")) {
                JSONArray items = result.getJSONArray("items");
                for (int i = 0; i < items.length(); i++) {
                    JSONObject item = items.getJSONObject(i);
                    String txid = Helper.getJSONString("txid", null, item);
                    String claimId = Helper.getJSONString("claim_id", null, item);
                    if (!Helper.isNullOrEmpty(txid) && !Helper.isNullOrEmpty(claimId)) {
                        txids.add(txid);
                        claimIds.add(claimId);
                    }
                }
            }

            if (txids.size() > 0 && txids.size() == claimIds.size()) {
                options = new HashMap<>();
                options.put("txid", txids);
                options.put("claim_id", claimIds);
                options.put("blocking", true);
                Lbry.genericApiCall(Lbry.METHOD_TXO_SPEND, options);
            }

            return true;
        } catch (ApiCallException | ClassCastException | JSONException ex) {
            error = ex;
            return false;
        }
    }

    protected void onPostExecute(Boolean result) {
        if (handler != null) {
            if (result) {
                handler.onSuccess();
            } else {
                handler.onError(error);
            }
        }
    }
}
