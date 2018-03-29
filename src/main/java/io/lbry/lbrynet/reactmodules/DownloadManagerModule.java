package io.lbry.lbrynet.reactmodules;

import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import io.lbry.lbrynet.R;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Random;

/**
 * Created by akinwale on 3/15/18.
 */

public class DownloadManagerModule extends ReactContextBaseJavaModule {
    private Context context;

    private HashMap<Integer, NotificationCompat.Builder> builders = new HashMap<Integer, NotificationCompat.Builder>();

    private HashMap<String, Integer> downloadIdNotificationIdMap = new HashMap<String, Integer>();

    private static final int MAX_PROGRESS = 100;

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.##");

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

    @ReactMethod
    public void startDownload(String id, String fileName) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setContentTitle(String.format("Downloading %s...", fileName))
                .setSmallIcon(R.drawable.ic_file_download_black_24dp)
                .setPriority(NotificationCompat.PRIORITY_LOW);

        builder.setProgress(MAX_PROGRESS, 0, false);

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

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        NotificationCompat.Builder builder = builders.get(notificationId);
        builder.setProgress(MAX_PROGRESS, new Double(progress).intValue(), false);
        builder.setContentText(String.format("%.0f%% (%s / %s)", progress, formatBytes(writtenBytes), formatBytes(totalBytes)));
        builder.setOngoing(true);
        notificationManager.notify(notificationId, builder.build());

        if (progress == MAX_PROGRESS) {
            builder.setContentTitle(String.format("Downloaded %s.", fileName));
            builder.setOngoing(false);
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
        builder.setOngoing(false);
        notificationManager.cancel(notificationId);

        downloadIdNotificationIdMap.remove(id);
        builders.remove(notificationId);
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
