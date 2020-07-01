package io.lbry.browser.tasks;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import io.lbry.browser.utils.Helper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class BufferEventTask extends AsyncTask<Void, Void, Void> {
    private static final String TAG = "LbryBufferEvent";
    private static final String ENDPOINT = "https://api.lbry.tv/api/v1/metric/ui";

    private String streamUrl;
    private String userIdHash;
    private long streamDuration;
    private long streamPosition;

    public BufferEventTask(String streamUrl, long streamDuration, long streamPosition, String userIdHash) {
        this.streamUrl = streamUrl;
        this.streamDuration = streamDuration;
        this.streamPosition = streamPosition;
        this.userIdHash = userIdHash;
    }

    protected Void doInBackground(Void... params) {
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("name", "buffer");
            requestBody.put("mobile", true);
            requestBody.put("url", streamUrl);
            requestBody.put("stream_duration", streamDuration);
            requestBody.put("stream_position", streamPosition);
            requestBody.put("user_id_hash", userIdHash);

            RequestBody body = RequestBody.create(requestBody.toString(), Helper.JSON_MEDIA_TYPE);
            Request request =  new Request.Builder().url(ENDPOINT).post(body).build();
            OkHttpClient client = new OkHttpClient.Builder().
                    writeTimeout(60, TimeUnit.SECONDS).
                    readTimeout(60, TimeUnit.SECONDS).
                    build();

            Response response = client.newCall(request).execute();
            String responseString = response.body().string();
            Log.d(TAG, responseString);
        } catch (Exception ex) {
            // we don't want to fail if a buffer event fails to register
            Log.d(TAG, String.format("buffer event log failed: %s", ex.getMessage()), ex);
        }

        return null;
    }
}
