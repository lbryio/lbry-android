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

import java.util.List;
import java.util.ArrayList;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

public class StatePersistorModule extends ReactContextBaseJavaModule {
    private Context context;

    private List<ReadableMap> queue;
    
    private ReadableMap filter;
    
    private ReadableMap lastState;

    private AsyncTask persistTask;
    
    public StatePersistorModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.context = reactContext;
        queue = new ArrayList<ReadableMap>();
    }

    @Override
    public String getName() {
        return "StatePersistor";
    }
    
    /*private WritableMap filterState(ReadableMap state) {
        WritableMap filteredState = Arguments.createMap();
    
        return state;
    }*/
    
    public boolean hasStateChanged(ReadableMap newState) {
        return false;
    }

    @ReactMethod
    public void update(ReadableMap state, ReadableMap filter) {
        if (this.filter == null) {
            this.filter = filter;
        }
        // process state updates from the queue using a background task
        synchronized (this) {
            queue.add(state);
        }
        persistState();
    }
    
    private void persistState() {
        persistState(false);
    }
    
    private void persistState(final boolean flush) {
        if (flush && persistTask != null) {
            persistTask.cancel(true);
            persistTask = null;
        }
        
        if (persistTask == null) {
            persistTask = (new AsyncTask<Object, Void, Boolean>() {
                protected Boolean doInBackground(Object... param) {
                    // get the first item in the queue
                    ReadableMap queuedState = null;
                    if (queue.size() > 0) {
                        synchronized (StatePersistorModule.this) {
                            queuedState = queue.remove(flush ? queue.size() - 1 : 0);
                            if (flush) {
                                // we only want the final state in this scenario
                                queue.clear();
                            }
                        }
                    }
                    
                    if (queuedState != null) {
                        ReadableMap state = queuedState; //(ReadableMap) filterState(queuedState);
                        // convert to JSON object
                        
                        try {
                            JSONObject json = readableMapToJSON(state);
                            
                            // save the state file
                            // TODO: explore this option at a later date
                            throw new UnsupportedOperationException();
                        } catch (JSONException ex) {
                            // normally shouldn't happen, but if it does, reinsert into the queue
                            if (queuedState != null) {
                                synchronized (StatePersistorModule.this) {
                                    queue.add(0, queuedState);
                                }
                            }
                            return false;
                        }
                    }
                    
                    return false;
                }
        
                public void onPostExecute(Boolean result) {
                    if (queue.size() > 0) {
                        persistState();
                    }
                    
                    persistTask = null;
                }
            });
            persistTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }
    
    @ReactMethod
    public void flush() {
        persistState(true);
    }

    private static JSONObject readableMapToJSON(ReadableMap readableMap) throws JSONException {
        JSONObject json = new JSONObject();
        ReadableMapKeySetIterator iterator = readableMap.keySetIterator();
        while (iterator.hasNextKey()) {
            String key = iterator.nextKey();
            switch (readableMap.getType(key)) {
                case Map:
                    json.put(key, readableMapToJSON(readableMap.getMap(key)));
                    break;
                case Array:
                    json.put(key, readableArrayToJSON(readableMap.getArray(key)));
                    break;
                case Boolean:
                    json.put(key, readableMap.getBoolean(key));
                    break;
                case Null:
                    json.put(key, JSONObject.NULL);
                    break;
                case Number:
                    json.put(key, readableMap.getDouble(key));
                    break;
                case String:
                    json.put(key, readableMap.getString(key));
                    break;
            }
        }

        return json;
    }

    private static JSONArray readableArrayToJSON(ReadableArray readableArray) throws JSONException {
        JSONArray array = new JSONArray();
        for (int i = 0; i < readableArray.size(); i++) {
            switch (readableArray.getType(i)) {
                case Null:
                    break;
                case Boolean:
                    array.put(readableArray.getBoolean(i));
                    break;
                case Number:
                    array.put(readableArray.getDouble(i));
                    break;
                case String:
                    array.put(readableArray.getString(i));
                    break;
                case Map:
                    array.put(readableMapToJSON(readableArray.getMap(i)));
                    break;
                case Array:
                    array.put(readableArrayToJSON(readableArray.getArray(i)));
                    break;
            }
        }
        
        return array;
    }
}