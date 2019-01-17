package io.lbry.browser.reactmodules;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.Manifest;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.WindowManager;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import java.io.File;
import java.util.Random;

import io.lbry.browser.MainActivity;
import io.lbry.browser.R;
import io.lbry.browser.Utils;
import io.lbry.browser.reactmodules.DownloadManagerModule;

public class UtilityModule extends ReactContextBaseJavaModule {
    private static final String FILE_PROVIDER = "io.lbry.browser.fileprovider";

    private static final String NOTIFICATION_CHANNEL_ID = "io.lbry.browser.SUBSCRIPTIONS_NOTIFICATION_CHANNEL";

    private Context context;

    public UtilityModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.context = reactContext;
    }

    @Override
    public String getName() {
        return "UtilityModule";
    }

    @ReactMethod
    public void keepAwakeOn() {
        final Activity activity = getCurrentActivity();

        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                  activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                }
            });
        }
    }

    @ReactMethod
    public void keepAwakeOff() {
        final Activity activity = getCurrentActivity();

        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                }
            });
        }
    }

    @ReactMethod
    public void hideNavigationBar() {
        final Activity activity = MainActivity.getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    View decorView = activity.getWindow().getDecorView();
                    decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                                                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                                                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                                                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                                                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
                }
            });

        }
    }

    @ReactMethod
    public void showNavigationBar() {
        final Activity activity = MainActivity.getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    View decorView = activity.getWindow().getDecorView();
                    decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                                                    View.SYSTEM_UI_FLAG_VISIBLE);
                }
            });
        }
    }

    @ReactMethod
    public void getDeviceId(boolean requestPermission, final Promise promise) {
        if (isEmulator()) {
            promise.reject("Rewards cannot be claimed from an emulator nor virtual device.");
            return;
        }

        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String id = null;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                id = telephonyManager.getImei(); // GSM
                if (id == null) {
                    id = telephonyManager.getMeid(); // CDMA
                }
            } else {
                id = telephonyManager.getDeviceId();
            }
        } catch (SecurityException ex) {
            // Maybe the permission was not granted? Try to acquire permission
            /*if (requestPermission) {
                requestPhoneStatePermission();
            }*/
        } catch (Exception ex) {
            // id could not be obtained. Display a warning that rewards cannot be claimed.
            promise.reject(ex.getMessage());
        }

        if (id == null || id.trim().length() == 0) {
            promise.reject("Rewards cannot be claimed because your device could not be identified.");
            return;
        }

        promise.resolve(id);
    }

    @ReactMethod
    public void canReceiveSms(final Promise promise) {
        promise.resolve(MainActivity.hasPermission(Manifest.permission.RECEIVE_SMS, MainActivity.getActivity()));
    }

    @ReactMethod
    public void requestReceiveSmsPermission() {
        MainActivity activity = (MainActivity) MainActivity.getActivity();
        if (activity != null) {
            // Request for the RECEIVE_SMS permission
            MainActivity.checkReceiveSmsPermission(activity);
        }
    }

    @ReactMethod
    public void shareLogFile(Callback errorCallback) {
        String logFileName = "lbrynet.log";
        File logFile = new File(String.format("%s/%s", Utils.getAppInternalStorageDir(context), "lbrynet"), logFileName);
        if (!logFile.exists()) {
            errorCallback.invoke("The lbrynet.log file could not be found.");
            return;
        }

        try {
            Uri fileUri = FileProvider.getUriForFile(context, FILE_PROVIDER, logFile);
            if (fileUri != null) {
                Intent shareIntent = new Intent();
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
                context.startActivity(Intent.createChooser(shareIntent, "Send LBRY log"));
            }
        } catch (IllegalArgumentException e) {
            errorCallback.invoke("The lbrynet.log file cannot be shared due to permission restrictions.");
        }
    }

    @ReactMethod
    public void showNotificationForContent(String uri, String title, String publisher, String thumbnailUri) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                NOTIFICATION_CHANNEL_ID, "LBRY Subscriptions", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("LBRY subscription notifications");
            notificationManager.createNotificationChannel(channel);
        }

        int notificationId = 0;
        Random random = new Random();
        do {
            notificationId = random.nextInt();
        } while (notificationId < 100);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);
        builder.setColor(ContextCompat.getColor(context, R.color.lbrygreen))
               .setContentIntent(DownloadManagerModule.getLaunchPendingIntent(uri, context))
               .setContentTitle(title)
               .setContentText(publisher)
               .setSmallIcon(R.drawable.ic_lbry)
               /*.setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle()
                         .setShowActionsInCompactView(0))
               .addAction(paused ? android.R.drawable.ic_media_play : android.R.drawable.ic_media_pause,
                          paused ? "Play" : "Pause",
                          paused ? playPendingIntent : pausePendingIntent)*/
               .build();

        notificationManager.notify(notificationId, builder.build());
    }

    private static boolean isEmulator() {
        String buildModel = Build.MODEL.toLowerCase();
        return (// Check FINGERPRINT
                Build.FINGERPRINT.startsWith("generic") ||
                Build.FINGERPRINT.startsWith("unknown") ||
                Build.FINGERPRINT.contains("test-keys") ||

                // Check MODEL
                buildModel.contains("google_sdk") ||
                buildModel.contains("emulator") ||
                buildModel.contains("android sdk built for x86") ||

                // Check MANUFACTURER
                Build.MANUFACTURER.contains("Genymotion") ||
                "unknown".equals(Build.MANUFACTURER) ||

                // Check HARDWARE
                Build.HARDWARE.contains("goldfish") ||
                Build.HARDWARE.contains("vbox86") ||

                // Check PRODUCT
                "google_sdk".equals(Build.PRODUCT) ||
                "sdk_google_phone_x86".equals(Build.PRODUCT) ||
                "sdk".equals(Build.PRODUCT) ||
                "sdk_x86".equals(Build.PRODUCT) ||
                "vbox86p".equals(Build.PRODUCT) ||

                // Check BRAND and DEVICE
                (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
               );
    }
}
