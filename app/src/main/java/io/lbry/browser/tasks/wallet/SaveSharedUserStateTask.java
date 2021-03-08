package io.lbry.browser.tasks.wallet;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.lbry.browser.exceptions.ApiCallException;
import io.lbry.browser.exceptions.LbryUriException;
import io.lbry.browser.model.lbryinc.Subscription;
import io.lbry.browser.utils.Helper;
import io.lbry.browser.utils.Lbry;
import io.lbry.browser.utils.LbryUri;
import io.lbry.browser.utils.Lbryio;

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
public class SaveSharedUserStateTask extends AsyncTask<Void, Void, Boolean> {
    private static final String KEY = "shared";
    private static final String VERSION = "0.1";
    private final SaveSharedUserStateHandler handler;
    private Exception error;

    public SaveSharedUserStateTask(SaveSharedUserStateHandler handler) {
        this.handler = handler;
    }

    protected Boolean doInBackground(Void... params) {
        // data to save
        // current subscriptions
        List<Subscription> subs = new ArrayList<>(Lbryio.subscriptions);
        List<String> subscriptionUrls = new ArrayList<>();
        try {
            for (Subscription subscription : subs) {
                LbryUri uri = LbryUri.parse(LbryUri.normalize(subscription.getUrl()));
                subscriptionUrls.add(uri.toString());
            }
        } catch (LbryUriException ex) {
            error = ex;
            return false;
        }

        // followed tags
        List<String> followedTags = Helper.getTagsForTagObjects(Lbry.followedTags);

        // Get the previous saved state
        try {
            boolean isExistingValid = false;
            JSONObject sharedObject = null;
            JSONObject result = (JSONObject) Lbry.genericApiCall(Lbry.METHOD_PREFERENCE_GET, Lbry.buildSingleParam("key", KEY));
            if (result != null) {
                JSONObject shared = result.getJSONObject("shared");
                if (shared.has("type")
                        && "object".equalsIgnoreCase(shared.getString("type"))
                        && shared.has("value")) {
                    isExistingValid = true;
                    JSONObject value = shared.getJSONObject("value");
                    value.put("subscriptions", Helper.jsonArrayFromList(subscriptionUrls));
                    value.put("tags", Helper.jsonArrayFromList(followedTags));
                    value.put("following", buildUpdatedNotificationsDisabledStates(subs));
                    sharedObject = shared;
                }
            }

            if (!isExistingValid) {
                // build a  new object
                JSONObject value = new JSONObject();
                value.put("subscriptions", Helper.jsonArrayFromList(subscriptionUrls));
                value.put("tags", Helper.jsonArrayFromList(followedTags));
                value.put("following", buildUpdatedNotificationsDisabledStates(subs));

                sharedObject = new JSONObject();
                sharedObject.put("type", "object");
                sharedObject.put("value", value);
                sharedObject.put("version", VERSION);
            }

            Map<String, Object> options = new HashMap<>();
            options.put("key", KEY);
            options.put("value", sharedObject.toString());
            Lbry.genericApiCall(Lbry.METHOD_PREFERENCE_SET, options);

            return true;
        } catch (ApiCallException | JSONException ex) {
            // failed
            error = ex;
        }
        return false;
    }

    private static JSONArray buildUpdatedNotificationsDisabledStates(List<Subscription> subscriptions) {
        JSONArray states = new JSONArray();
        for (Subscription subscription : subscriptions) {
            if (!Helper.isNullOrEmpty(subscription.getUrl())) {
                try {
                    JSONObject item = new JSONObject();
                    LbryUri uri = LbryUri.parse(LbryUri.normalize(subscription.getUrl()));
                    item.put("uri", uri.toString());
                    item.put("notificationsDisabled", subscription.isNotificationsDisabled());
                    states.put(item);
                } catch (JSONException | LbryUriException ex) {
                    // pass

                }
            }
        }

        return states;
    }

    protected void onPostExecute(Boolean result) {
        if (handler != null) {
            if (result) {
                handler.onSuccess();
            } else {
                handler.onError(error);
            }
        }
    }

    public interface SaveSharedUserStateHandler {
        void onSuccess();
        void onError(Exception error);
    }
}
