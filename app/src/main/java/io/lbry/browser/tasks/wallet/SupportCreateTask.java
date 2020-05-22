package io.lbry.browser.tasks.wallet;

import android.os.AsyncTask;
import android.view.View;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import io.lbry.browser.exceptions.ApiCallException;
import io.lbry.browser.tasks.GenericTaskHandler;
import io.lbry.browser.utils.Helper;
import io.lbry.browser.utils.Lbry;

public class SupportCreateTask extends AsyncTask<Void, Void, Boolean> {
    private String claimId;
    private BigDecimal amount;
    private boolean tip;
    private View progressView;
    private GenericTaskHandler handler;
    private Exception error;

    public SupportCreateTask(String claimId, BigDecimal amount, boolean tip, View progressView, GenericTaskHandler handler) {
        this.claimId = claimId;
        this.amount = amount;
        this.tip = tip;
        this.progressView = progressView;
        this.handler = handler;
    }

    protected void onPreExecute() {
        if (handler != null) {
            handler.beforeStart();
        }
        Helper.setViewVisibility(progressView, View.VISIBLE);
    }
    protected Boolean doInBackground(Void... params) {
        try {
            Map<String, Object> options = new HashMap<>();
            options.put("claim_id", claimId);
            options.put("amount", new DecimalFormat(Helper.SDK_AMOUNT_FORMAT, new DecimalFormatSymbols(Locale.US)).format(amount.doubleValue()));
            options.put("tip", tip);
            Lbry.genericApiCall(Lbry.METHOD_SUPPORT_CREATE, options);
        } catch (ApiCallException ex) {
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
