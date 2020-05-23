package io.lbry.browser.tasks.wallet;

import android.os.AsyncTask;

import org.json.JSONObject;

import java.math.BigDecimal;

import io.lbry.browser.exceptions.ApiCallException;
import io.lbry.browser.model.WalletBalance;
import io.lbry.browser.utils.Helper;
import io.lbry.browser.utils.Lbry;

public class WalletBalanceTask extends AsyncTask<Void, Void, WalletBalance> {
    private WalletBalanceHandler handler;
    private Exception error;

    public WalletBalanceTask(WalletBalanceHandler handler) {
        this.handler = handler;
    }

    protected WalletBalance doInBackground(Void... params) {
       WalletBalance balance;
        try {
            JSONObject json = (JSONObject) Lbry.genericApiCall(Lbry.METHOD_WALLET_BALANCE);
            balance = WalletBalance.fromJSONObject(json);
        } catch (ApiCallException | ClassCastException ex) {
            error = ex;
            return null;
        }

        return balance;
    }

    protected void onPostExecute(WalletBalance walletBalance) {
        if (handler != null) {
            if (walletBalance != null) {
                handler.onSuccess(walletBalance);
            } else {
                handler.onError(error);
            }
        }
    }

    public interface WalletBalanceHandler {
        void onSuccess(WalletBalance walletBalance);
        void onError(Exception error);
    }
}
