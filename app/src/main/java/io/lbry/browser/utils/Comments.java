package io.lbry.browser.utils;

import android.os.Build;

import org.apache.commons.codec.binary.Hex;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import io.lbry.browser.exceptions.ApiCallException;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Comments {
    private static final String STATUS_ENDPOINT = "https://comments.lbry.com";
    public static final String COMMENT_SERVER_ENDPOINT = "https://comments.lbry.com/api/v2";

    public static JSONObject channelSign(JSONObject commentBody, String channelId, String channelName) throws ApiCallException, JSONException {
        byte[] commentBodyBytes = commentBody.getString("comment").getBytes(StandardCharsets.UTF_8);
        String encodedCommentBody;

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O_MR1)
            encodedCommentBody = Hex.encodeHexString(commentBodyBytes, false);
        else
            encodedCommentBody = new String(Hex.encodeHex(commentBodyBytes));

        Map<String, Object> signingParams = new HashMap<>(3);
        signingParams.put("hexdata", encodedCommentBody);
        signingParams.put("channel_id", channelId);
        signingParams.put("channel_name", channelName);

        return (JSONObject) Lbry.genericApiCall("channel_sign", signingParams);
    }

    /**
     * Performs request to default Comment Server
     * @param params JSON containing parameters to send to the server
     * @param method One of the available methods for comments
     * @return Response from the server
     * @throws IOException throwable from OkHttpClient execute()
     */
    public static Response performRequest(JSONObject params, String method) throws IOException {
        return performRequest(COMMENT_SERVER_ENDPOINT, params, method);
    }

    /**
     * Performs the request to Comment Server
     * @param commentServer Url where to direct the request
     * @param params JSON containing parameters to send to the server
     * @param method One of the available methods for comments
     * @return Response from the server
     * @throws IOException throwable from OkHttpClient execute()
     */
    public static Response performRequest(String commentServer, JSONObject params, String method) throws IOException {
        final MediaType JSON = MediaType.get("application/json; charset=utf-8");

        Map<String, Object> requestParams = new HashMap<>(4);
        requestParams.put("jsonrpc", "2.0");
        requestParams.put("id", TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
        requestParams.put("method", method);
        requestParams.put("params", params);

        RequestBody requestBody = RequestBody.create(Lbry.buildJsonParams(requestParams).toString(), JSON);

        Request commentCreateRequest = new Request.Builder()
                                                  .url(commentServer.concat("?m=").concat(method))
                                                  .post(requestBody)
                                                  .build();

        OkHttpClient client = new OkHttpClient.Builder().writeTimeout(30, TimeUnit.SECONDS)
                                                        .readTimeout(30, TimeUnit.SECONDS)
                                                        .build();

        return client.newCall(commentCreateRequest).execute();
    }

    public static void checkCommentsEndpointStatus() throws IOException, JSONException, ApiCallException {
        Request request = new Request.Builder().url(STATUS_ENDPOINT).build();
        OkHttpClient client = new OkHttpClient.Builder().writeTimeout(30, TimeUnit.SECONDS)
                                                        .readTimeout(30, TimeUnit.SECONDS)
                                                        .build();
        Response response = client.newCall(request).execute();
        JSONObject status = new JSONObject(Objects.requireNonNull(response.body()).string());
        String statusText = Helper.getJSONString("text", null, status);
        boolean isRunning = Helper.getJSONBoolean("is_running", false, status);
        if (!"ok".equalsIgnoreCase(statusText) || !isRunning) {
            throw new ApiCallException("The comment server is not available at this time. Please try again later.");
        }
    }
}
