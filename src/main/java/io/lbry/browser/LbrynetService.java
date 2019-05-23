package io.lbry.browser;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;
import org.kivy.android.PythonService;
import org.renpy.android.AssetExtract;
import org.renpy.android.ResourceManager;

/**
 * This service class is based on the auto-generated P4A service class
 * which changes the service start type to START_STICKY and lets it run
 * properly as a background service.
 *
 * @author akinwale
 * @version 0.1
 */
public class LbrynetService extends PythonService {

    public static final int SERVICE_NOTIFICATION_GROUP_ID = 5;

    public static final String ACTION_STOP_SERVICE = "io.lbry.browser.ACTION_STOP_SERVICE";

    public static final String ACTION_CHECK_DOWNLOADS = "io.lbry.browser.ACTION_CHECK_DOWNLOADS";

    public static final String GROUP_SERVICE = "io.lbry.browser.GROUP_SERVICE";

    public static final String NOTIFICATION_CHANNEL_ID = "io.lbry.browser.DAEMON_NOTIFICATION_CHANNEL";

    public static String TAG = "LbrynetService";

    public static LbrynetService serviceInstance;

    private static final String SDK_URL = "http://127.0.0.1:5279";

    private static final int SDK_POLL_INTERVAL = 500; // 500 milliseconds

    private BroadcastReceiver stopServiceReceiver;

    private BroadcastReceiver downloadReceiver;

    private DownloadManager downloadManager;

    private ScheduledExecutorService taskExecutor;

    private ScheduledFuture taskExecutorHandle = null;

    @Override
    public boolean canDisplayNotification() {
        return true;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Register the stop service receiver
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_STOP_SERVICE);
        stopServiceReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                LbrynetService.this.stopSelf();
            }
        };
        registerReceiver(stopServiceReceiver, intentFilter);

        IntentFilter downloadFilter = new IntentFilter();
        downloadFilter.addAction(ACTION_CHECK_DOWNLOADS);
        downloadReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                LbrynetService.this.checkDownloads();
            }
        };
        registerReceiver(downloadReceiver, downloadFilter);
    }

    @Override
    protected void doStartForeground(Bundle extras) {
        String serviceTitle = extras.getString("serviceTitle");
        String serviceDescription = "The LBRY service is running in the background.";

        Context context = getApplicationContext();
        downloadManager = new DownloadManager(context);
        NotificationManager notificationManager =
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                NOTIFICATION_CHANNEL_ID, "LBRY Browser", NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("LBRY service notification channel");
            channel.setShowBadge(false);
            notificationManager.createNotificationChannel(channel);
        }

        Intent contextIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, contextIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent stopIntent = new Intent(ACTION_STOP_SERVICE);
        PendingIntent stopPendingIntent = PendingIntent.getBroadcast(context, 0, stopIntent, 0);

        // Create the notification group
        NotificationCompat.Builder groupBuilder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);
        groupBuilder.setContentTitle("LBRY Browser")
                    .setColor(ContextCompat.getColor(context, R.color.lbrygreen))
                    .setSmallIcon(R.drawable.ic_lbry)
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setGroup(GROUP_SERVICE)
                    .setGroupSummary(true);
        notificationManager.notify(SERVICE_NOTIFICATION_GROUP_ID, groupBuilder.build());

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);
        Notification notification = builder.setColor(ContextCompat.getColor(context, R.color.lbrygreen))
                                           .setContentIntent(pendingIntent)
                                           .setContentTitle(serviceTitle)
                                           .setContentText(serviceDescription)
                                           .setGroup(GROUP_SERVICE)
                                           .setWhen(System.currentTimeMillis())
                                           .setSmallIcon(R.drawable.ic_lbry)
                                           .setOngoing(true)
                                           .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", stopPendingIntent)
                                           .build();
        startForeground(1, notification);
    }

    private void checkDownloads() {
        if (taskExecutor == null) {
            taskExecutor = Executors.newScheduledThreadPool(1);
            taskExecutorHandle = taskExecutor.scheduleAtFixedRate(new Runnable() {
                public void run() {
                    LbrynetService.this.pollFileList();
                }
            }, 0, SDK_POLL_INTERVAL, TimeUnit.MILLISECONDS);
        }
    }

    private void pollFileList() {
        BufferedReader reader = null;
        DataOutputStream dos = null;
        HttpURLConnection conn = null;

        try {
            JSONObject request = new JSONObject();
            request.put("method", "file_list");

            URL url = new URL(SDK_URL);
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-type", "application/json");

            dos = new DataOutputStream(conn.getOutputStream());
            dos.writeBytes(request.toString());
            dos.flush();
            dos.close();

            if (conn.getResponseCode() == 200) {
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
                StringBuilder sb = new StringBuilder();
                String input;
                while ((input = reader.readLine()) != null) {
                    sb.append(input);
                }

                JSONObject response = new JSONObject(sb.toString());
                if (!response.has("error")) {
                    handlePollFileResponse(response);
                }
            } else {
                reader = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "utf-8"));
                StringBuilder sb = new StringBuilder();
                String error;
                while ((error = reader.readLine()) != null) {
                    sb.append(error);
                }
                android.util.Log.d(TAG, sb.toString());
            }
        } catch (IOException ex) {
            android.util.Log.e(TAG, ex.getMessage(), ex);
            // ignore and continue
        } catch (Exception ex) {
            android.util.Log.e(TAG, ex.getMessage(), ex);
            // ignore
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
                if (conn != null) {
                    conn.disconnect();
                }
            } catch (IOException ex) {
                // pass
            }
        }
    }

    private void handlePollFileResponse(JSONObject response) {
        Context context = getApplicationContext();
        if (response.has("result")) {
            JSONArray fileItems = response.optJSONArray("result");
            if (fileItems != null) {
                try {
                    List<String> itemUris = new ArrayList<String>();
                    for (int i = 0; i < fileItems.length(); i++) {
                        JSONObject item = fileItems.getJSONObject(i);
                        String downloadPath = item.getString("download_path");
                        if (downloadPath == null || downloadPath.trim().length() == 0) {
                            continue;
                        }

                        String claimId = item.getString("claim_id");
                        String claimName = item.getString("claim_name");
                        String uri = String.format("lbry://%s#%s", claimName, claimId);
                        String outpoint = item.getString("outpoint");
                        boolean completed = item.getBoolean("completed");
                        double writtenBytes = item.optDouble("written_bytes", -1);
                        double totalBytes = item.optDouble("total_bytes", -1);

                        if (downloadManager.isDownloadActive(uri) && (writtenBytes == -1 || totalBytes == -1)) {
                            // possibly deleted, abort the download
                            downloadManager.abortDownload(uri);
                            continue;
                        }

                        if (!itemUris.contains(uri)) {
                            itemUris.add(uri);
                        }

                        Intent intent = new Intent();
                        intent.setAction(DownloadManager.ACTION_DOWNLOAD_EVENT);
                        intent.putExtra("uri", uri);
                        intent.putExtra("outpoint", outpoint);
                        intent.putExtra("file_info", item.toString());

                        File file = new File(downloadPath);
                        if (downloadManager.isDownloadActive(uri)) {
                            if (writtenBytes >= totalBytes || completed) {
                                // completed download
                                intent.putExtra("action", "complete");
                                downloadManager.completeDownload(uri, file.getName(), totalBytes);
                            } else {
                                intent.putExtra("action", "update");
                                intent.putExtra("progress", (writtenBytes / totalBytes) * 100);
                                downloadManager.updateDownload(uri, file.getName(), writtenBytes, totalBytes);
                            }

                            if (context != null) {
                                context.sendBroadcast(intent);
                            }
                        } else {
                            if (!completed && writtenBytes < totalBytes) {
                                intent.putExtra("action", "start");
                                downloadManager.startDownload(uri, file.getName());


                                if (context != null) {
                                    context.sendBroadcast(intent);
                                }
                            }
                        }
                    }

                    // check download manager uris and clear downloads that may have been cancelled / deleted
                    List<String> activeUris = downloadManager.getActiveDownloads();
                    for (int i = 0; i < activeUris.size(); i++) {
                        String activeUri = activeUris.get(i);
                        if (!itemUris.contains(activeUri)) {
                            downloadManager.abortDownload(activeUri);
                        }
                    }
                } catch (JSONException ex) {
                    // pass
                    android.util.Log.e(TAG, ex.getMessage(), ex);
                }
            }
        }

        if (!downloadManager.hasActiveDownloads()) {
            // stop polling
            if (taskExecutorHandle != null) {
                taskExecutorHandle.cancel(true);
                taskExecutorHandle = null;
            }
            if (taskExecutor != null) {
                taskExecutor.shutdownNow();
                taskExecutor = null;
            }
        }
    }

    @Override
    public int startType() {
        return START_STICKY;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Assign service instance
        serviceInstance = this;

        // Extract files
        File app_root_file = new File(getAppRoot());
        unpackData("private", app_root_file);

        if (intent == null) {
            intent = ServiceHelper.buildIntent(
                getApplicationContext(), "", LbrynetService.class, "lbrynetservice");
        }

        checkDownloads();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        if (downloadReceiver != null) {
            unregisterReceiver(downloadReceiver);
            downloadReceiver = null;
        }

        if (stopServiceReceiver != null) {
            unregisterReceiver(stopServiceReceiver);
            stopServiceReceiver = null;
        }

        Context context = getApplicationContext();
        NotificationManager notificationManager =
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();

        super.onDestroy();
        serviceInstance = null;
    }

    public String getAppRoot() {
        String app_root = getApplicationContext().getFilesDir().getAbsolutePath() + "/app";
        return app_root;
    }

    public void recursiveDelete(File f) {
        if (f.isDirectory()) {
            for (File r : f.listFiles()) {
                recursiveDelete(r);
            }
        }
        f.delete();
    }

    public void unpackData(final String resource, File target) {
        Log.v(TAG, "UNPACKING!!! " + resource + " " + target.getName());

        // The version of data in memory and on disk.
        ResourceManager resourceManager = new ResourceManager(getApplicationContext());
        String data_version = resourceManager.getString(resource + "_version");
        String disk_version = null;

        Log.v(TAG, "Data version is " + data_version);

        // If no version, no unpacking is necessary.
        if (data_version == null) {
            return;
        }

        // Check the current disk version, if any.
        String filesDir = target.getAbsolutePath();
        String disk_version_fn = filesDir + "/" + resource + ".version";

        try {
            byte buf[] = new byte[64];
            InputStream is = new FileInputStream(disk_version_fn);
            int len = is.read(buf);
            disk_version = new String(buf, 0, len);
            is.close();
        } catch (Exception e) {
            disk_version = "";
        }

        // If the disk data is out of date, extract it and write the
        // version file.
        // if (! data_version.equals(disk_version)) {
        if (! data_version.equals(disk_version)) {
            Log.v(TAG, "Extracting " + resource + " assets.");

            recursiveDelete(target);
            target.mkdirs();

            AssetExtract ae = new AssetExtract(getApplicationContext());
            if (!ae.extractTar(resource + ".mp3", target.getAbsolutePath())) {
                //toastError("Could not extract " + resource + " data.");
                Log.e(TAG, "Could not extract " + resource + " data.");
            }

            try {
                // Write .nomedia.
                new File(target, ".nomedia").createNewFile();

                // Write version file.
                FileOutputStream os = new FileOutputStream(disk_version_fn);
                os.write(data_version.getBytes());
                os.close();
            } catch (Exception e) {
                Log.w("python", e);
            }
        }
    }
}
