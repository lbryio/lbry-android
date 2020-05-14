package io.lbry.browser.tasks.file;

import android.os.AsyncTask;
import android.view.View;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import io.lbry.browser.exceptions.ApiCallException;
import io.lbry.browser.model.LbryFile;
import io.lbry.browser.utils.Helper;
import io.lbry.browser.utils.Lbry;

public class GetFileTask extends AsyncTask<Void, Void, LbryFile> {
    private String uri;
    private boolean saveFile;
    private View progressView;
    private GetFileHandler handler;
    private Exception error;

    public GetFileTask(String uri, boolean saveFile, View progressView, GetFileHandler handler) {
        this.uri = uri;
        this.saveFile = saveFile;
        this.progressView = progressView;
        this.handler = handler;
    }

    protected void onPreExecute() {
        Helper.setViewVisibility(progressView, View.VISIBLE);
        if (handler != null) {
            handler.beforeStart();
        }
    }

    protected LbryFile doInBackground(Void... params) {
        LbryFile file = null;
        try {
            Map<String, Object> options = new HashMap<>();
            options.put("uri", uri);
            options.put("save_file", saveFile);
            JSONObject streamInfo = (JSONObject) Lbry.genericApiCall("get", options);
            if (streamInfo.has("error")) {
                throw new ApiCallException(Helper.getJSONString("error", "", streamInfo));
            }

            file = LbryFile.fromJSONObject(streamInfo);
        } catch (ApiCallException ex) {
            error = ex;
        }

        return file;
    }

    protected void onPostExecute(LbryFile file) {
        Helper.setViewVisibility(progressView, View.GONE);
        if (handler != null) {
            if (file != null) {
                handler.onSuccess(file, saveFile);
            } else {
                handler.onError(error, saveFile);
            }
        }
    }

    public interface GetFileHandler {
        void beforeStart();
        void onSuccess(LbryFile file, boolean saveFile);
        void onError(Exception error, boolean saveFile);
    }
}
