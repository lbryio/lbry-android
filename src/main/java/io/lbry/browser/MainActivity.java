package io.lbry.browser;

import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.app.ActivityManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.SharedPreferences;
import android.Manifest;
import android.net.Uri;
import android.provider.Settings;
import android.widget.Toast;

import com.brentvatne.react.ReactVideoPackage;
import com.facebook.react.common.LifecycleState;
import com.facebook.react.modules.core.DefaultHardwareBackBtnHandler;
import com.facebook.react.ReactRootView;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.shell.MainReactPackage;

import io.lbry.browser.reactpackages.LbryReactPackage;
import io.lbry.browser.reactmodules.DownloadManagerModule;

public class MainActivity extends Activity implements DefaultHardwareBackBtnHandler {
    
    private static final int OVERLAY_PERMISSION_REQ_CODE = 101;
    
    private static final int STORAGE_PERMISSION_REQ_CODE = 201;    

    private ReactRootView mReactRootView;
    
    private ReactInstanceManager mReactInstanceManager;
    
    public static final String SHARED_PREFERENCES_NAME = "LBRY";
    
    /**
     * Flag which indicates whether or not the service is running. Will be updated in the
     * onResume method.
     */
    private boolean serviceRunning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Request external storage permission on Android version >= 6
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Toast.makeText(this,
                        "LBRY requires access to your device storage to be able to download files and media.", Toast.LENGTH_LONG).show();
                } else {
                    ActivityCompat.requestPermissions(this,
                        new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE }, STORAGE_PERMISSION_REQ_CODE);
                }
            }
        }
      
        super.onCreate(savedInstanceState);
        
        // Start the daemon service if it is not started
        serviceRunning = isServiceRunning(LbrynetService.class);
        if (!serviceRunning) {
            ServiceHelper.start(this, "", LbrynetService.class, "lbrynetservice");
        }
        
        mReactRootView = new ReactRootView(this);
        mReactInstanceManager = ReactInstanceManager.builder()
                .setApplication(getApplication())
                .setBundleAssetName("index.android.bundle")
                .setJSMainModulePath("index")
                .addPackage(new MainReactPackage())
                .addPackage(new ReactVideoPackage())
                .addPackage(new LbryReactPackage())
                .setUseDeveloperSupport(true)
                .setInitialLifecycleState(LifecycleState.RESUMED)
                .build();
        mReactRootView.startReactApplication(mReactInstanceManager, "LBRYApp", null);

        setContentView(mReactRootView);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == OVERLAY_PERMISSION_REQ_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this)) {
                    // SYSTEM_ALERT_WINDOW permission not granted...
                }
            }
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case STORAGE_PERMISSION_REQ_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (BuildConfig.DEBUG && !Settings.canDrawOverlays(this)) {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                                   Uri.parse("package:" + getPackageName()));
                        startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE);
                    }
                } else {
                    // Permission not granted. Show a message and terminate the application
                    Toast.makeText(this,
                        "LBRY requires access to your device storage to be able to download files and media." +
                        " Please enable the storage permission and restart the app.", Toast.LENGTH_LONG).show();
                    if (serviceRunning) {
                        ServiceHelper.stop(this, LbrynetService.class);
                    }
                    finish();
                }
                break;
        }
    }

    @Override
    public void invokeDefaultOnBackPressed() {
        super.onBackPressed();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
    
        if (mReactInstanceManager != null) {
            mReactInstanceManager.onHostPause(this);
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
    
        serviceRunning = isServiceRunning(LbrynetService.class);
        if (!serviceRunning) {
            ServiceHelper.start(this, "", LbrynetService.class, "lbrynetservice");
        }
    
        if (mReactInstanceManager != null) {
            mReactInstanceManager.onHostResume(this, this);
        }
    }
    
    @Override
    protected void onDestroy() {
        // check service running setting and end it here
        SharedPreferences sp = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        boolean shouldKeepDaemonRunning = sp.getBoolean("keepDaemonRunning", true);
        if (!shouldKeepDaemonRunning) {
            serviceRunning = isServiceRunning(LbrynetService.class);
            if (serviceRunning) {
               ServiceHelper.stop(this, LbrynetService.class);
            }
        }

        super.onDestroy();
        
        if (mReactInstanceManager != null) {
            mReactInstanceManager.onHostDestroy(this);
        }
    }

    @Override
    public void onBackPressed() {
        if (mReactInstanceManager != null) {
            mReactInstanceManager.onBackPressed();
        } else {
            super.onBackPressed();
        }
    }
   
    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo serviceInfo : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(serviceInfo.service.getClassName())) {
                return true;
            }
        }

        return false;
    }
}
