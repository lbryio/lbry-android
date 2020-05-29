package io.lbry.browser.model;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import lombok.Data;

@Data
public class Comment {
    private final String channelName, text, id, parentId;

    public Comment(String channelName, String text, String id, String parentId) {
        this.channelName = channelName;
        this.text = text;
        this.id = id;
        this.parentId = parentId;
    }

    public static Comment fromJSONObject(JSONObject jsonObject) {
        try {
            String parentId = null;
            if (jsonObject.has("parent_id")) {
                parentId = jsonObject.getString("parent_id");
            }

            return new Comment(
                    jsonObject.getString("channel_name"),
                    jsonObject.getString("comment"),
                    jsonObject.getString("comment_id"),
                    parentId
            );
        } catch (JSONException ex) {
            // TODO: Throw exception
            Log.e("Comments", ex.toString());
            return null;
        }
    }
}
