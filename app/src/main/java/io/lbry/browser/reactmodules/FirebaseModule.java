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
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

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

    private FirebaseAnalytics firebaseAnalytics;

    public FirebaseModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.context = reactContext;
        this.firebaseAnalytics = FirebaseAnalytics.getInstance(context);
    }

    @Override
    public String getName() {
        return "Firebase";
    }

    @ReactMethod
    public void setCurrentScreen(String name, final Promise promise) {
        final Activity activity = getCurrentActivity();
        if (activity != null && firebaseAnalytics != null) {
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    firebaseAnalytics.setCurrentScreen(activity, name, Utils.capitalizeAndStrip(name));
                }
            });
        }
        promise.resolve(true);
    }

    @ReactMethod
    public void track(String name, ReadableMap payload) {
        Bundle bundle = new Bundle();
        if (payload != null) {
            HashMap<String, Object> payloadMap = payload.toHashMap();
            for (Map.Entry<String, Object> entry : payloadMap.entrySet()) {
                Object value = entry.getValue();
                if (value != null) {
                    bundle.putString(entry.getKey(), entry.getValue().toString());
                }
            }
        }

        if (firebaseAnalytics != null) {
            firebaseAnalytics.logEvent(name, bundle);
        }
    }

    @ReactMethod
    public void logException(boolean fatal, String message, String error) {
        Bundle bundle = new Bundle();
        bundle.putString("message", message);
        bundle.putString("error", error);
        if (firebaseAnalytics != null) {
            firebaseAnalytics.logEvent(fatal ? "reactjs_exception" : "reactjs_warning", bundle);
        }

        if (fatal) {
            Toast.makeText(context,
                           "An application error occurred which has been automatically logged. " +
                           "If you keep seeing this message, please provide feedback to the LBRY " +
                           "team by emailing hello@lbry.com.",
                            Toast.LENGTH_LONG).show();
        }
    }

    @ReactMethod
    public void getMessagingToken(final Promise promise) {
        FirebaseInstanceId.getInstance().getInstanceId()
            .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                @Override
                public void onComplete(Task<InstanceIdResult> task) {
                    if (!task.isSuccessful()) {
                        promise.reject("Firebase getInstanceId call failed");
                        return;
                    }

                    // Get new Instance ID token
                    String token = task.getResult().getToken();
                    promise.resolve(token);
                }
            });
    }
    
    @ReactMethod
    public void logLaunchTiming() {
        Date end = new Date();
        MainActivity.LaunchTiming currentTiming = MainActivity.CurrentLaunchTiming;
        if (currentTiming == null) {
            // no start timing data, so skip this
            return;
        }
        
        long totalTimeMs = end.getTime() - currentTiming.getStart().getTime();
        String eventName = currentTiming.isColdStart() ? "app_cold_start" : "app_warm_start";
        Bundle bundle = new Bundle();
        bundle.putLong("total_ms", totalTimeMs);
        bundle.putLong("total_seconds", new Double(Math.ceil(totalTimeMs / 1000.0)).longValue());
        if (firebaseAnalytics != null) {
            firebaseAnalytics.logEvent(eventName, bundle);
        }
    }
}