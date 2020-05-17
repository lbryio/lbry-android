package io.lbry.browser.tasks;

import android.os.AsyncTask;

import java.util.HashMap;
import java.util.Map;

import io.lbry.browser.exceptions.ApiCallException;
import io.lbry.browser.utils.Lbry;

public class SetSdkSettingTask extends AsyncTask<Void, Void, Boolean> {
    private String key;
    private String value;
    private GenericTaskHandler handler;
    private Exception error;
    public SetSdkSettingTask(String key, String value, GenericTaskHandler handler) {
        this.key = key;
        this.value = value;
        this.handler = handler;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            Map<String, Object> options = new HashMap<>();
            options.put("key", key);
            options.put("value", value);
            Lbry.genericApiCall("setting_set", options);
            return true;
        } catch (ApiCallException ex) {
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
