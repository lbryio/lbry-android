package io.lbry.browser.reactmodules;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;

import io.lbry.browser.BuildConfig;
import io.lbry.browser.MainActivity;
import io.lbry.lbrysdk.Utils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;
import org.json.JSONException;

public class FirebaseModule extends ReactContextBaseJavaModule {

    private Context context;


    public FirebaseModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    public String getName() {
        return "Firebase";
    }

    @ReactMethod
    public void setCurrentScreen(String name, final Promise promise) {
        promise.resolve(true);
    }

    @ReactMethod
    public void track(String name, ReadableMap payload) {
    }

    @ReactMethod
    public void logException(boolean fatal, String message, String error) {
    }

    @ReactMethod
    public void getMessagingToken(final Promise promise) {
                    promise.resolve("");
                }
            });
    }
    
    @ReactMethod
    public void logLaunchTiming() {
    }
}