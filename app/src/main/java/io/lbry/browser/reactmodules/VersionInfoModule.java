package io.lbry.browser.reactmodules;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

public class VersionInfoModule extends ReactContextBaseJavaModule {
    private Context context;

    public VersionInfoModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.context = reactContext;
    }

    @Override
    public String getName() {
        return "VersionInfo";
    }

    @ReactMethod
    public void getAppVersion(final Promise promise) {
        PackageManager packageManager = this.context.getPackageManager();
        String packageName = this.context.getPackageName();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
            promise.resolve(packageInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            // normally shouldn't happen
            promise.resolve("Unknown");
        }
    }
}
