package io.lbry.browser.reactmodules;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

    private HashMap<String, Boolean> stoppedDownloadsMap = new HashMap<String, Boolean>();

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#");

    private static final int MAX_FILENAME_LENGTH = 20;

    private static final int MAX_PROGRESS = 100;

    private static final String GROUP_DOWNLOADS = "io.lbry.browser.GROUP_DOWNLOADS";

    private static final String NOTIFICATION_CHANNEL_ID = "io.lbry.browser.DOWNLOADS_NOTIFICATION_CHANNEL";

    private static boolean channelCreated = false;

    public static final String NOTIFICATION_ID_KEY = "io.lbry.browser.notificationId";

    public static final int GROUP_ID = 20;

    private static NotificationCompat.Builder groupBuilder = null;

    public static boolean groupCreated = false;

    public DownloadManagerModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.context = reactContext;
    }

    private int generateNotificationId() {
        int id = 0;
        Random random = new Random();
        do {
            id = random.nextInt();
        } while (id < 1000);

        return id;
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
            groupBuilder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);
            groupBuilder.setContentTitle("Active LBRY downloads")
                        // contentText will be displayed if there are no notifications in the group
                        .setContentText("There are no active LBRY downloads.")
                        .setSmallIcon(android.R.drawable.stat_sys_download)
                        .setPriority(NotificationCompat.PRIORITY_LOW)
                        .setGroup(GROUP_DOWNLOADS)
                        .setGroupSummary(true)
                        .setDeleteIntent(pendingIntent);
            notificationManager.notify(GROUP_ID, groupBuilder.build());

            groupCreated = true;
        }
    }

    public static PendingIntent getLaunchPendingIntent(String uri, Context context) {
        Intent launchIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        launchIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent intent = PendingIntent.getActivity(context, 0, launchIntent, 0);
        return intent;
    }

    @ReactMethod
    public void startDownload(String id, String filename) {
        if (filename == null || filename.trim().length() == 0) {
            return;
        }

        createNotificationChannel();
        createNotificationGroup();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);
        // The file URI is used as the unique ID
        builder.setContentIntent(getLaunchPendingIntent(id, context))
               .setContentTitle(String.format("Downloading %s", truncateFilename(filename)))
               .setGroup(GROUP_DOWNLOADS)
               .setPriority(NotificationCompat.PRIORITY_LOW)
               .setProgress(MAX_PROGRESS, 0, false)
               .setSmallIcon(android.R.drawable.stat_sys_download);

        int notificationId = getNotificationId(id);
        downloadIdNotificationIdMap.put(id, notificationId);
        builders.put(notificationId, builder);
        notificationManager.notify(notificationId, builder.build());

        if (groupCreated && groupBuilder != null) {
            groupBuilder.setSmallIcon(android.R.drawable.stat_sys_download);
            notificationManager.notify(GROUP_ID, groupBuilder.build());
        }
    }

    @ReactMethod
    public void updateDownload(String id, String filename, double progress, double writtenBytes, double totalBytes) {
        if (filename == null || filename.trim().length() == 0) {
            return;
        }

        int notificationId = getNotificationId(id);
        if (notificationId == -1) {
            return;
        }

        if (stoppedDownloadsMap.containsKey(id) && stoppedDownloadsMap.get(id)) {
            // if this happens, the download was canceled, so remove the notification
            // TODO: Figure out why updateDownload is called in the React Native code after stopDownload
            removeDownloadNotification(id);
            stoppedDownloadsMap.remove(id);
            return;
        }

        createNotificationChannel();
        createNotificationGroup();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        NotificationCompat.Builder builder = null;
        if (builders.containsKey(notificationId)) {
            builder = builders.get(notificationId);
        } else {
            builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);
            builder.setContentTitle(String.format("Downloading %s", truncateFilename(filename)))
                   .setPriority(NotificationCompat.PRIORITY_LOW);
            builders.put(notificationId, builder);
        }

        builder.setContentIntent(getLaunchPendingIntent(id, context))
               .setContentText(String.format("%.0f%% (%s / %s)", progress, formatBytes(writtenBytes), formatBytes(totalBytes)))
               .setGroup(GROUP_DOWNLOADS)
               .setProgress(MAX_PROGRESS, new Double(progress).intValue(), false)
               .setSmallIcon(android.R.drawable.stat_sys_download);
        notificationManager.notify(notificationId, builder.build());

        if (progress == MAX_PROGRESS) {
            builder.setContentTitle(String.format("Downloaded %s", truncateFilename(filename, 30)))
                   .setContentText(String.format("%s", formatBytes(totalBytes)))
                   .setGroup(GROUP_DOWNLOADS)
                   .setProgress(0, 0, false)
                   .setSmallIcon(android.R.drawable.stat_sys_download_done);
            notificationManager.notify(notificationId, builder.build());

            if (downloadIdNotificationIdMap.containsKey(id)) {
                downloadIdNotificationIdMap.remove(id);
            }
            if (builders.containsKey(notificationId)) {
                builders.remove(notificationId);
            }

            // If there are no more downloads and the group exists, set the icon to stop animating
            if (groupCreated && groupBuilder != null && downloadIdNotificationIdMap.size() == 0) {
                groupBuilder.setSmallIcon(android.R.drawable.stat_sys_download_done);
                notificationManager.notify(GROUP_ID, groupBuilder.build());
            }

            String spKey = String.format("dl__%s", id);
            SharedPreferences sp = context.getSharedPreferences(MainActivity.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.remove(spKey);
            editor.apply();
        }
    }

    @ReactMethod
    public void stopDownload(String id, String filename) {
        android.util.Log.d("ReactNativeJS", "Stop download for id=" + id + "; filename=" + filename);
        stoppedDownloadsMap.put(id, true);
        removeDownloadNotification(id);
    }

    private void removeDownloadNotification(String id) {
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

    private int getNotificationId(String id) {
        String spKey = String.format("dl__%s", id);
        SharedPreferences sp = context.getSharedPreferences(MainActivity.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        int notificationId = sp.getInt(spKey, -1);
        if (notificationId == -1) {
            notificationId = generateNotificationId();
            SharedPreferences.Editor editor = sp.edit();
            editor.putInt(spKey, notificationId);
            editor.apply();
        }

        if (MainActivity.downloadNotificationIds != null &&
            !MainActivity.downloadNotificationIds.contains(notificationId)) {
            MainActivity.downloadNotificationIds.add(notificationId);
        }
        downloadIdNotificationIdMap.put(id, notificationId);
        return notificationId;
    }

    private static String formatBytes(double bytes)
    {
        if (bytes < 1048576) { // < 1MB
            return String.format("%s KB", DECIMAL_FORMAT.format(bytes / 1024.0));
        }

        if (bytes < 1073741824) { // < 1GB
            return String.format("%s MB", DECIMAL_FORMAT.format(bytes / (1024.0 * 1024.0)));
        }

        return String.format("%s GB", DECIMAL_FORMAT.format(bytes / (1024.0 * 1024.0 * 1024.0)));
    }

    private static String truncateFilename(String filename, int alternateMaxLength) {
        int maxLength = alternateMaxLength > 0 ? alternateMaxLength : MAX_FILENAME_LENGTH;
        if (filename.length() < maxLength) {
            return filename;
        }

        // Get the extension
        int dotIndex = filename.lastIndexOf(".");
        if (dotIndex > -1) {
            String extension = filename.substring(dotIndex);
            return String.format("%s...%s", filename.substring(0, maxLength - extension.length() - 4), extension);
        }

        return String.format("%s...", filename.substring(0, maxLength - 3));
    }

    private static String truncateFilename(String filename) {
        return truncateFilename(filename, 0);
    }
}
