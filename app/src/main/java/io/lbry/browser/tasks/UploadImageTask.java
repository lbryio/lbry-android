package io.lbry.browser.tasks;

import android.os.AsyncTask;
import android.view.View;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import java.io.File;
import java.util.concurrent.TimeUnit;

import io.lbry.browser.exceptions.LbryResponseException;
import io.lbry.browser.utils.Helper;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UploadImageTask extends AsyncTask<Void, Void, String> {
    private String filePath;
    private View progressView;
    private UploadThumbnailHandler handler;
    private Exception error;

    public UploadImageTask(String filePath, View progressView, UploadThumbnailHandler handler) {
        this.filePath = filePath;
        this.progressView = progressView;
        this.handler = handler;
    }

    protected void onPreExecute() {
        Helper.setViewVisibility(progressView, View.VISIBLE);
    }
    protected String doInBackground(Void... params) {
        String thumbnailUrl = null;
        try {
            File file = new File(filePath);
            String fileName = file.getName();
            int dotIndex = fileName.lastIndexOf('.');
            String extension = "jpg";
            if (dotIndex > -1) {
                extension = fileName.substring(dotIndex + 1);
            }
            String fileType = String.format("image/%s", extension);
            RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM).
                    addFormDataPart("name", Helper.makeid(24)).
                    addFormDataPart("file", fileName, RequestBody.create(file, MediaType.parse(fileType))).
                    build();
            Request request = new Request.Builder().url("https://spee.ch/api/claim/publish").post(body).build();
            OkHttpClient client = new OkHttpClient.Builder().
                    writeTimeout(300, TimeUnit.SECONDS).
                    readTimeout(300, TimeUnit.SECONDS).
                    build();
            Response response = client.newCall(request).execute();
            JSONObject json = new JSONObject(response.body().string());
            if (json.has("success") && Helper.getJSONBoolean("success", false, json)) {
                JSONObject data = json.getJSONObject("data");
                String url = Helper.getJSONString("url", null, data);
                if (Helper.isNullOrEmpty(url)) {
                    throw new LbryResponseException("Invalid thumbnail url returned after upload.");
                }

                thumbnailUrl = String.format("%s.%s", url, extension);
            } else if (json.has("error") || json.has("message")) {
                JSONObject error = Helper.getJSONObject("error", json);
                String message = null;
                if (error != null) {
                    message = Helper.getJSONString("message", null, error);
                }
                if (Helper.isNullOrEmpty(message)) {
                    message  = Helper.getJSONString("message", null, json);
                }
                throw new LbryResponseException(Helper.isNullOrEmpty(message) ? "The image failed to upload." : message);
            }
        } catch (IOException | JSONException | LbryResponseException ex) {
            error = ex;
        }

        return thumbnailUrl;
    }
    protected void onPostExecute(String thumbnailUrl) {
        Helper.setViewVisibility(progressView, View.GONE);
        if (handler != null) {
            if (!Helper.isNullOrEmpty(thumbnailUrl)) {
                handler.onSuccess(thumbnailUrl);
            } else {
                handler.onError(error);
            }
        }
    }

    public interface UploadThumbnailHandler {
        void onSuccess(String url);
        void onError(Exception error);
    }
}
