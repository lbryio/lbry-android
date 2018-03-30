package io.lbry.browser;

import android.content.Intent;
import android.content.Context;

public class ServiceHelper {
    public static Intent buildIntent(Context ctx, String pythonServiceArgument, Class serviceClass, String pythonName) {
        Intent intent = new Intent(ctx, serviceClass);
        String argument = ctx.getFilesDir().getAbsolutePath() + "/app";
        intent.putExtra("androidPrivate", ctx.getFilesDir().getAbsolutePath());
        intent.putExtra("androidArgument", argument);
        intent.putExtra("serviceEntrypoint", "./" + pythonName + ".py");
        intent.putExtra("pythonName", pythonName);
        intent.putExtra("pythonHome", argument);
        intent.putExtra("pythonPath", argument + ":" + argument + "/lib");
        intent.putExtra("pythonServiceArgument", pythonServiceArgument);

        return intent;
    }

    public static void start(Context ctx, String pythonServiceArgument, Class serviceClass, String pythonName) {
        Intent intent = buildIntent(ctx, pythonServiceArgument, serviceClass, pythonName);
        ctx.startService(intent);
    }

    public static void stop(Context ctx, Class serviceClass) {
        Intent intent = new Intent(ctx, serviceClass);
        ctx.stopService(intent);
    }
}