package io.lbry.browser.tasks.lbryinc;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.lbry.browser.MainActivity;
import io.lbry.browser.data.DatabaseHelper;
import io.lbry.browser.exceptions.LbryioRequestException;
import io.lbry.browser.exceptions.LbryioResponseException;
import io.lbry.browser.model.lbryinc.Subscription;
import io.lbry.browser.utils.Helper;
import io.lbry.browser.utils.LbryUri;
import io.lbry.browser.utils.Lbryio;

public class FetchSubscriptionsTask extends AsyncTask<Void, Void, List<Subscription>> {
    private Context context;
    private FetchSubscriptionsHandler handler;
    private ProgressBar progressBar;
    private Exception error;

    public FetchSubscriptionsTask(Context context, ProgressBar progressBar, FetchSubscriptionsHandler handler) {
        this.context = context;
        this.progressBar = progressBar;
        this.handler = handler;
    }
    protected void onPreExecute() {
        Helper.setViewVisibility(progressBar, View.VISIBLE);
    }
    protected List<Subscription> doInBackground(Void... params) {
        List<Subscription> subscriptions = new ArrayList<>();
        SQLiteDatabase db = null;
        try {
            JSONArray array = (JSONArray) Lbryio.parseResponse(Lbryio.call("subscription", "list", context));
            if (context instanceof MainActivity) {
                db = ((MainActivity) context).getDbHelper().getWritableDatabase();
            }
            if (array != null) {
                for (int i = 0; i < array.length(); i++) {
                    JSONObject item = array.getJSONObject(i);
                    String claimId = item.getString("claim_id");
                    String channelName = item.getString("channel_name");

                    LbryUri url = new LbryUri();
                    url.setChannelName(channelName);
                    url.setClaimId(claimId);
                    Subscription subscription = new Subscription(channelName, url.toString());
                    subscriptions.add(subscription);
                    // Persist the subscription locally if it doesn't exist
                    if (db != null) {
                        DatabaseHelper.createOrUpdateSubscription(subscription, db);
                    }
                }
            }
        } catch (ClassCastException | LbryioRequestException | LbryioResponseException | JSONException | IllegalStateException ex) {
            error = ex;
            return null;
        }

        return subscriptions;
    }
    protected void onPostExecute(List<Subscription> subscriptions) {
        Helper.setViewVisibility(progressBar, View.GONE);
        if (handler != null) {
            if (subscriptions != null) {
                handler.onSuccess(subscriptions);
            } else {
                handler.onError(error);
            }
        }
    }

    public interface FetchSubscriptionsHandler {
        void onSuccess(List<Subscription> subscriptions);
        void onError(Exception exception);
    }
}
