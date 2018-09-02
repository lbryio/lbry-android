package io.lbry.browser.reactmodules;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import io.lbry.browser.LbrynetService;
import io.lbry.browser.MainActivity;
import io.lbry.browser.R;

public class BackgroundMediaModule extends ReactContextBaseJavaModule {

    public static final int NOTIFICATION_ID = 30;

    public static final String ACTION_PLAY = "io.lbry.browser.ACTION_MEDIA_PLAY";

    public static final String ACTION_PAUSE = "io.lbry.browser.ACTION_MEDIA_PAUSE";

    public static final String ACTION_STOP = "io.lbry.browser.ACTION_MEDIA_STOP";

    private Context context;

    public BackgroundMediaModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.context = reactContext;
    }

    @Override
    public String getName() {
        return "BackgroundMedia";
    }

    @ReactMethod
    public void showPlaybackNotification(String title, String publisher, String uri, boolean paused) {
        Intent contextIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, contextIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent playIntent = new Intent();
        playIntent.setAction(ACTION_PLAY);
        PendingIntent playPendingIntent = PendingIntent.getBroadcast(context, 0, playIntent, 0);

        Intent pauseIntent = new Intent();
        pauseIntent.setAction(ACTION_PAUSE);
        PendingIntent pausePendingIntent = PendingIntent.getBroadcast(context, 0, pauseIntent, 0);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, LbrynetService.NOTIFICATION_CHANNEL_ID);
        builder.setColor(ContextCompat.getColor(context, R.color.lbrygreen))
               .setContentIntent(pendingIntent)
               .setContentTitle(title)
               .setContentText(publisher)
               .setGroup(LbrynetService.GROUP_SERVICE)
               .setOngoing(!paused)
               .setSmallIcon(paused ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play)
               .setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle()
                         .setShowActionsInCompactView(0))
               .addAction(paused ? android.R.drawable.ic_media_play : android.R.drawable.ic_media_pause,
                          paused ? "Play" : "Pause",
                          paused ? playPendingIntent : pausePendingIntent)
               .build();

        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    @ReactMethod
    public void hidePlaybackNotification() {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancel(NOTIFICATION_ID);
    }
}
