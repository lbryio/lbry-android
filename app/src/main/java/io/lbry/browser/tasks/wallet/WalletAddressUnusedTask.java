package io.lbry.browser.tasks.wallet;

import android.os.AsyncTask;

import io.lbry.browser.exceptions.ApiCallException;
import io.lbry.browser.utils.Helper;
import io.lbry.browser.utils.Lbry;

public class WalletAddressUnusedTask extends AsyncTask<Void, Void, String> {
    private final WalletAddressUnusedHandler handler;
    private Exception error;

    public WalletAddressUnusedTask(WalletAddressUnusedHandler handler) {
        this.handler = handler;
    }

    protected void onPreExecute() {
        if (handler != null) {
            handler.beforeStart();
        }
    }

    protected String doInBackground(Void... params) {
        String address = null;
        try {
            address = (String) Lbry.genericApiCall(Lbry.METHOD_ADDRESS_UNUSED);
        } catch (ApiCallException | ClassCastException ex) {
            error = ex;
        }

        return address;
    }

    protected void onPostExecute(String unusedAddress) {
        if (handler != null) {
            if (!Helper.isNullOrEmpty(unusedAddress)) {
                handler.onSuccess(unusedAddress);
            } else {
                handler.onError(error);
            }
        }
    }

    public interface WalletAddressUnusedHandler {
        void beforeStart();
        void onSuccess(String newAddress);
        void onError(Exception error);
    }
}
