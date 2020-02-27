package io.lbry.browser.reactmodules;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

public class ScreenOrientationModule extends ReactContextBaseJavaModule {
    private Context context;

    public ScreenOrientationModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.context = reactContext;
    }

    @Override
    public String getName() {
        return "ScreenOrientation";
    }

    @ReactMethod
    public void unlockOrientation() {
        Activity activity = getCurrentActivity();
        if (activity != null) {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
        }
    }

    @ReactMethod
    public void lockOrientationLandscape() {
        Activity activity = getCurrentActivity();
        if (activity != null) {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    }

    @ReactMethod
    public void lockOrientationPortrait() {
        Activity activity = getCurrentActivity();
        if (activity != null) {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }
}
