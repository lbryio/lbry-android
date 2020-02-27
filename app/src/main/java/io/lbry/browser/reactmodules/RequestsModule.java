package io.lbry.browser.reactmodules;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.facebook.react.bridge.ReadableType;
import com.facebook.react.bridge.WritableMap;

import io.lbry.browser.MainActivity;
import io.lbry.lbrysdk.Utils;

import java.util.List;
import java.util.ArrayList;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

public class RequestsModule extends ReactContextBaseJavaModule {
    private Context context;
    
    public RequestsModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.context = reactContext;
    }

    @Override
    public String getName() {
        return "Requests";
    }
    
    @ReactMethod
    public void get(final String url, final Promise promise) {
        (new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                try {
                    return Utils.performRequest(url);
                } catch (Exception ex) {
                    return null;
                }
            }
            
            protected void onPostExecute(String response) {
                if (response == null) {
                    promise.reject(String.format("Request to %s returned null.", url));
                    return;
                }
                
                promise.resolve(response);
            }
        }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        
    }
    
    @ReactMethod
    public void lbryioCall(String authToken, final Promise promise) {
        // get the auth token here, or let the app pass it in?
    }
    
    @ReactMethod
    public void lbryCall(final Promise promise) {
        
    }
}