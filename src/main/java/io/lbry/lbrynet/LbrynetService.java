package io.lbry.lbrynet;

import android.app.Activity;
import android.content.Intent;
import android.content.Context;
import android.app.Notification;
import android.app.PendingIntent;
import android.util.Log;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.File;
import android.os.Bundle;
import org.kivy.android.PythonService;
import org.kivy.android.PythonActivity;
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

    @Override
    public int startType() {
        return START_STICKY;
    }

    @Override
    public boolean canDisplayNotification() {
        return false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Extract files
        File app_root_file = new File(getAppRoot());
        unpackData("private", app_root_file);

        if (intent == null) {
            intent = buildIntent(getApplicationContext(), "");
        }

        return super.onStartCommand(intent, flags, startId);
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

    public static Intent buildIntent(Context ctx, String pythonServiceArgument) {
        Intent intent = new Intent(ctx, LbrynetService.class);
        String argument = ctx.getFilesDir().getAbsolutePath() + "/app";
        intent.putExtra("androidPrivate", ctx.getFilesDir().getAbsolutePath());
        intent.putExtra("androidArgument", argument);
        intent.putExtra("serviceEntrypoint", "./lbrynetservice.py");
        intent.putExtra("pythonName", "lbrynetservice");
        intent.putExtra("pythonHome", argument);
        intent.putExtra("pythonPath", argument + ":" + argument + "/lib");
        intent.putExtra("pythonServiceArgument", pythonServiceArgument);

        return intent;
    }

    public static void start(Context ctx, String pythonServiceArgument) {
        Intent intent = buildIntent(ctx, pythonServiceArgument);
        ctx.startService(intent);
    }

    public static void stop(Context ctx) {
        Intent intent = new Intent(ctx, LbrynetService.class);
        ctx.stopService(intent);
    }
}
