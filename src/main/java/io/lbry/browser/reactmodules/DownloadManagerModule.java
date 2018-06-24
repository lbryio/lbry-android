package io.lbry.browser.reactmodules;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import io.lbry.browser.MainActivity;
import io.lbry.browser.R;
import io.lbry.browser.receivers.NotificationDeletedReceiver;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Random;

public class DownloadManagerModule extends ReactContextBaseJavaModule {

    private Context context;

    private HashMap<Integer, NotificationCompat.Builder> builders = new HashMap<Integer, NotificationCompat.Builder>();

    private HashMap<String, Integer> downloadIdNotificationIdMap = new HashMap<String, Integer>();

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#");

    private static final int MAX_PROGRESS = 100;

    private static final String GROUP_DOWNLOADS = "io.lbry.browser.GROUP_DOWNLOADS";

    private static final String NOTIFICATION_CHANNEL_ID = "io.lbry.browser.DOWNLOADS_NOTIFICATION_CHANNEL";

    private static boolean channelCreated = false;

    public static final String NOTIFICATION_ID_KEY = "io.lbry.browser.notificationId";

    public static final int GROUP_ID = 0;

    public static boolean groupCreated = false;

    public DownloadManagerModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.context = reactContext;
    }

    private int generateNotificationId() {
        return new Random().nextInt();
    }

    @Override
    public String getName() {
        return "LbryDownloadManager";
    }

    private void createNotificationChannel() {
        // Only applies to Android 8.0 Oreo (API Level 26) or higher
        if (!channelCreated && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel(
                NOTIFICATION_CHANNEL_ID, "LBRY Downloads", NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("LBRY file downloads");
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void createNotificationGroup() {
        if (!groupCreated) {
            Intent intent = new Intent(context, NotificationDeletedReceiver.class);
            intent.putExtra(NOTIFICATION_ID_KEY, GROUP_ID);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, GROUP_ID, intent, 0);
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);
            builder.setContentTitle("Active downloads")
                   .setContentText("Active downloads")
                   .setSmallIcon(R.drawable.ic_file_download_black_24dp)
                   .setPriority(NotificationCompat.PRIORITY_LOW)
                   .setGroup(GROUP_DOWNLOADS)
                   .setGroupSummary(true)
                   .setDeleteIntent(pendingIntent);
            notificationManager.notify(GROUP_ID, builder.build());

            groupCreated = true;
        }
    }

    private PendingIntent getLaunchPendingIntent(String uri) {
        Intent launchIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        launchIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent intent = PendingIntent.getActivity(context, 0, launchIntent, 0);
        return intent;
    }

    @ReactMethod
    public void startDownload(String id, String fileName) {
        createNotificationChannel();
        createNotificationGroup();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);
        // The file URI is used as the unique ID
        builder.setContentIntent(getLaunchPendingIntent(id))
               .setContentTitle(String.format("Downloading %s...", fileName))
               .setGroup(GROUP_DOWNLOADS)
               .setPriority(NotificationCompat.PRIORITY_LOW)
               .setProgress(MAX_PROGRESS, 0, false)
               .setSmallIcon(R.drawable.ic_file_download_black_24dp);

        int notificationId = generateNotificationId();
        downloadIdNotificationIdMap.put(id, notificationId);

        builders.put(notificationId, builder);
        notificationManager.notify(notificationId, builder.build());
    }

    @ReactMethod
    public void updateDownload(String id, String fileName, double progress, double writtenBytes, double totalBytes) {
        if (!downloadIdNotificationIdMap.containsKey(id)) {
            return;
        }

        int notificationId = downloadIdNotificationIdMap.get(id);
        if (!builders.containsKey(notificationId)) {
            return;
        }

        createNotificationChannel();
        createNotificationGroup();
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        NotificationCompat.Builder builder = builders.get(notificationId);
        builder.setContentIntent(getLaunchPendingIntent(id))
               .setContentText(String.format("%.0f%% (%s / %s)", progress, formatBytes(writtenBytes), formatBytes(totalBytes)))
               .setGroup(GROUP_DOWNLOADS)
               .setProgress(MAX_PROGRESS, new Double(progress).intValue(), false);
        notificationManager.notify(notificationId, builder.build());

        if (progress == MAX_PROGRESS) {
            builder.setContentTitle(String.format("Downloaded %s", fileName))
                   .setContentText(String.format("%s", formatBytes(totalBytes)))
                   .setProgress(0, 0, false);
            notificationManager.notify(notificationId, builder.build());

            downloadIdNotificationIdMap.remove(id);
            builders.remove(notificationId);
        }
    }

    @ReactMethod
    public void stopDownload(String id, String filename) {
        if (!downloadIdNotificationIdMap.containsKey(id)) {
            return;
        }

        int notificationId = downloadIdNotificationIdMap.get(id);
        if (!builders.containsKey(notificationId)) {
            return;
        }

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        NotificationCompat.Builder builder = builders.get(notificationId);
        notificationManager.cancel(notificationId);

        downloadIdNotificationIdMap.remove(id);
        builders.remove(notificationId);

        if (builders.values().size() == 0) {
            notificationManager.cancel(GROUP_ID);
            groupCreated = false;
        }
    }

    private String formatBytes(double bytes)
    {
        if (bytes < 1048576) { // < 1MB
            return String.format("%s KB", DECIMAL_FORMAT.format(bytes / 1024.0));
        }

        if (bytes < 1073741824) { // < 1GB
            return String.format("%s MB", DECIMAL_FORMAT.format(bytes / (1024.0 * 1024.0)));
        }

        return String.format("%s GB", DECIMAL_FORMAT.format(bytes / (1024.0 * 1024.0 * 1024.0)));
    }
}
