package io.lbry.browser;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import android.util.Log;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import io.lbry.browser.data.DatabaseHelper;
import io.lbry.browser.model.lbryinc.LbryNotification;
import io.lbry.browser.utils.LbryAnalytics;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class LbrynetMessagingService extends FirebaseMessagingService {
    public static final String ACTION_NOTIFICATION_RECEIVED = "io.lbry.browser.Broadcast.NotificationReceived";

    private static final String TAG = "LbrynetMessagingService";
    private static final String NOTIFICATION_CHANNEL_ID = "io.lbry.browser.LBRY_ENGAGEMENT_CHANNEL";
    private static final String TYPE_SUBSCRIPTION = "subscription";
    private static final String TYPE_REWARD = "reward";
    private static final String TYPE_INTERESTS = "interests";
    private static final String TYPE_CREATOR = "creator";
    private FirebaseAnalytics firebaseAnalytics;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (firebaseAnalytics == null) {
            firebaseAnalytics = FirebaseAnalytics.getInstance(this);
        }

        Map<String, String> payload = remoteMessage.getData();
        if (payload != null) {
            String type = payload.get("type");
            String url = payload.get("target");
            String title = payload.get("title");
            String body = payload.get("body");
            String name = payload.get("name"); // notification name

            if (type != null && getEnabledTypes().indexOf(type) > -1 && body != null && body.trim().length() > 0) {
                // only log the receive event for valid notifications received
                if (firebaseAnalytics != null) {
                    Bundle bundle = new Bundle();
                    bundle.putString("name", name);
                    firebaseAnalytics.logEvent(LbryAnalytics.EVENT_LBRY_NOTIFICATION_RECEIVE, bundle);
                }

                sendNotification(title, body, type, url, name);
            }

            // persist the notification data
            try {
                DatabaseHelper helper = DatabaseHelper.getInstance();
                SQLiteDatabase db = helper.getWritableDatabase();
                LbryNotification lnotification = new LbryNotification();
                lnotification.setTitle(title);
                lnotification.setDescription(body);
                lnotification.setTargetUrl(url);
                lnotification.setTimestamp(new Date());
                DatabaseHelper.createOrUpdateNotification(lnotification, db);

                // send a broadcast
                Intent intent = new Intent(ACTION_NOTIFICATION_RECEIVED);
                intent.putExtra("title", title);
                intent.putExtra("body", body);
                intent.putExtra("url", url);
                intent.putExtra("timestamp", lnotification.getTimestamp().getTime());
                sendBroadcast(intent);
            } catch (Exception ex) {
                // don't fail if any error occurs while saving a notification
                Log.e(TAG, "could not save notification", ex);
            }
        }
    }

    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(token);
    }

    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {
        // TODO: Implement this method to send token to your app server.
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    private void sendNotification(String title, String messageBody, String type, String url, String name) {
        //Intent intent = new Intent(this, MainActivity.class);
        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        android.util.Log.d("#HELP", "Title=" + title + "; Body=" + messageBody + "; Type=" + type +"; url=" + url + "; name=" + name);

        if (url == null) {
            if (TYPE_REWARD.equals(type)) {
                url = "lbry://?rewards";
            } else {
                // default to home page
                url = "lbry://?discover";
            }
        }

        Intent launchIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        launchIntent.putExtra("notification_name", name);
        launchIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                        .setColor(ContextCompat.getColor(this, R.color.lbryGreen))
                        .setSmallIcon(R.drawable.ic_lbry)
                        .setContentTitle(title)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID, "LBRY Engagement", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(3, notificationBuilder.build());
    }

    public List<String> getEnabledTypes() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        List<String> enabledTypes = new ArrayList<String>();

        if (sp.getBoolean(MainActivity.PREFERENCE_KEY_NOTIFICATION_SUBSCRIPTIONS, true)) {
            enabledTypes.add(TYPE_SUBSCRIPTION);
        }
        if (sp.getBoolean(MainActivity.PREFERENCE_KEY_NOTIFICATION_REWARDS, true)) {
            enabledTypes.add(TYPE_REWARD);
        }
        if (sp.getBoolean(MainActivity.PREFERENCE_KEY_NOTIFICATION_CONTENT_INTERESTS, true)) {
            enabledTypes.add(TYPE_INTERESTS);
        }
        if (sp.getBoolean(MainActivity.PREFERENCE_KEY_NOTIFICATION_CREATOR, true)) {
            enabledTypes.add(TYPE_CREATOR);
        }

        return enabledTypes;
    }
}