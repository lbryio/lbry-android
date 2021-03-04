package io.lbry.browser.tasks.wallet;

import android.os.AsyncTask;

import java.util.HashMap;
import java.util.Map;

import io.lbry.browser.exceptions.ApiCallException;
import io.lbry.browser.tasks.GenericTaskHandler;
import io.lbry.browser.utils.Lbry;

public class UnlockTipsTask extends AsyncTask<Void, Void, Boolean> {

    private final GenericTaskHandler handler;
    private Exception error;

    public UnlockTipsTask(GenericTaskHandler handler) {
        this.handler = handler;
    }

    public Boolean doInBackground(Void... params) {

        try {
            // TODO Consolidate transactions when there are more than 500 UTXO

            Map<String, Object> options = new HashMap<>();
            options.put("type", "support");
            options.put("is_not_my_input", true);
            options.put("blocking", true);

            Lbry.genericApiCall(Lbry.METHOD_TXO_SPEND, options);

            return true;
        } catch (ApiCallException | ClassCastException ex) {
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
