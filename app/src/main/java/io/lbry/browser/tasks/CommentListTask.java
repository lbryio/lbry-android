package io.lbry.browser.tasks;

import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.lbry.browser.model.Comment;
import io.lbry.browser.utils.Helper;
import io.lbry.browser.utils.Lbry;

public class CommentListTask extends AsyncTask<Void, Void, List<Comment>> {
    private final int page;
    private final int pageSize;
    private final String claim;
    private ProgressBar progressBar;
    private CommentListHandler handler;
    private Exception error;

    public CommentListTask(int page, int pageSize, String claim, ProgressBar progressBar, CommentListHandler handler) {
        this.page = page;
        this.pageSize = pageSize;
        this.claim = claim;
        this.progressBar = progressBar;
        this.handler = handler;
    }

    protected void onPreExecute() {
        Helper.setViewVisibility(progressBar, View.VISIBLE);
    }

    protected List<Comment> doInBackground(Void... voids) {
        List<Comment> comments = null;

        try {
            Map<String, Object> options = new HashMap<>();

            options.put("claim_id", claim);
            options.put("page", page);
            options.put("page_size", pageSize);
            options.put("include_replies", false);
            options.put("is_channel_signature_valid", true);
            options.put("visible", true);
            options.put("hidden", false);

            JSONObject result = (JSONObject) Lbry.genericApiCall(Lbry.METHOD_COMMENT_LIST, options);
            JSONArray items = result.getJSONArray("items");
            comments = new ArrayList<>();
            for (int i = 0; i < items.length(); i++) {
                comments.add(Comment.fromJSONObject(items.getJSONObject(i)));
            }
        } catch (Exception ex) {
            error = ex;
        }
        return comments;
    }

    protected void onPostExecute(List<Comment> comments) {
        Helper.setViewVisibility(progressBar, View.GONE);
        if (handler != null) {
            if (comments != null && error == null) {
                handler.onSuccess(comments);
            } else {
                handler.onError(error);
                if (error != null) {
                }
            }
        }
    }
}
