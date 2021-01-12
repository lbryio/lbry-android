package io.lbry.browser.tasks.claim;

import android.os.AsyncTask;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.lbry.browser.exceptions.ApiCallException;
import io.lbry.browser.model.Claim;
import io.lbry.browser.utils.Helper;
import io.lbry.browser.utils.Lbry;

public class PurchaseListTask extends AsyncTask<Void, Void, List<Claim>> {
    private String claimId;
    private int page;
    private int pageSize;
    private final ClaimSearchResultHandler handler;
    private final View progressView;
    private Exception error;

    public PurchaseListTask(String claimId, View progressView, ClaimSearchResultHandler handler) {
        this.claimId = claimId;
        this.progressView = progressView;
        this.handler = handler;
    }

    public PurchaseListTask(int page, int pageSize, View progressView, ClaimSearchResultHandler handler) {
        this.page = page;
        this.pageSize = pageSize;
        this.progressView = progressView;
        this.handler = handler;
    }

    protected void onPreExecute() {
        Helper.setViewVisibility(progressView, View.VISIBLE);
    }
    protected List<Claim> doInBackground(Void... params) {
        List<Claim> claims = null;
        try {
            Map<String, Object> options = new HashMap<>();
            if (!Helper.isNullOrEmpty(claimId)) {
                options.put("claim_id", claimId);
            }
            if (page > 0) {
                options.put("page", page);
            }
            if (pageSize > 0) {
                options.put("page_size", pageSize);
            }
            options.put("resolve", true);

            JSONObject result = (JSONObject) Lbry.genericApiCall(Lbry.METHOD_PURCHASE_LIST, options);
            JSONArray items = result.getJSONArray("items");
            claims = new ArrayList<>();
            for (int i = 0; i < items.length(); i++) {
                Claim claim = Claim.fromJSONObject(items.getJSONObject(i).getJSONObject("claim"));
                if (!Helper.isNullOrEmpty(claim.getClaimId())) {
                    claims.add(claim);
                    Lbry.addClaimToCache(claim);
                }
            }
        } catch (ApiCallException | JSONException | ClassCastException ex) {
            error = ex;
        }

        return claims;
    }
    protected void onPostExecute(List<Claim> claims) {
        Helper.setViewVisibility(progressView, View.GONE);
        if (handler != null) {
            if (claims != null) {
                handler.onSuccess(claims, claims.size() < pageSize);
            } else {
                handler.onError(error);
            }
        }
    }
}
