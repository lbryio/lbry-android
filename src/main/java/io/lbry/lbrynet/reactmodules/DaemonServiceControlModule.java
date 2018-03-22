package io.lbry.lbrynet.reactmodules;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import io.lbry.lbrynet.LbrynetService;
import io.lbry.lbrynet.ServiceHelper;

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
    public void stopService() {
        ServiceHelper.stop(context, LbrynetService.class);
    }
}
