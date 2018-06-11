package io.lbry.browser.reactmodules;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.SharedPreferences;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import io.lbry.browser.MainActivity;

public class FirstRunModule extends ReactContextBaseJavaModule {
    private Context context;

    private SharedPreferences sp;

    public FirstRunModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.context = reactContext;
        this.sp = reactContext.getSharedPreferences(MainActivity.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    @Override
    public String getName() {
        return "FirstRun";
    }

    @ReactMethod
    public void isFirstRun(final Promise promise) {
        // If firstRun flag does not exist, default to true
        boolean firstRun = sp.getBoolean("firstRun", true);
        promise.resolve(firstRun);
    }

    @ReactMethod
    public void firstRunCompleted() {
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("firstRun", false);
        editor.commit();
    }
}
