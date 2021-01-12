package io.lbry.browser.tasks;

import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
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
    private final ProgressBar progressBar;
    private final CommentListHandler handler;
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
            options.put("hidden", false);
            options.put("include_replies", false);
            options.put("is_channel_signature_valid", true);
            options.put("skip_validation", true);
            options.put("visible", true);

            JSONObject result = (JSONObject) Lbry.genericApiCall(Lbry.METHOD_COMMENT_LIST, options);
            JSONArray items = result.getJSONArray("items");

            List<Comment> children = new ArrayList<>();
            comments = new ArrayList<>();
            for (int i = 0; i < items.length(); i++) {
                Comment comment = Comment.fromJSONObject(items.getJSONObject(i));
                if (comment != null) {
                    if (!Helper.isNullOrEmpty(comment.getParentId())) {
                        children.add(comment);
                    } else {
                        comments.add(comment);
                    }
                }
            }

            // Sort all replies from oldest to newest at once
            Collections.sort(children);

            for (Comment child : children) {
                for (Comment parent : comments) {
                    if (parent.getId().equalsIgnoreCase(child.getParentId())) {
                        parent.addReply(child);
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            error = ex;
        }
        return comments;
    }

    protected void onPostExecute(List<Comment> comments) {
        Helper.setViewVisibility(progressBar, View.GONE);
        if (handler != null) {
            if (comments != null) {
                handler.onSuccess(comments, comments.size() < pageSize);
            } else {
                handler.onError(error);
            }
        }
    }
}
