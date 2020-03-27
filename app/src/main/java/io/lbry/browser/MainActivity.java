package io.lbry.browser;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.Manifest;
import android.net.Uri;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import android.telephony.SmsMessage;
import android.widget.Toast;

import com.azendoo.reactnativesnackbar.SnackbarPackage;
import com.brentvatne.react.ReactVideoPackage;
import com.dylanvann.fastimage.FastImageViewPackage;
import com.facebook.react.common.LifecycleState;
import com.facebook.react.modules.core.DefaultHardwareBackBtnHandler;
import com.facebook.react.ReactRootView;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.modules.core.PermissionAwareActivity;
import com.facebook.react.modules.core.PermissionListener;
import com.facebook.react.shell.MainReactPackage;
import com.facebook.soloader.SoLoader;
import com.reactnativecommunity.asyncstorage.AsyncStoragePackage;
import com.reactnativecommunity.webview.RNCWebViewPackage;
import com.rnfs.RNFSPackage;
import com.swmansion.gesturehandler.react.RNGestureHandlerEnabledRootView;
import com.swmansion.gesturehandler.react.RNGestureHandlerPackage;
import com.swmansion.reanimated.ReanimatedPackage;
import com.RNFetchBlob.RNFetchBlobPackage;

import io.lbry.browser.reactmodules.UtilityModule;
import io.lbry.browser.reactpackages.LbryReactPackage;
import io.lbry.browser.reactmodules.BackgroundMediaModule;
import io.lbry.lbrysdk.LbrynetService;
import io.lbry.lbrysdk.ServiceHelper;
import io.lbry.lbrysdk.Utils;

import java.io.File;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;
import org.reactnative.camera.RNCameraPackage;

public class MainActivity extends FragmentActivity implements DefaultHardwareBackBtnHandler, PermissionAwareActivity {

    private static Activity currentActivity = null;
    private static final int OVERLAY_PERMISSION_REQ_CODE = 101;
    private static final int STORAGE_PERMISSION_REQ_CODE = 201;
    private static final int PHONE_STATE_PERMISSION_REQ_CODE = 202;
    private static final int RECEIVE_SMS_PERMISSION_REQ_CODE = 203;
    public static final int DOCUMENT_PICKER_RESULT_CODE = 301;
    public static final String SHARED_PREFERENCES_NAME = "LBRY";
    public static final String SALT_KEY = "salt";
    public static final String DEVICE_ID_KEY = "deviceId";
    public static final String SOURCE_NOTIFICATION_ID_KEY = "sourceNotificationId";
    public static final String SETTING_KEEP_DAEMON_RUNNING = "keepDaemonRunning";
    public static List<Integer> downloadNotificationIds = new ArrayList<Integer>();

    private BroadcastReceiver notificationsReceiver;
    private BroadcastReceiver smsReceiver;
    private BroadcastReceiver serviceActionsReceiver;
    private BroadcastReceiver downloadEventReceiver;
    private ReactRootView mReactRootView;
    private ReactInstanceManager mReactInstanceManager;

    /**
     * Flag which indicates whether or not the service is running. Will be updated in the
     * onResume method.
     */
    private boolean serviceRunning;
    private CheckSdkReadyTask checkSdkReadyTask;
    private boolean receivedStopService;
    private PermissionListener permissionListener;
    public static boolean lbrySdkReady;

    protected String getMainComponentName() {
        return "LBRYApp";
    }
    
    public static LaunchTiming CurrentLaunchTiming;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        CurrentLaunchTiming = new LaunchTiming(new Date());
        super.onCreate(savedInstanceState);
        currentActivity = this;

        SoLoader.init(this, false);

        // Register the stop service receiver (so that we close the activity if the user requests the service to stop)
        registerServiceActionsReceiver();

        // Register SMS receiver for handling verification texts
        registerSmsReceiver();

        // Register the receiver to emit download events
        registerDownloadEventReceiver();

        // Start the sdk service if it is not started
        // Check the dht setting
        SharedPreferences sp = getSharedPreferences(MainActivity.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        LbrynetService.setDHTEnabled(sp.getBoolean(UtilityModule.DHT_ENABLED, false));
        serviceRunning = isServiceRunning(this, LbrynetService.class);
        if (!serviceRunning) {
            CurrentLaunchTiming.setColdStart(true);
            ServiceHelper.start(this, "", LbrynetService.class, "lbrynetservice");
        }
        checkSdkReady();

        checkNotificationOpenIntent(getIntent());

        mReactRootView = new RNGestureHandlerEnabledRootView(this);
        mReactInstanceManager = ReactInstanceManager.builder()
                .setApplication(getApplication())
                .setCurrentActivity(this)
                .setBundleAssetName("index.android.bundle")
                .setJSMainModulePath("index")
                .addPackage(new MainReactPackage())
                .addPackage(new AsyncStoragePackage())
                .addPackage(new FastImageViewPackage())
                .addPackage(new RNCWebViewPackage())
                .addPackage(new ReactVideoPackage())
                .addPackage(new ReanimatedPackage())
                .addPackage(new RNCameraPackage())
                .addPackage(new RNFetchBlobPackage())
                .addPackage(new RNFSPackage())
                .addPackage(new RNGestureHandlerPackage())
                .addPackage(new SnackbarPackage())
                .addPackage(new LbryReactPackage())
                .setUseDeveloperSupport(BuildConfig.DEBUG)
                .setInitialLifecycleState(LifecycleState.RESUMED)
                .build();
        mReactRootView.startReactApplication(mReactInstanceManager, "LBRYApp", null);

        registerNotificationsReceiver();

        setContentView(mReactRootView);
    }

    private void checkSdkReady() {
        if (!lbrySdkReady) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (checkSdkReadyTask != null && checkSdkReadyTask.getStatus() != AsyncTask.Status.FINISHED) {
                        // task already running
                        return;
                    }
                    checkSdkReadyTask = new CheckSdkReadyTask();
                    checkSdkReadyTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            }, 1000);
        }
    }

    private void checkNotificationOpenIntent(Intent intent) {
        if (intent != null) {
            String notificationName = intent.getStringExtra("notification_name");
            if (notificationName != null) {
                logNotificationOpen(notificationName);
            }
        }
    }

    private void logNotificationOpen(String name) {

    }

    private void registerDownloadEventReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DownloadManager.ACTION_DOWNLOAD_EVENT);
        downloadEventReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String downloadAction = intent.getStringExtra("action");
                String uri = intent.getStringExtra("uri");
                String outpoint = intent.getStringExtra("outpoint");
                String fileInfoJson = intent.getStringExtra("file_info");


                if (uri == null || outpoint == null || (fileInfoJson == null && !"abort".equals(downloadAction))) {
                    return;
                }

                String eventName = null;
                WritableMap params = Arguments.createMap();
                params.putString("uri", uri);
                params.putString("outpoint", outpoint);

                ReactContext reactContext = mReactInstanceManager.getCurrentReactContext();
                if ("abort".equals(downloadAction)) {
                    eventName = "onDownloadAborted";
                    if (reactContext != null) {
                        reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(eventName, params);
                    }
                    return;
                }

                try {
                    JSONObject json = new JSONObject(fileInfoJson);
                    WritableMap fileInfo = JSONObjectToMap(json);
                    params.putMap("fileInfo", fileInfo);

                    if (DownloadManager.ACTION_UPDATE.equals(downloadAction)) {
                        double progress = intent.getDoubleExtra("progress", 0);
                        params.putDouble("progress", progress);
                        eventName = "onDownloadUpdated";
                    } else {
                        eventName = (DownloadManager.ACTION_START.equals(downloadAction)) ? "onDownloadStarted" : "onDownloadCompleted";
                    }

                    if (reactContext != null) {
                        reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(eventName, params);
                    }
                } catch (JSONException ex) {
                    // pass
                }
            }
        };
        registerReceiver(downloadEventReceiver, intentFilter);
    }

    private void registerServiceActionsReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LbrynetService.LBRY_SDK_SERVICE_STARTED);
        intentFilter.addAction(LbrynetService.ACTION_STOP_SERVICE);
        serviceActionsReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (LbrynetService.ACTION_STOP_SERVICE.equals(action)) {
                    MainActivity.this.receivedStopService = true;
                    MainActivity.this.finish();
                } else if (LbrynetService.LBRY_SDK_SERVICE_STARTED.equals(action)) {
                    // Rebuild the service notification
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Notification svcNotification = buildServiceNotification();
                            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                            notificationManager.notify(1, svcNotification);
                        }
                    }, 1000);
                }
            }
        };
        registerReceiver(serviceActionsReceiver, intentFilter);
    }

    private Notification buildServiceNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, LbrynetService.NOTIFICATION_CHANNEL_ID);
        Intent contextIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, contextIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent stopIntent = new Intent(LbrynetService.ACTION_STOP_SERVICE);
        PendingIntent stopPendingIntent = PendingIntent.getBroadcast(this, 0, stopIntent, 0);

        String serviceDescription = "The LBRY service is running in the background.";
        Notification notification = builder.setColor(ContextCompat.getColor(this, R.color.lbryGreen))
                .setContentIntent(pendingIntent)
                .setContentText(serviceDescription)
                .setGroup(LbrynetService.GROUP_SERVICE)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_lbry)
                .setOngoing(true)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", stopPendingIntent)
                .build();

        return notification;
    }

    private void registerNotificationsReceiver() {
        // Background media receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(BackgroundMediaModule.ACTION_PLAY);
        filter.addAction(BackgroundMediaModule.ACTION_PAUSE);
        notificationsReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                ReactContext reactContext = mReactInstanceManager.getCurrentReactContext();
                if (reactContext != null) {
                    if (BackgroundMediaModule.ACTION_PLAY.equals(action)) {
                        reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                            .emit("onBackgroundPlayPressed", null);
                    }
                    if (BackgroundMediaModule.ACTION_PAUSE.equals(action)) {
                        reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                            .emit("onBackgroundPausePressed", null);
                    }
                }
            }
        };
        registerReceiver(notificationsReceiver, filter);
    }

    public void registerSmsReceiver() {
        if (!hasPermission(Manifest.permission.RECEIVE_SMS, this)) {
            // don't create the receiver if we don't have the read sms permission
            return;
        }

        IntentFilter smsFilter = new IntentFilter();
        smsFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
        smsReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // Get the message
                Bundle bundle = intent.getExtras();
                if (bundle != null) {
                    Object[] pdus = (Object[]) bundle.get("pdus");
                    if (pdus != null && pdus.length > 0) {
                        SmsMessage sms  = SmsMessage.createFromPdu((byte[]) pdus[0]);
                        String text = sms.getMessageBody();
                        if (text == null || text.trim().length() == 0) {
                            return;
                        }

                        // Retrieve verification code from the text message if it contains
                        // the strings "lbry", "verification code" and the colon (following the expected format)
                        text = text.toLowerCase();
                        if (text.indexOf("lbry") > -1 &&  text.indexOf("verification code") > -1 && text.indexOf(":") > -1) {
                            String code = text.substring(text.lastIndexOf(":") + 1).trim();
                            ReactContext reactContext = mReactInstanceManager.getCurrentReactContext();
                            if (reactContext != null) {
                                WritableMap params = Arguments.createMap();
                                params.putString("code", code);
                                reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                                    .emit("onVerificationCodeReceived", params);
                            }
                        }
                    }
                }
            }
        };
        registerReceiver(smsReceiver, smsFilter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == OVERLAY_PERMISSION_REQ_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this)) {
                    // SYSTEM_ALERT_WINDOW permission not granted...
                }
            }
        }

        if (requestCode == DOCUMENT_PICKER_RESULT_CODE) {
            ReactContext reactContext = mReactInstanceManager.getCurrentReactContext();
            if (reactContext != null) {
                if (resultCode == RESULT_OK) {
                    Uri fileUri = data.getData();
                    String filePath = getRealPathFromURI_API19(this, fileUri);
                    WritableMap params = Arguments.createMap();
                    params.putString("path", filePath);
                    reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                        .emit("onDocumentPickerFilePicked", params);
                } else if (resultCode == RESULT_CANCELED) {
                    // user canceled or request failed
                    reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                        .emit("onDocumentPickerCanceled", null);
                }
            }
        }
    }

    public static Activity getActivity() {
        Activity activity = new Activity();
        activity = currentActivity;
        return activity;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        ReactContext reactContext = mReactInstanceManager.getCurrentReactContext();
        switch (requestCode) {
            case STORAGE_PERMISSION_REQ_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (BuildConfig.DEBUG && Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(this)) {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:" + getPackageName()));
                        startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE);
                    }
                    if (reactContext != null) {
                        reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                            .emit("onStoragePermissionGranted", null);
                    }
                } else {
                    // Permission not granted
                    /*Toast.makeText(this,
                        "LBRY requires access to your device storage to be able to download files and media." +
                        " Please enable the storage permission and restart the app.", Toast.LENGTH_LONG).show();*/
                    if (reactContext != null) {
                        reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                            .emit("onStoragePermissionRefused", null);
                    }
                }
                break;

            case PHONE_STATE_PERMISSION_REQ_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted. Emit an onPhoneStatePermissionGranted event
                    if (reactContext != null) {
                        reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                            .emit("onPhoneStatePermissionGranted", null);
                    }
                } else {
                    // Permission not granted. Simply show a message.
                    Toast.makeText(this,
                        "No permission granted to read your device state. Rewards cannot be claimed.", Toast.LENGTH_LONG).show();
                }
                break;

            case RECEIVE_SMS_PERMISSION_REQ_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted. Emit an onPhoneStatePermissionGranted event
                    if (reactContext != null) {
                        reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                            .emit("onReceiveSmsPermissionGranted", null);
                    }

                    // register the receiver
                    if (smsReceiver == null) {
                        registerSmsReceiver();
                    }
                } else {
                    // Permission not granted. Simply show a message.
                    Toast.makeText(this,
                        "No permission granted to receive your SMS messages. You may have to enter the verification code manually.",
                        Toast.LENGTH_LONG).show();
                }
                break;
        }

        if (permissionListener != null) {
            permissionListener.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void invokeDefaultOnBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mReactInstanceManager != null) {
            mReactInstanceManager.onHostPause(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences sp = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        LbrynetService.setDHTEnabled(sp.getBoolean(UtilityModule.DHT_ENABLED, false));

        serviceRunning = isServiceRunning(this, LbrynetService.class);
        if (!serviceRunning) {
            ServiceHelper.start(this, "", LbrynetService.class, "lbrynetservice");
        }
        checkSdkReady();

        if (mReactInstanceManager != null) {
            mReactInstanceManager.onHostResume(this, this);
        }
    }

    @Override
    protected void onDestroy() {
        // check service running setting and end it here
        SharedPreferences sp = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        boolean shouldKeepDaemonRunning = sp.getBoolean(SETTING_KEEP_DAEMON_RUNNING, true);
        if (!shouldKeepDaemonRunning) {
            serviceRunning = isServiceRunning(this, LbrynetService.class);
            if (serviceRunning) {
               ServiceHelper.stop(this, LbrynetService.class);
            }
        }

        if (notificationsReceiver != null) {
            unregisterReceiver(notificationsReceiver);
            notificationsReceiver = null;
        }

        if (smsReceiver != null) {
            unregisterReceiver(smsReceiver);
            smsReceiver = null;
        }

        if (downloadEventReceiver != null) {
            unregisterReceiver(downloadEventReceiver);
            downloadEventReceiver = null;
        }

        if (serviceActionsReceiver != null) {
            unregisterReceiver(serviceActionsReceiver);
            serviceActionsReceiver = null;
        }

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.cancel(BackgroundMediaModule.NOTIFICATION_ID);
        notificationManager.cancel(DownloadManager.DOWNLOAD_NOTIFICATION_GROUP_ID);
        if (downloadNotificationIds != null) {
            for (int i = 0; i < downloadNotificationIds.size(); i++) {
                notificationManager.cancel(downloadNotificationIds.get(i));
            }
        }
        if (receivedStopService || !isServiceRunning(this, LbrynetService.class)) {
            notificationManager.cancelAll();
        }
        super.onDestroy();

        if (mReactInstanceManager != null) {
            mReactInstanceManager.onHostDestroy(this);
        }
    }

    @Override
    public void onBackPressed() {
        if (mReactInstanceManager != null) {
            mReactInstanceManager.onBackPressed();
        } else {
            super.onBackPressed();
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void requestPermissions(String[] permissions, int requestCode, PermissionListener listener) {
        permissionListener = listener;
        ActivityCompat.requestPermissions(this, permissions, requestCode);
    }

    @Override
    public void onNewIntent(Intent intent) {
        if (mReactInstanceManager != null) {
            mReactInstanceManager.onNewIntent(intent);
        }

        if (intent != null) {
            int sourceNotificationId = intent.getIntExtra(SOURCE_NOTIFICATION_ID_KEY, -1);
            if (sourceNotificationId > -1) {
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
                notificationManager.cancel(sourceNotificationId);
            }

            checkNotificationOpenIntent(intent);
        }

        super.onNewIntent(intent);
    }

    private static void checkPermission(String permission, int requestCode, String rationale, Context context, boolean forceRequest) {
        if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (!forceRequest && ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, permission)) {
                Toast.makeText(context, rationale, Toast.LENGTH_LONG).show();
            } else {
                ActivityCompat.requestPermissions((Activity) context, new String[] { permission }, requestCode);
            }
        }
    }

    private static void checkPermission(String permission, int requestCode, String rationale, Context context) {
        checkPermission(permission, requestCode, rationale, context, false);
    }

    public static boolean hasPermission(String permission, Context context) {
        return (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED);
    }

    public static void checkPhoneStatePermission(Context context) {
        // Request read phone state permission
        checkPermission(Manifest.permission.READ_PHONE_STATE,
                        PHONE_STATE_PERMISSION_REQ_CODE,
                        "LBRY requires optional access to be able to identify your device for rewards. " +
                        "You cannot claim rewards without this permission.",
                        context,
                        true);
    }

    public static void checkReceiveSmsPermission(Context context) {
        // Request read phone state permission
        checkPermission(Manifest.permission.RECEIVE_SMS,
                        RECEIVE_SMS_PERMISSION_REQ_CODE,
                        "LBRY requires access to be able to read a verification text message for rewards.",
                        context,
                        true);
    }

    public static void checkStoragePermission(Context context) {
        // Request read phone state permission
        checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        STORAGE_PERMISSION_REQ_CODE,
                        "LBRY requires access to your device storage to be able to download files and media.",
                        context,
                        true);
    }

    public static boolean isServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo serviceInfo : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(serviceInfo.service.getClassName())) {
                return true;
            }
        }

        return false;
    }

    private static WritableMap JSONObjectToMap(JSONObject jsonObject) throws JSONException {
        WritableMap map = Arguments.createMap();
        Iterator<String> keys = jsonObject.keys();
        while(keys.hasNext()) {
            String key = keys.next();
            Object value = jsonObject.get(key);
            if (value instanceof JSONArray) {
                map.putArray(key, JSONArrayToList((JSONArray) value));
            } else if (value instanceof JSONObject) {
                map.putMap(key, JSONObjectToMap((JSONObject) value));
            } else if (value instanceof  Boolean) {
                map.putBoolean(key, (Boolean) value);
            } else if (value instanceof  Integer) {
                map.putInt(key, (Integer) value);
            } else if (value instanceof  Double) {
                map.putDouble(key, (Double) value);
            } else if (value instanceof String)  {
                map.putString(key, (String) value);
            } else {
                map.putString(key, value.toString());
            }
        }

        return map;
    }

    private static WritableArray JSONArrayToList(JSONArray jsonArray) throws JSONException {
        WritableArray array = Arguments.createArray();
        for(int i = 0; i < jsonArray.length(); i++) {
            Object value = jsonArray.get(i);
            if (value instanceof JSONArray) {
                array.pushArray(JSONArrayToList((JSONArray) value));
            } else if (value instanceof JSONObject) {
                array.pushMap(JSONObjectToMap((JSONObject) value));
            } else if (value instanceof  Boolean) {
                array.pushBoolean((Boolean) value);
            } else if (value instanceof  Integer) {
                array.pushInt((Integer) value);
            } else if (value instanceof  Double) {
                array.pushDouble((Double) value);
            } else if (value instanceof String)  {
                array.pushString((String) value);
            } else {
                array.pushString(value.toString());
            }
        }

        return array;
    }

    /**
     * https://gist.github.com/HBiSoft/15899990b8cd0723c3a894c1636550a8
     */
    @SuppressLint("NewApi")
    public static String getRealPathFromURI_API19(final Context context, final Uri uri) {
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                // This is for checking Main Memory
                if ("primary".equalsIgnoreCase(type)) {
                    if (split.length > 1) {
                        return Environment.getExternalStorageDirectory() + "/" + split[1];
                    } else {
                        return Environment.getExternalStorageDirectory() + "/";
                    }
                    // This is for checking SD Card
                } else {
                    return "storage" + "/" + docId.replace(":", "/");
                }

            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {
                String fileName = getFilePath(context, uri);
                if (fileName != null) {
                    return Environment.getExternalStorageDirectory().toString() + "/Download/" + fileName;
                }

                String id = DocumentsContract.getDocumentId(uri);
                if (id.startsWith("raw:")) {
                    id = id.replaceFirst("raw:", "");
                    File file = new File(id);
                    if (file.exists())
                        return id;
                }

                final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    public static String getFilePath(Context context, Uri uri) {
        Cursor cursor = null;
        final String[] projection = { MediaStore.MediaColumns.DISPLAY_NAME };

        try {
            cursor = context.getContentResolver().query(uri, projection, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    private class CheckSdkReadyTask extends AsyncTask<Void, Void, Boolean> {

        public Boolean doInBackground(Void... params) {
            boolean sdkReady = false;
            try {
                String response = Utils.sdkCall("status");
                if (response != null) {
                    JSONObject result = new JSONObject(response);
                    JSONObject status = result.getJSONObject("result");

                    // send status response for splash page updates
                    WritableMap sdkStatus = JSONObjectToMap(status);
                    ReactContext reactContext = mReactInstanceManager.getCurrentReactContext();
                    if (reactContext != null) {
                        WritableMap evtParams = Arguments.createMap();
                        evtParams.putMap("status", sdkStatus);
                        reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("onSdkStatusResponse", evtParams);
                    }

                    JSONObject startupStatus = status.getJSONObject("startup_status");
                    sdkReady = startupStatus.has("stream_manager") && startupStatus.has("wallet") &&
                            startupStatus.getBoolean("stream_manager") && startupStatus.getBoolean("wallet") &&
                            (status.getJSONObject("wallet").getLong("blocks_behind") <= 0);
                }
            } catch (ConnectException ex) {
                // pass
            } catch (JSONException ex) {
                // pass
            }

            return sdkReady;
        }
        protected void onPostExecute(Boolean sdkReady) {
            lbrySdkReady = sdkReady;
            ReactContext reactContext = mReactInstanceManager.getCurrentReactContext();
            if (sdkReady && reactContext != null) {
                reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("onSdkReady", null);
            }

            if (!sdkReady) {
                checkSdkReady();
            }
        }
    }
    
    public static class LaunchTiming {
        private Date start;
        private boolean coldStart;
        
        public LaunchTiming(Date start) {
            this.start = start;
        }
        
        public Date getStart() {
            return start;
        }
        public void setStart(Date start) {
            this.start = start;
        }
        public boolean isColdStart() {
            return coldStart;
        }
        public void setColdStart(boolean coldStart) {
            this.coldStart = coldStart;
        }
    }
}
