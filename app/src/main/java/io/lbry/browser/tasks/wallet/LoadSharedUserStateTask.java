package io.lbry.browser.tasks.wallet;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.lbry.browser.MainActivity;
import io.lbry.browser.data.DatabaseHelper;
import io.lbry.browser.exceptions.ApiCallException;
import io.lbry.browser.exceptions.LbryUriException;
import io.lbry.browser.model.Tag;
import io.lbry.browser.model.lbryinc.Subscription;
import io.lbry.browser.utils.Lbry;
import io.lbry.browser.utils.LbryUri;

/*
  version: '0.1',
  value: {
    subscriptions?: Array<string>,
    tags?: Array<string>,
    blocked?: Array<string>,
    settings?: any,
    app_welcome_version?: number,
    sharing_3P?: boolean,
  },
 */
public class LoadSharedUserStateTask extends AsyncTask<Void, Void, Boolean> {
    private static final String KEY = "shared";

    private Context context;
    private LoadSharedUserStateHandler handler;
    private Exception error;

    private List<Subscription> subscriptions;
    private List<Tag> followedTags;

    public LoadSharedUserStateTask(Context context, LoadSharedUserStateHandler handler) {
        this.context = context;
        this.handler = handler;
    }

    protected Boolean doInBackground(Void... params) {
        // data to save
        // current subscriptions
        // Get the previous saved state
        try {
            SQLiteDatabase db = null;
            JSONObject result = (JSONObject) Lbry.genericApiCall(Lbry.METHOD_PREFERENCE_GET, Lbry.buildSingleParam("key", KEY));
            if (result != null) {
                if (context instanceof MainActivity) {
                    db = ((MainActivity) context).getDbHelper().getWritableDatabase();
                }

                JSONObject shared = result.getJSONObject("shared");
                if (shared.has("type")
                        && "object".equalsIgnoreCase(shared.getString("type"))
                        && shared.has("value")) {
                    JSONObject value = shared.getJSONObject("value");

                    JSONArray subscriptionUrls =
                            value.has("subscriptions") && !value.isNull("subscriptions") ? value.getJSONArray("subscriptions") : null;
                    JSONArray tags =
                            value.has("tags") && !value.isNull("tags") ? value.getJSONArray("tags") : null;

                    if (subscriptionUrls != null) {
                        subscriptions = new ArrayList<>();
                        for (int i = 0; i < subscriptionUrls.length(); i++) {
                            String url = subscriptionUrls.getString(i);
                            try {
                                LbryUri uri = LbryUri.parse(LbryUri.normalize(url));
                                Subscription subscription = new Subscription();
                                subscription.setChannelName(uri.getChannelName());
                                subscription.setUrl(url);
                                subscriptions.add(subscription);
                                if (db != null) {
                                    DatabaseHelper.createOrUpdateSubscription(subscription, db);
                                }
                            } catch (LbryUriException | SQLiteException | IllegalStateException ex) {
                                // pass
                            }
                        }
                    }

                    if (tags != null) {
                        if (db != null && tags.length() > 0) {
                            DatabaseHelper.setAllTagsUnfollowed(db);
                        }

                        followedTags = new ArrayList<>();
                        for (int i = 0; i < tags.length(); i++) {
                            String tagName = tags.getString(i);
                            Tag tag = new Tag(tagName);
                            tag.setFollowed(true);
                            followedTags.add(tag);

                            try {
                                if (db != null) {
                                    DatabaseHelper.createOrUpdateTag(tag, db);
                                }
                            } catch (SQLiteException | IllegalStateException ex) {
                                // pass
                            }
                        }
                    }
                }
            }

            return true;
        } catch (ApiCallException | JSONException ex) {
            // failed
            error = ex;
        }
        return false;
    }

    protected void onPostExecute(Boolean result) {
        if (handler != null) {
            if (result) {
                handler.onSuccess(subscriptions, followedTags);
            } else {
                handler.onError(error);
            }
        }
    }

    public interface LoadSharedUserStateHandler {
        void onSuccess(List<Subscription> subscriptions, List<Tag> followedTags);
        void onError(Exception error);
    }
}
