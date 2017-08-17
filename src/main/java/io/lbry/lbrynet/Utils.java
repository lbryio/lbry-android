package io.lbry.lbrynet;

public final class Utils {
    public static String getAndroidRelease() {
        return android.os.Build.VERSION.RELEASE;
    }

    public static int getAndroidSdk() {
        return android.os.Build.VERSION.SDK_INT;
    }
}
