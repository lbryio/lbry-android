package io.lbry.browser.reactmodules;

import android.content.Context;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;

import com.mixpanel.android.mpmetrics.MixpanelAPI;

import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;
import org.json.JSONException;

public class MixpanelModule extends ReactContextBaseJavaModule {

    // TODO: Detect dev / debug and release mode and update value accordingly
    //private static final String MIXPANEL_TOKEN = "93b81fb957cb0ddcd3198c10853a6a95"; // Production
    private static final String MIXPANEL_TOKEN = "bc1630b8be64c5dfaa4700b3a62969f3"; // Dev Testing

    private Context context;
    
    private MixpanelAPI mixpanel;

    public MixpanelModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.context = reactContext;
        this.mixpanel = MixpanelAPI.getInstance(this.context, MIXPANEL_TOKEN);
    }

    @Override
    public String getName() {
        return "Mixpanel";
    }
    
    @ReactMethod
    public void track(String name, ReadableMap payload) {
        JSONObject props = new JSONObject();
        try {
            if (payload != null) {
                HashMap<String, Object> payloadMap = payload.toHashMap();
                for (Map.Entry<String, Object> entry : payloadMap.entrySet()) {
                    props.put(entry.getKey(), entry.getValue());
                }
            }
        } catch (JSONException e) {
            // Cannot use props. Stick with empty props.    
        }
        
        if (mixpanel != null) {
            mixpanel.track(name, props);
        }
    }
}
