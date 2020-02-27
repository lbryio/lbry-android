package io.lbry.browser;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import io.lbry.browser.receivers.NotificationDeletedReceiver;
import io.lbry.lbrysdk.LbrynetService;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Random;

public class DownloadManager {
    private Context context;

    private List<String> activeDownloads = new ArrayList<String>();

    private List<String> completedDownloads = new ArrayList<String>();

    private Map<String, String> downloadIdOutpointsMap = new HashMap<String, String>();

    // maintain a map of uris to writtenBytes, so that we check if it's changed and don't flood RN with update events every 500ms
    private Map<String, Double> writtenDownloadBytes = new HashMap<String, Double>();

    private HashMap<Integer, NotificationCompat.Builder> builders = new HashMap<Integer, NotificationCompat.Builder>();

    private HashMap<String, Integer> downloadIdNotificationIdMap = new HashMap<String, Integer>();

    private HashMap<String, Boolean> stoppedDownloadsMap = new HashMap<String, Boolean>();

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#");

    private static final int MAX_FILENAME_LENGTH = 20;

    private static final int MAX_PROGRESS = 100;

    private static final String GROUP_DOWNLOADS = "io.lbry.browser.GROUP_DOWNLOADS";

    private static final String NOTIFICATION_CHANNEL_ID = "io.lbry.browser.DOWNLOADS_NOTIFICATION_CHANNEL";

    private static boolean channelCreated = false;

    private static NotificationCompat.Builder groupBuilder = null;

    public static final String NOTIFICATION_ID_KEY = "io.lbry.browser.notificationId";

    public static final String ACTION_DOWNLOAD_EVENT = "io.lbry.browser.ACTION_DOWNLOAD_EVENT";

    public static final String ACTION_START = "start";

    public static final String ACTION_COMPLETE = "complete";

    public static final String ACTION_UPDATE = "update";

    public static final int DOWNLOAD_NOTIFICATION_GROUP_ID = 20;

    public static boolean groupCreated = false;

    public DownloadManager(Context context) {
        this.context = context;
    }

    private int generateNotificationId() {
        int id = 0;
        Random random = new Random();
        do {
            id = random.nextInt();
        } while (id < 1000);

        return id;
    }

    private void createNotificationChannel() {
        // Only applies to Android 8.0 Oreo (API Level 26) or higher
        if (!channelCreated && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel(
                NOTIFICATION_CHANNEL_ID, "LBRY Downloads", NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("LBRY file downloads");
            channel.setSound(null, null);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void createNotificationGroup() {
        if (!groupCreated) {
            Intent intent = new Intent(context, NotificationDeletedReceiver.class);
            intent.putExtra(NOTIFICATION_ID_KEY, DOWNLOAD_NOTIFICATION_GROUP_ID);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, DOWNLOAD_NOTIFICATION_GROUP_ID, intent, 0);
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
            notificationManager.notify(DOWNLOAD_NOTIFICATION_GROUP_ID, groupBuilder.build());

            groupCreated = true;
        }
    }

    public static PendingIntent getLaunchPendingIntent(String uri, Context context) {
        Intent launchIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        launchIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent intent = PendingIntent.getActivity(context, 0, launchIntent, 0);
        return intent;
    }

    public void updateWrittenBytesForDownload(String id, double writtenBytes) {
        if (!writtenDownloadBytes.containsKey(id)) {
            writtenDownloadBytes.put(id, writtenBytes);
        }
    }

    public double getWrittenBytesForDownload(String id) {
        if (writtenDownloadBytes.containsKey(id)) {
            return writtenDownloadBytes.get(id);
        }

        return -1;
    }

    public void clearWrittenBytesForDownload(String id) {
        if (writtenDownloadBytes.containsKey(id)) {
            writtenDownloadBytes.remove(id);
        }
    }

    private Intent getDeleteDownloadIntent(String uri) {
        Intent intent = new Intent();
        intent.setAction(LbrynetService.ACTION_DELETE_DOWNLOAD);
        intent.putExtra("uri", uri);
        intent.putExtra("nativeDelete", true);
        return intent;
    }

    public void startDownload(String id, String filename, String outpoint) {
        if (filename == null || filename.trim().length() == 0) {
            return;
        }

        synchronized (this) {
            if (!isDownloadActive(id)) {
                activeDownloads.add(id);
                downloadIdOutpointsMap.put(id, outpoint);
            }

            createNotificationChannel();
            createNotificationGroup();

            PendingIntent stopDownloadIntent = PendingIntent.getBroadcast(context, 0, getDeleteDownloadIntent(id), PendingIntent.FLAG_CANCEL_CURRENT);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);
            // The file URI is used as the unique ID
            builder.setColor(ContextCompat.getColor(context, R.color.lbryGreen))
                   .setContentIntent(getLaunchPendingIntent(id, context))
                   .setContentTitle(String.format("Downloading %s", truncateFilename(filename)))
                   .setGroup(GROUP_DOWNLOADS)
                   .setPriority(NotificationCompat.PRIORITY_LOW)
                   .setProgress(MAX_PROGRESS, 0, false)
                   .setSmallIcon(android.R.drawable.stat_sys_download)
                   .setOngoing(true)
                   .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", stopDownloadIntent);

            int notificationId = getNotificationId(id);
            downloadIdNotificationIdMap.put(id, notificationId);
            builders.put(notificationId, builder);
            notificationManager.notify(notificationId, builder.build());

            if (groupCreated && groupBuilder != null) {
                groupBuilder.setSmallIcon(android.R.drawable.stat_sys_download);
                notificationManager.notify(DOWNLOAD_NOTIFICATION_GROUP_ID, groupBuilder.build());
            }
        }
    }

    public void updateDownload(String id, String filename, double writtenBytes, double totalBytes) {
        if (filename == null || filename.trim().length() == 0) {
            return;
        }

        synchronized (this) {
            createNotificationChannel();
            createNotificationGroup();

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            NotificationCompat.Builder builder = null;
            int notificationId = getNotificationId(id);
            if (builders.containsKey(notificationId)) {
                builder = builders.get(notificationId);
            } else {
                PendingIntent stopDownloadIntent = PendingIntent.getBroadcast(context, 0, getDeleteDownloadIntent(id), PendingIntent.FLAG_CANCEL_CURRENT);
                builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);
                builder.setColor(ContextCompat.getColor(context, R.color.lbryGreen))
                       .setContentTitle(String.format("Downloading %s", truncateFilename(filename)))
                       .setPriority(NotificationCompat.PRIORITY_LOW)
                       .setOngoing(true)
                       .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", stopDownloadIntent);
                builders.put(notificationId, builder);
            }

            double progress = (writtenBytes / totalBytes) * 100;
            builder.setContentIntent(getLaunchPendingIntent(id, context))
                   .setContentText(String.format("%.0f%% (%s / %s)", progress, formatBytes(writtenBytes), formatBytes(totalBytes)))
                   .setGroup(GROUP_DOWNLOADS)
                   .setProgress(MAX_PROGRESS, new Double(progress).intValue(), false)
                   .setSmallIcon(android.R.drawable.stat_sys_download);
            notificationManager.notify(notificationId, builder.build());

            if (progress >= MAX_PROGRESS) {
                builder.setContentTitle(String.format("Downloaded %s", truncateFilename(filename, 30)))
                       .setContentText(String.format("%s", formatBytes(totalBytes)))
                       .setGroup(GROUP_DOWNLOADS)
                       .setProgress(0, 0, false)
                       .setSmallIcon(android.R.drawable.stat_sys_download_done)
                       .setOngoing(false);
                builder.mActions.clear();
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
                    notificationManager.notify(DOWNLOAD_NOTIFICATION_GROUP_ID, groupBuilder.build());
                }

                completeDownload(id, filename, totalBytes);
            }
        }
    }

    public void completeDownload(String id, String filename, double totalBytes) {
        synchronized (this) {
            if (isDownloadActive(id)) {
                activeDownloads.remove(id);
            }
            if (!isDownloadCompleted(id)) {
                completedDownloads.add(id);
            }

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            NotificationCompat.Builder builder = null;
            int notificationId = getNotificationId(id);
            if (builders.containsKey(notificationId)) {
                builder = builders.get(notificationId);
            } else {
                builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);
                builder.setPriority(NotificationCompat.PRIORITY_LOW);
                builders.put(notificationId, builder);
            }

            builder.setContentTitle(String.format("Downloaded %s", truncateFilename(filename, 30)))
                   .setContentText(String.format("%s", formatBytes(totalBytes)))
                   .setGroup(GROUP_DOWNLOADS)
                   .setProgress(0, 0, false)
                   .setSmallIcon(android.R.drawable.stat_sys_download_done)
                   .setOngoing(false);
            builder.mActions.clear();
            notificationManager.notify(notificationId, builder.build());

            // If there are no more downloads and the group exists, set the icon to stop animating
            checkGroupDownloadIcon(notificationManager);
        }
    }

    public void abortDownload(String id) {
        synchronized (this) {
            if (downloadIdNotificationIdMap.containsKey(id)) {
                removeDownloadNotification(id);
            }
            activeDownloads.remove(id);
        }
    }

    public boolean isDownloadActive(String id) {
        return (activeDownloads.contains(id));
    }

    public boolean isDownloadCompleted(String id) {
        return (completedDownloads.contains(id));
    }

    public boolean hasActiveDownloads() {
        return activeDownloads.size() > 0;
    }

    public List<String> getActiveDownloads() {
        return activeDownloads;
    }

    public List<String> getCompletedDownloads() {
        return completedDownloads;
    }

    public String getOutpointForDownload(String uri) {
        if (downloadIdOutpointsMap.containsKey(uri)) {
            return downloadIdOutpointsMap.get(uri);
        }

        return null;
    }

    public void deleteDownloadUri(String uri) {
        synchronized (this) {
            activeDownloads.remove(uri);
            completedDownloads.remove(uri);

            if (downloadIdOutpointsMap.containsKey(uri)) {
                downloadIdOutpointsMap.remove(uri);
            }
            if (downloadIdNotificationIdMap.containsKey(uri)) {
                removeDownloadNotification(uri);
            }
        }
    }

    private void removeDownloadNotification(String id) {
        int notificationId = downloadIdNotificationIdMap.get(id);
        if (downloadIdNotificationIdMap.containsKey(id)) {
            downloadIdNotificationIdMap.remove(id);
        }
        if (builders.containsKey(notificationId)) {
            builders.remove(notificationId);
        }

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        NotificationCompat.Builder builder = builders.get(notificationId);
        notificationManager.cancel(notificationId);

        checkGroupDownloadIcon(notificationManager);
        if (builders.values().size() == 0) {
            notificationManager.cancel(DOWNLOAD_NOTIFICATION_GROUP_ID);
            groupCreated = false;
        }
    }

    private int getNotificationId(String id) {
        if (downloadIdNotificationIdMap.containsKey(id)) {
            return downloadIdNotificationIdMap.get(id);
        }

        int notificationId = generateNotificationId();
        if (MainActivity.downloadNotificationIds != null &&
            !MainActivity.downloadNotificationIds.contains(notificationId)) {
            MainActivity.downloadNotificationIds.add(notificationId);
        }
        downloadIdNotificationIdMap.put(id, notificationId);
        return notificationId;
    }

    private void checkGroupDownloadIcon(NotificationManagerCompat notificationManager) {
        if (groupCreated && groupBuilder != null && downloadIdNotificationIdMap.size() == 0) {
             groupBuilder.setSmallIcon(android.R.drawable.stat_sys_download_done);
             notificationManager.notify(DOWNLOAD_NOTIFICATION_GROUP_ID, groupBuilder.build());
         }
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
