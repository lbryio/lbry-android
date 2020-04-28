package io.lbry.browser.tasks.wallet;

import android.os.AsyncTask;
import android.view.View;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import io.lbry.browser.exceptions.ApiCallException;
import io.lbry.browser.utils.Helper;
import io.lbry.browser.utils.Lbry;

public class WalletSendTask extends AsyncTask<Void, Void, Boolean> {
    private String recipientAddress;
    private String amount;
    private View progressView;
    private WalletSendHandler handler;
    private Exception error;

    public WalletSendTask(String recipientAddress, String amount, View progressView, WalletSendHandler handler) {
        this.recipientAddress = recipientAddress;
        this.amount = amount;
        this.progressView = progressView;
        this.handler = handler;
    }

    protected void onPreExecute() {
        Helper.setViewVisibility(progressView, View.VISIBLE);
    }
    protected Boolean doInBackground(Void... params) {
        try {
            Map<String, Object> options = new HashMap<>();
            options.put("addresses", Arrays.asList(recipientAddress));
            options.put("amount", amount);
            Lbry.genericApiCall(Lbry.METHOD_WALLET_SEND, options);
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

    public interface WalletSendHandler {
        void onSuccess();
        void onError(Exception error);
    }
}
