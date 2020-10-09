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
    private boolean replaceLocal;

    public MergeSubscriptionsTask(List<Subscription> base, boolean replaceLocal, Context context, MergeSubscriptionsHandler handler) {
        this.base = base;
        this.replaceLocal = replaceLocal;
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
                if (replaceLocal) {
                    DatabaseHelper.clearSubscriptions(db);
                    for (Subscription sub : base) {
                        DatabaseHelper.createOrUpdateSubscription(sub, db);
                    }
                } else {
                    localSubs = DatabaseHelper.getSubscriptions(db);
                    for (Subscription sub : localSubs) {
                        if (!combined.contains(sub)) {
                            combined.add(sub);
                        }
                    }
                }
            }

            // fetch remote subscriptions
            JSONArray array = (JSONArray) Lbryio.parseResponse(Lbryio.call("subscription", "list", context));
            if (array != null) {
                // check for any remote subs that may have been removed, and unsubscribe from them
                for (int i = 0; i < array.length(); i++) {
                    JSONObject item = array.getJSONObject(i);
                    // TODO: Refactor by creating static Subscription.fromJSON method
                    String claimId = item.getString("claim_id");
                    String channelName = item.getString("channel_name");
                    boolean isNotificationsDisabled  = item.getBoolean("is_notifications_disabled");

                    LbryUri url = new LbryUri();
                    url.setChannelName(channelName);
                    url.setClaimId(claimId);
                    Subscription subscription = new Subscription(channelName, url.toString(), isNotificationsDisabled);
                    remoteSubs.add(subscription);
                }
            }

            List<Subscription> remoteUnsubs = new ArrayList<>();
            List<Subscription> finalRemoteSubs = new ArrayList<>();
            if (remoteSubs.size() > 0) {
                for (int i = 0; i < remoteSubs.size(); i++) {
                    Subscription sub = remoteSubs.get(i);
                    if (!combined.contains(sub)) {
                        Map<String, String> options = new HashMap<>();
                        LbryUri uri = LbryUri.tryParse(sub.getUrl());
                        if (uri != null) {
                            options.put("claim_id", uri.getChannelClaimId());
                            Lbryio.parseResponse(Lbryio.call("subscription", "delete", options, context));
                            remoteUnsubs.add(sub);
                        } else {
                            finalRemoteSubs.add(sub);
                        }
                    }
                }
            }

            if (!replaceLocal) {
                for (int i = 0; i < localSubs.size(); i++) {
                    Subscription local = localSubs.get(i);
                    if (!base.contains(local) && !diff.contains(local)) {
                        diff.add(local);
                    }
                }
                for (int i = 0; i < finalRemoteSubs.size(); i++) {
                    Subscription remote = finalRemoteSubs.get(i);
                    if (!combined.contains(remote)) {
                        combined.add(remote);
                        if (!diff.contains(remote)) {
                            diff.add(remote);
                        }
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
