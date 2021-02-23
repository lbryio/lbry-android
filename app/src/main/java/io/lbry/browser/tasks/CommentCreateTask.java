package io.lbry.browser.tasks;

import android.os.AsyncTask;
import android.view.View;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

import io.lbry.browser.exceptions.ApiCallException;
import io.lbry.browser.model.Comment;
import io.lbry.browser.utils.Comments;
import io.lbry.browser.utils.Helper;
import okhttp3.Response;

public class CommentCreateTask extends AsyncTask<Void, Void, Comment> {
    private final Comment comment;
    private final View progressView;
    private final CommentCreateWithTipHandler handler;
    private Exception error;

    public CommentCreateTask(Comment comment, View progressView, CommentCreateWithTipHandler handler) {
        this.comment = comment;
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
            Comments.checkCommentsEndpointStatus();

            JSONObject comment_body = new JSONObject();
            comment_body.put("comment", comment.getText());
            comment_body.put("claim_id", comment.getClaimId());
            if (!Helper.isNullOrEmpty(comment.getParentId())) {
                comment_body.put("parent_id", comment.getParentId());
            }
            comment_body.put("channel_id", comment.getChannelId());
            comment_body.put("channel_name", comment.getChannelName());

            JSONObject jsonChannelSign = Comments.channelSign(comment_body, comment.getChannelId(), comment.getChannelName());

            if (jsonChannelSign.has("signature") && jsonChannelSign.has("signing_ts")) {
                comment_body.put("signature", jsonChannelSign.getString("signature"));
                comment_body.put("signing_ts", jsonChannelSign.getString("signing_ts"));
            }

            Response resp = Comments.performRequest(comment_body, "comment.Create");
            String responseString = Objects.requireNonNull(resp.body()).string();
            resp.close();
            JSONObject jsonResponse = new JSONObject(responseString);

            if (jsonResponse.has("result"))
                createdComment = Comment.fromJSONObject(jsonResponse.getJSONObject("result"));
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
