package io.lbry.browser.reactmodules;

import android.app.Activity;
import android.app.NotificationChannel;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.SharedPreferences;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import io.lbry.browser.LbrynetService;
import io.lbry.browser.MainActivity;
import io.lbry.browser.ServiceHelper;

public class DaemonServiceControlModule extends ReactContextBaseJavaModule {

    private Context context;

    public DaemonServiceControlModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.context = reactContext;
    }

    @Override
    public String getName() {
        return "DaemonServiceControl";
    }

    @ReactMethod
    public void startService() {
        ServiceHelper.start(context, "", LbrynetService.class, "lbrynetservice");
    }

    @ReactMethod
    public void stopService() {
        ServiceHelper.stop(context, LbrynetService.class);
    }

    @ReactMethod
    public void setKeepDaemonRunning(boolean value) {
        if (context != null) {
            SharedPreferences sp = context.getSharedPreferences(MainActivity.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean(MainActivity.SETTING_KEEP_DAEMON_RUNNING, value);
            editor.commit();
        }
    }
}
