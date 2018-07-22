package io.lbry.browser.reactmodules;

import android.app.Activity;
import android.content.Context;
import android.view.WindowManager;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

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
}
