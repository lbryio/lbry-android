package io.lbry.lbrynet;

import android.content.Intent;
import android.content.Context;
import android.app.Notification;
import android.app.PendingIntent;
import android.os.Bundle;
import org.kivy.android.PythonService;
import org.kivy.android.PythonActivity;

/**
 * This service class is based on the auto-generated P4A service class
 * which changes the service start type to START_STICKY and lets it run
 * properly as a background service.
 *
 * @author akinwale
 * @version 0.1
 */
public class LbrynetService extends PythonService {
    @Override
    public int startType() {
        return START_STICKY;
    }

    @Override
    public boolean canDisplayNotification() {
        return false;
    }

    /*@Override
    protected void doStartForeground(Bundle extras) {
        Context context = getApplicationContext();
        Notification notification = new Notification(context.getApplicationInfo().icon,
            "lbrydroid", System.currentTimeMillis());
        Intent contextIntent = new Intent(context, PythonActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(context, 0, contextIntent,
            PendingIntent.FLAG_UPDATE_CURRENT);
        notification.setLatestEventInfo(context, "lbrydroid", "LbrynetService", pIntent);
        startForeground(1, notification);
    }*/


    public static void start(Context ctx, String pythonServiceArgument) {
        Intent intent = new Intent(ctx, LbrynetService.class);
        String argument = ctx.getFilesDir().getAbsolutePath() + "/app";
        intent.putExtra("androidPrivate", ctx.getFilesDir().getAbsolutePath());
        intent.putExtra("androidArgument", argument);
        intent.putExtra("serviceEntrypoint", "./lbrynetservice.py");
        intent.putExtra("pythonName", "lbrynetservice");
        intent.putExtra("pythonHome", argument);
        intent.putExtra("pythonPath", argument + ":" + argument + "/lib");
        intent.putExtra("pythonServiceArgument", pythonServiceArgument);
        ctx.startService(intent);
    }

    public static void stop(Context ctx) {
        Intent intent = new Intent(ctx, ServiceLbrynetservice.class);
        ctx.stopService(intent);
    }

    /**
     * TODO: Move to a Utils class
     */
    public static String getAndroidRelease() {
        return android.os.Build.VERSION.RELEASE;
    }

    public static int getAndroidSdk() {
        return android.os.Build.VERSION.SDK_INT;
    }
}