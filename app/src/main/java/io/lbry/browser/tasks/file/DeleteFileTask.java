package io.lbry.browser.tasks.file;

import android.os.AsyncTask;

import java.util.HashMap;
import java.util.Map;

import io.lbry.browser.exceptions.ApiCallException;
import io.lbry.browser.tasks.GenericTaskHandler;
import io.lbry.browser.utils.Lbry;

public class DeleteFileTask extends AsyncTask<Void, Void, Boolean> {
    private String claimId;
    private Exception error;
    private GenericTaskHandler handler;

    public DeleteFileTask(String claimId, GenericTaskHandler handler) {
        this.claimId = claimId;
        this.handler = handler;
    }

    protected Boolean doInBackground(Void... params) {
        try {
            Map<String, Object> options = new HashMap<>();
            options.put("claim_id", claimId);
            options.put("delete_from_download_dir", true);
            return (boolean) Lbry.genericApiCall(Lbry.METHOD_FILE_DELETE, options);
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
