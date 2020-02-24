package io.lbry.browser;

import android.content.Intent;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import java.io.Closeable;
import java.io.File;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class ServiceHelper {
    
    public static final String SHARED_PREFERENCES_NAME = "LBRY";

    private static final String TAG = "io.lbry.browser.ServiceHelper";

    private static final String HEADERS_ASSET_KEY = "headersAssetInitialized";

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

    public static void start(final Context ctx, String pythonServiceArgument, Class serviceClass, String pythonName) {
        // always check initial headers status before starting the service
        final SharedPreferences sp = ctx.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        boolean initHeaders = sp.getBoolean(HEADERS_ASSET_KEY, false);
        if (initHeaders) {
            // initial headers asset copy already done. simply start the service
            Intent intent = buildIntent(ctx, pythonServiceArgument, serviceClass, pythonName);
            ctx.startService(intent);
            return;
        }

        (new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                String targetPath = String.format("%s/lbryum/lbc_mainnet", Utils.getAppInternalStorageDir(ctx));
                File targetDir = new File(targetPath);
                if (!targetDir.exists()) {
                    targetDir.mkdirs();
                }

                String headersPath = String.format("%s/headers", targetPath);
                android.util.Log.d(TAG, String.format("HeadersPath: %s", headersPath));
                File targetHeadersFile = new File(headersPath);
                InputStream in = null;
                FileOutputStream out = null;

                long sourceLen = 0;
                long targetLen = 0;
                try {
                    android.util.Log.d(TAG, "Opening asset: blockchain/headers");
                    in = ctx.getAssets().open("blockchain/headers");
                    out = new FileOutputStream(targetHeadersFile);
                    byte[] b = new byte[1120]; // 10 headers at a time?
                    int len;
                    while ((len = in.read(b, 0, 1120)) > 0) {
                        sourceLen += len;
                        out.write(b, 0, len);
                    }
                } catch (Exception ex) {
                    // failed to copy the headers. sdk will perform the download instead.
                    android.util.Log.e(TAG, "failed to copy headers asset", ex);
                } finally {
                    closeStream(in);
                    closeStream(out);

                    // only mark the copy as successful if the file sizes are the same
                    targetLen = targetHeadersFile.exists() ? targetHeadersFile.length() : 0;
                    android.util.Log.d(TAG, String.format("SourceLength=%d; TargetLength=%d", sourceLen, targetLen));
                    if (targetLen > 0 && targetLen == sourceLen) {
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putBoolean(HEADERS_ASSET_KEY, true);
                        editor.commit();
                    }
                }

                return null;
            }

            protected void onPostExecute(Void result) {
                Intent intent = buildIntent(ctx, pythonServiceArgument, serviceClass, pythonName);
                ctx.startService(intent);
            }
        }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public static void stop(Context ctx, Class serviceClass) {
        Intent intent = new Intent(ctx, serviceClass);
        ctx.stopService(intent);
    }

    private static void closeStream(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException ex) {
            // pass
        }
    }
}