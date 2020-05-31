package io.lbry.browser.tasks;

import android.os.AsyncTask;
import android.view.View;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.lbry.browser.exceptions.ApiCallException;
import io.lbry.browser.model.Comment;
import io.lbry.browser.utils.Helper;
import io.lbry.browser.utils.Lbry;
import io.lbry.browser.utils.Lbryio;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CommentCreateWithTipTask extends AsyncTask<Void, Void, Comment> {
    private static final String STATUS_ENDPOINT = "https://comments.lbry.com";

    private Comment comment;
    private BigDecimal amount;
    private View progressView;
    private CommentCreateWithTipHandler handler;
    private Exception error;

    public CommentCreateWithTipTask(Comment comment, BigDecimal amount, View progressView, CommentCreateWithTipHandler handler) {
        this.comment = comment;
        this.amount = amount;
        this.progressView = progressView;
        this.handler = handler;
    }

    protected void onPreExecute() {
        Helper.setViewVisibility(progressView, View.VISIBLE);
    }

    public Comment doInBackground(Void... params) {
        Comment createdComment = null;
        try {
            // check comments status endpoint
            Request request = new Request.Builder().url(STATUS_ENDPOINT).build();
            OkHttpClient client = new OkHttpClient.Builder().
                    writeTimeout(30, TimeUnit.SECONDS).
                    readTimeout(30, TimeUnit.SECONDS).
                    build();
            Response response = client.newCall(request).execute();
            JSONObject status = new JSONObject(response.body().string());
            String statusText = Helper.getJSONString("text", null, status);
            boolean isRunning = Helper.getJSONBoolean("is_running", false, status);
            if (!"ok".equalsIgnoreCase(statusText) || !isRunning) {
                throw new ApiCallException("The comment server is not available at this time. Please try again later.");
            }

            Map<String, Object> options = new HashMap<>();
            options.put("blocking", true);
            options.put("claim_id", comment.getClaimId());
            options.put("amount", new DecimalFormat(Helper.SDK_AMOUNT_FORMAT, new DecimalFormatSymbols(Locale.US)).format(amount.doubleValue()));
            options.put("tip", true);
            Lbry.genericApiCall(Lbry.METHOD_SUPPORT_CREATE, options);

            options = new HashMap<>();
            options.put("comment", comment.getText());
            options.put("claim_id", comment.getClaimId());
            options.put("channel_id", comment.getChannelId());
            options.put("channel_name", comment.getChannelName());
            if (!Helper.isNullOrEmpty(comment.getParentId())) {
                options.put("parent_id", comment.getParentId());
            }
            JSONObject jsonObject = (JSONObject) Lbry.genericApiCall(Lbry.METHOD_COMMENT_CREATE, options);
            createdComment = Comment.fromJSONObject(jsonObject);
        } catch (ApiCallException | ClassCastException | IOException | JSONException ex) {
            error = ex;
        }

        return createdComment;
    }

    protected void onPostExecute(Comment createdComment) {
        Helper.setViewVisibility(progressView, View.GONE);
        if (handler != null) {
            if (createdComment != null) {
                handler.onSuccess(createdComment);
            } else {
                handler.onError(error);
            }
        }
    }

    public interface CommentCreateWithTipHandler {
        void onSuccess(Comment createdComment);
        void onError(Exception error);
    }
}
