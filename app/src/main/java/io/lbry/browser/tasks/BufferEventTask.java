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
    private static final String ENDPOINT = "https://collector-service.api.lbry.tv/api/v1/events/video";

    private final String streamUrl;
    private final String userIdHash;
    private final long streamDuration;
    private final long streamPosition;
    private final long bufferDuration;

    public BufferEventTask(String streamUrl, long streamDuration, long streamPosition, long bufferDuration, String userIdHash) {
        this.streamUrl = streamUrl;
        this.bufferDuration = bufferDuration;
        this.streamDuration = streamDuration;
        this.streamPosition = streamPosition;
        this.userIdHash = userIdHash;
    }

    protected Void doInBackground(Void... params) {
        JSONObject requestBody = new JSONObject();
        JSONObject data = new JSONObject();
        try {
            data.put("url", streamUrl);
            data.put("position", streamPosition);
            data.put("stream_duration", streamDuration);
            //data.put("duration", bufferDuration);

            requestBody.put("device", "android");
            requestBody.put("type", "buffering");
            requestBody.put("client", userIdHash);
            requestBody.put("data", data);

            RequestBody body = RequestBody.create(requestBody.toString(), Helper.JSON_MEDIA_TYPE);
            Request request =  new Request.Builder().url(ENDPOINT).post(body).build();
            OkHttpClient client = new OkHttpClient.Builder().
                    writeTimeout(60, TimeUnit.SECONDS).
                    readTimeout(60, TimeUnit.SECONDS).
                    build();

            Response response = client.newCall(request).execute();
            String responseString = response.body().string();
            Log.d(TAG, String.format("buffer event sent: %s", responseString));
        } catch (Exception ex) {
            // we don't want to fail if a buffer event fails to register
            Log.d(TAG, String.format("buffer event log failed: %s", ex.getMessage()), ex);
        }

        return null;
    }
}
