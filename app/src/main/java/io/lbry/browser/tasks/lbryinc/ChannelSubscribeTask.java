package io.lbry.browser.tasks.lbryinc;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;

import java.util.HashMap;
import java.util.Map;

import io.lbry.browser.MainActivity;
import io.lbry.browser.data.DatabaseHelper;
import io.lbry.browser.exceptions.LbryioRequestException;
import io.lbry.browser.exceptions.LbryioResponseException;
import io.lbry.browser.model.lbryinc.Subscription;
import io.lbry.browser.utils.Helper;
import io.lbry.browser.utils.Lbryio;

public class ChannelSubscribeTask extends AsyncTask<Void, Void, Boolean> {
    private Context context;
    private String channelClaimId;
    private Subscription subscription;
    private ChannelSubscribeHandler handler;
    private Exception error;
    private boolean isUnsubscribing;

    public ChannelSubscribeTask(Context context, String channelClaimId, Subscription subscription, boolean isUnsubscribing, ChannelSubscribeHandler handler) {
        this.context = context;
        this.channelClaimId = channelClaimId;
        this.subscription = subscription;
        this.handler = handler;
        this.isUnsubscribing = isUnsubscribing;
    }
    protected Boolean doInBackground(Void... params) {
        SQLiteDatabase db = null;
        try {
            // Save to (or delete from) local store
            if (context instanceof MainActivity) {
                db = ((MainActivity) context).getDbHelper().getWritableDatabase();
            }
            if (db != null) {
                if (!isUnsubscribing) {
                    DatabaseHelper.createOrUpdateSubscription(subscription, db);
                } else {
                    DatabaseHelper.deleteSubscription(subscription, db);
                }
            }

            // Save with Lbryio
            Map<String, String> options = new HashMap<>();
            options.put("claim_id", channelClaimId);
            if (!isUnsubscribing) {
                options.put("channel_name", subscription.getChannelName());
            }

            String action = isUnsubscribing ? "delete" : "new";
            Lbryio.call("subscription", action, options, context);

            if (!isUnsubscribing) {
                Lbryio.addSubscription(subscription);
            } else {
                Lbryio.removeSubscription(subscription);
            }
        } catch (LbryioRequestException | LbryioResponseException | SQLiteException ex) {
            error = ex;
            return false;
        }

        return true;
    }
    protected void onPostExecute(Boolean success) {
        if (handler != null) {
            if (success) {
                handler.onSuccess();
            } else {
                handler.onError(error);
            }
        }
    }

    public interface ChannelSubscribeHandler {
        void onSuccess();
        void onError(Exception exception);
    }
}
