package io.lbry.browser;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
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
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.widget.Toast;

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
import com.facebook.react.ReactRootView;
import com.reactnativecommunity.asyncstorage.AsyncStoragePackage;
import com.rnfs.RNFSPackage;
import com.swmansion.gesturehandler.react.RNGestureHandlerEnabledRootView;
import com.swmansion.gesturehandler.react.RNGestureHandlerPackage;
import com.swmansion.reanimated.ReanimatedPackage;
import com.RNFetchBlob.RNFetchBlobPackage;

import io.lbry.browser.reactpackages.LbryReactPackage;
import io.lbry.browser.reactmodules.BackgroundMediaModule;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;
import org.reactnative.camera.RNCameraPackage;

public class MainActivity extends Activity implements DefaultHardwareBackBtnHandler, PermissionAwareActivity {

    private static Activity currentActivity = null;

    private static final int OVERLAY_PERMISSION_REQ_CODE = 101;

    private static final int STORAGE_PERMISSION_REQ_CODE = 201;

    private static final int PHONE_STATE_PERMISSION_REQ_CODE = 202;

    private static final int RECEIVE_SMS_PERMISSION_REQ_CODE = 203;
    
    public static final int DOCUMENT_PICKER_RESULT_CODE = 301;

    private BroadcastReceiver notificationsReceiver;

    private BroadcastReceiver smsReceiver;

    private BroadcastReceiver stopServiceReceiver;

    private BroadcastReceiver downloadEventReceiver;

    private ReactRootView mReactRootView;

    private ReactInstanceManager mReactInstanceManager;

    public static final String SHARED_PREFERENCES_NAME = "LBRY";

    public static final String SALT_KEY = "salt";

    public static final String DEVICE_ID_KEY = "deviceId";

    public static final String SOURCE_NOTIFICATION_ID_KEY = "sourceNotificationId";

    public static final String SETTING_KEEP_DAEMON_RUNNING = "keepDaemonRunning";

    public static List<Integer> downloadNotificationIds = new ArrayList<Integer>();

    /**
     * Flag which indicates whether or not the service is running. Will be updated in the
     * onResume method.
     */
    private boolean serviceRunning;

    private boolean receivedStopService;

    private PermissionListener permissionListener;

    protected String getMainComponentName() {
        return "LBRYApp";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Request external storage permission on Android version >= 6
            checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            STORAGE_PERMISSION_REQ_CODE,
                            "LBRY requires access to your device storage to be able to download files and media.",
                            this);
        }

        super.onCreate(savedInstanceState);
        currentActivity = this;

        // Register the stop service receiver (so that we close the activity if the user requests the service to stop)
        registerStopReceiver();

        // Register SMS receiver for handling verification texts
        registerSmsReceiver();

        // Register the receiver to emit download events
        registerDownloadEventReceiver();

        // Start the daemon service if it is not started
        serviceRunning = isServiceRunning(LbrynetService.class);
        if (!serviceRunning) {
            ServiceHelper.start(this, "", LbrynetService.class, "lbrynetservice");
        }

        mReactRootView = new RNGestureHandlerEnabledRootView(this);
        mReactInstanceManager = ReactInstanceManager.builder()
                .setApplication(getApplication())
                .setBundleAssetName("index.android.bundle")
                .setJSMainModulePath("index")
                .addPackage(new MainReactPackage())
                .addPackage(new AsyncStoragePackage())
                .addPackage(new FastImageViewPackage())
                .addPackage(new ReactVideoPackage())
                .addPackage(new ReanimatedPackage())
                .addPackage(new RNCameraPackage())
                .addPackage(new RNFetchBlobPackage())
                .addPackage(new RNFSPackage())
                .addPackage(new RNGestureHandlerPackage())
                .addPackage(new LbryReactPackage())
                .setUseDeveloperSupport(true)
                .setInitialLifecycleState(LifecycleState.RESUMED)
                .build();
        mReactRootView.startReactApplication(mReactInstanceManager, "LBRYApp", null);

        registerNotificationsReceiver();

        setContentView(mReactRootView);
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

                if (uri == null || outpoint == null || fileInfoJson == null) {
                    return;
                }

                try {
                    String eventName = null;
                    JSONObject json = new JSONObject(fileInfoJson);
                    WritableMap fileInfo = JSONObjectToMap(json);
                    WritableMap params = Arguments.createMap();
                    params.putString("uri", uri);
                    params.putString("outpoint", outpoint);
                    params.putMap("fileInfo", fileInfo);

                    if (DownloadManager.ACTION_UPDATE.equals(downloadAction)) {
                        double progress = intent.getDoubleExtra("progress", 0);
                        params.putDouble("progress", progress);
                        eventName = "onDownloadUpdated";
                    } else {
                        eventName = (DownloadManager.ACTION_START.equals(downloadAction)) ? "onDownloadStarted" : "onDownloadCompleted";
                    }

                    ReactContext reactContext = mReactInstanceManager.getCurrentReactContext();
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

    private void registerStopReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LbrynetService.ACTION_STOP_SERVICE);
        stopServiceReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                MainActivity.this.receivedStopService = true;
                MainActivity.this.finish();
            }
        };
        registerReceiver(stopServiceReceiver, intentFilter);
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
        switch (requestCode) {
            case STORAGE_PERMISSION_REQ_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (BuildConfig.DEBUG && !Settings.canDrawOverlays(this)) {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                                   Uri.parse("package:" + getPackageName()));
                        startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE);
                    }
                } else {
                    // Permission not granted. Show a message and terminate the application
                    Toast.makeText(this,
                        "LBRY requires access to your device storage to be able to download files and media." +
                        " Please enable the storage permission and restart the app.", Toast.LENGTH_LONG).show();
                    if (serviceRunning) {
                        ServiceHelper.stop(this, LbrynetService.class);
                    }
                    finish();
                }
                break;

            case PHONE_STATE_PERMISSION_REQ_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted. Emit an onPhoneStatePermissionGranted event
                    ReactContext reactContext = mReactInstanceManager.getCurrentReactContext();
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
                    ReactContext reactContext = mReactInstanceManager.getCurrentReactContext();
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

    public static String acquireDeviceId(Context context) {
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
            checkPhoneStatePermission(context);
        } catch (Exception ex) {
            // id could not be obtained. Display a warning that rewards cannot be claimed.
        }

        if (id == null || id.trim().length() == 0) {
            Toast.makeText(context, "Rewards cannot be claimed because we could not identify your device.", Toast.LENGTH_LONG).show();
        }

        SharedPreferences sp = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(DEVICE_ID_KEY, id);
        editor.commit();

        return id;
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
        serviceRunning = isServiceRunning(LbrynetService.class);
        if (!serviceRunning) {
            ServiceHelper.start(this, "", LbrynetService.class, "lbrynetservice");
        }

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
            serviceRunning = isServiceRunning(LbrynetService.class);
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

        if (stopServiceReceiver != null) {
            unregisterReceiver(stopServiceReceiver);
            stopServiceReceiver = null;
        }

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.cancel(BackgroundMediaModule.NOTIFICATION_ID);
        notificationManager.cancel(DownloadManager.DOWNLOAD_NOTIFICATION_GROUP_ID);
        if (downloadNotificationIds != null) {
            for (int i = 0; i < downloadNotificationIds.size(); i++) {
                notificationManager.cancel(downloadNotificationIds.get(i));
            }
        }
        if (receivedStopService || !isServiceRunning(LbrynetService.class)) {
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

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
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
}
