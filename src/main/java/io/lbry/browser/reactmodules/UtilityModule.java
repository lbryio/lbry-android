package io.lbry.browser.reactmodules;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
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
    public void getDeviceId(final Promise promise) {
        SharedPreferences sp = context.getSharedPreferences(MainActivity.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        String deviceId = sp.getString(MainActivity.DEVICE_ID_KEY, null);
        promise.resolve(deviceId);
    }
}
