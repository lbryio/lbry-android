package io.lbry.lbrynet;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.os.Bundle;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;

import org.kivy.android.PythonService;
import org.renpy.android.AssetExtract;
import org.renpy.android.ResourceManager;

/**
 * This service class is based on the auto-generated P4A service class
 * which changes the service start type to START_STICKY and lets it run
 * properly as a background service.
 *
 * @author akinwale
 * @version 0.1
 */
public class LbrynetService extends PythonService {
    
    public static String TAG = "LbrynetService";

    public static LbrynetService serviceInstance;

    @Override
    public int startType() {
        return START_STICKY;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Assign service instance
        serviceInstance = this;

        // Extract files
        File app_root_file = new File(getAppRoot());
        unpackData("private", app_root_file);

        if (intent == null) {
            intent = ServiceHelper.buildIntent(
                getApplicationContext(), "", LbrynetService.class, "lbrynetservice");
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        serviceInstance = null;
    }
    
    public String getAppRoot() {
        String app_root = getApplicationContext().getFilesDir().getAbsolutePath() + "/app";
        return app_root;
    }

    public void recursiveDelete(File f) {
        if (f.isDirectory()) {
            for (File r : f.listFiles()) {
                recursiveDelete(r);
            }
        }
        f.delete();
    }

    public void unpackData(final String resource, File target) {
        Log.v(TAG, "UNPACKING!!! " + resource + " " + target.getName());

        // The version of data in memory and on disk.
        ResourceManager resourceManager = new ResourceManager(getApplicationContext());
        String data_version = resourceManager.getString(resource + "_version");
        String disk_version = null;

        Log.v(TAG, "Data version is " + data_version);

        // If no version, no unpacking is necessary.
        if (data_version == null) {
            return;
        }

        // Check the current disk version, if any.
        String filesDir = target.getAbsolutePath();
        String disk_version_fn = filesDir + "/" + resource + ".version";

        try {
            byte buf[] = new byte[64];
            InputStream is = new FileInputStream(disk_version_fn);
            int len = is.read(buf);
            disk_version = new String(buf, 0, len);
            is.close();
        } catch (Exception e) {
            disk_version = "";
        }

        // If the disk data is out of date, extract it and write the
        // version file.
        // if (! data_version.equals(disk_version)) {
        if (! data_version.equals(disk_version)) {
            Log.v(TAG, "Extracting " + resource + " assets.");

            recursiveDelete(target);
            target.mkdirs();

            AssetExtract ae = new AssetExtract(getApplicationContext());
            if (!ae.extractTar(resource + ".mp3", target.getAbsolutePath())) {
                //toastError("Could not extract " + resource + " data.");
                Log.e(TAG, "Could not extract " + resource + " data.");
            }

            try {
                // Write .nomedia.
                new File(target, ".nomedia").createNewFile();

                // Write version file.
                FileOutputStream os = new FileOutputStream(disk_version_fn);
                os.write(data_version.getBytes());
                os.close();
            } catch (Exception e) {
                Log.w("python", e);
            }
        }
    }
}
