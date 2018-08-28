package io.lbry.browser.reactmodules;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.Manifest;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.WindowManager;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import io.lbry.browser.MainActivity;

public class UtilityModule extends ReactContextBaseJavaModule {
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
            if (requestPermission) {
                requestPhoneStatePermission();
            }
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
    public void canAcquireDeviceId(final Promise promise) {
        if (isEmulator()) {
            promise.resolve(false);
        }

        promise.resolve(MainActivity.hasPermission(Manifest.permission.READ_PHONE_STATE, MainActivity.getActivity()));
    }

    @ReactMethod
    public void requestPhoneStatePermission() {
        MainActivity activity = (MainActivity) MainActivity.getActivity();
        if (activity != null) {
            // Request for the READ_PHONE_STATE permission
            MainActivity.checkPhoneStatePermission(activity);
        }
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
