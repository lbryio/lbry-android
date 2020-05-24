package io.lbry.browser.tasks;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.lbry.browser.MainActivity;
import io.lbry.browser.data.DatabaseHelper;
import io.lbry.browser.exceptions.LbryUriException;
import io.lbry.browser.exceptions.LbryioRequestException;
import io.lbry.browser.exceptions.LbryioResponseException;
import io.lbry.browser.model.lbryinc.Subscription;
import io.lbry.browser.utils.Helper;
import io.lbry.browser.utils.LbryUri;
import io.lbry.browser.utils.Lbryio;
import okhttp3.Response;

// background task to create a diff of local and remote subscriptions and try to merge
public class MergeSubscriptionsTask extends AsyncTask<Void, Void, List<Subscription>> {
    private static final String TAG = "MergeSubscriptionsTask";
    private Context context;
    private List<Subscription> base;
    private List<Subscription> diff;
    private MergeSubscriptionsHandler handler;
    private Exception error;

    public MergeSubscriptionsTask(List<Subscription> base, Context context, MergeSubscriptionsHandler handler) {
        this.base = base;
        this.context = context;
        this.handler = handler;
    }

    protected List<Subscription> doInBackground(Void... params) {
        List<Subscription> combined = new ArrayList<>(base);
        List<Subscription> localSubs = new ArrayList<>();
        List<Subscription> remoteSubs = new ArrayList<>();
        diff = new ArrayList<>();
        SQLiteDatabase db = null;
        try {
            // fetch local subscriptions
            if (context instanceof MainActivity) {
                db = ((MainActivity) context).getDbHelper().getWritableDatabase();
            }
            if (db != null) {
                localSubs = DatabaseHelper.getSubscriptions(db);
                for (Subscription sub : localSubs) {
                    if (!combined.contains(sub)) {
                        combined.add(sub);
                    }
                }
            }

            // fetch remote subscriptions
            JSONArray array = (JSONArray) Lbryio.parseResponse(Lbryio.call("subscription", "list", context));
            if (array != null) {
                for (int i = 0; i < array.length(); i++) {
                    JSONObject item = array.getJSONObject(i);
                    String claimId = item.getString("claim_id");
                    String channelName = item.getString("channel_name");

                    LbryUri url = new LbryUri();
                    url.setChannelName(channelName);
                    url.setClaimId(claimId);
                    Subscription subscription = new Subscription(channelName, url.toString());
                    remoteSubs.add(subscription);
                }
            }

            for (int i = 0; i < combined.size(); i++) {
                Subscription local = combined.get(i);
                if (!remoteSubs.contains(local)) {
                    // add to remote subscriptions
                    try {
                        LbryUri uri = LbryUri.parse(local.getUrl());
                        Map<String, String> options = new HashMap<>();
                        String channelClaimId = uri.getChannelClaimId();
                        String channelName = Helper.normalizeChannelName(local.getChannelName());
                        if (!Helper.isNullOrEmpty(channelClaimId) && !Helper.isNullOrEmpty(channelName)) {
                            options.put("claim_id", channelClaimId);
                            options.put("channel_name", channelName);
                            Lbryio.parseResponse(Lbryio.call("subscription", "new", options, context));
                        }
                    } catch (LbryUriException | LbryioRequestException | LbryioResponseException ex) {
                        // pass
                        Log.e(TAG, String.format("subscription/new failed: %s", ex.getMessage()), ex);
                    }
                }
            }

            for (int i = 0; i < localSubs.size(); i++) {
                Subscription local = localSubs.get(i);
                if (!base.contains(local) && !diff.contains(local)) {
                    diff.add(local);
                }
            }
            for (int i = 0; i < remoteSubs.size(); i++) {
                Subscription remote = remoteSubs.get(i);
                if (!combined.contains(remote)) {
                    combined.add(remote);
                    if (!diff.contains(remote)) {
                        diff.add(remote);
                    }
                }
            }
        } catch (ClassCastException | LbryioRequestException | LbryioResponseException | JSONException | IllegalStateException | SQLiteException ex) {
            error = ex;
            return null;
        }

        return combined;
    }
    protected void onPostExecute(List<Subscription> subscriptions) {
        if (handler != null) {
            if (subscriptions != null) {
                handler.onSuccess(subscriptions, diff);
            } else {
                handler.onError(error);
            }
        }
    }

    public interface MergeSubscriptionsHandler {
        void onSuccess(List<Subscription> subscriptions, List<Subscription> diff);
        void onError(Exception error);
    }
}
