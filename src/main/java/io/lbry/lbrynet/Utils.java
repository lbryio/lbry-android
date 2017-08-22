package io.lbry.lbrynet;

import android.content.Context;
import java.io.File;

public final class Utils {
    public static String getAndroidRelease() {
        return android.os.Build.VERSION.RELEASE;
    }

    public static int getAndroidSdk() {
        return android.os.Build.VERSION.SDK_INT;
    }

    public static String getFilesDir(Context context) {
        return context.getFilesDir().getAbsolutePath();
    }

    public static String getAppInternalStorageDir(Context context) {
        File[] dirs = context.getExternalFilesDirs(null);
        return dirs[0].getAbsolutePath();
    }

    public static String getAppExternalStorageDir(Context context) {
        File[] dirs = context.getExternalFilesDirs(null);
        if (dirs.length > 1) {
            return dirs[1].getAbsolutePath();
        }
        return null;
    }

    public static String getInternalStorageDir(Context context) {
        String appInternal = getAppInternalStorageDir(context);
        return writableRootForPath(appInternal);
    }

    public static String getExternalStorageDir(Context context) {
        String appExternal = getAppInternalStorageDir(context);
        if (appExternal == null) {
            return null;
        }

        return writableRootForPath(appExternal);
    }

    public static String writableRootForPath(String path) {
        File file = new File(path);
        while (file != null && file.canWrite()) {
            File parent = file.getParentFile();
            if (parent != null && !parent.canWrite()) {
                break;
            }
            file = parent;
        }

        return file.getAbsolutePath();
    }
}
