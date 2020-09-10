package io.lbry.browser;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;
import androidx.preference.PreferenceManager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import io.lbry.browser.exceptions.AuthTokenInvalidatedException;
import io.lbry.browser.utils.Helper;
import io.lbry.browser.utils.Lbry;
import io.lbry.browser.utils.LbryAnalytics;
import io.lbry.browser.utils.Lbryio;
import io.lbry.lbrysdk.LbrynetService;
import io.lbry.lbrysdk.ServiceHelper;
import io.lbry.lbrysdk.Utils;

public class FirstRunActivity extends AppCompatActivity {

    private BroadcastReceiver sdkReceiver;
    private BroadcastReceiver authReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_run);

        TextView welcomeTos = findViewById(R.id.welcome_text_view_tos);
        welcomeTos.setMovementMethod(LinkMovementMethod.getInstance());
        welcomeTos.setText(HtmlCompat.fromHtml(getString(R.string.welcome_tos), HtmlCompat.FROM_HTML_MODE_LEGACY));

        findViewById(R.id.welcome_link_use_lbry).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishFirstRun();
            }
        });

        registerAuthReceiver();
        findViewById(R.id.welcome_wait_container).setVisibility(View.VISIBLE);
        IntentFilter filter = new IntentFilter();
        filter.addAction(MainActivity.ACTION_SDK_READY);
        filter.addAction(LbrynetService.ACTION_STOP_SERVICE);
        sdkReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (MainActivity.ACTION_SDK_READY.equals(action)) {
                    // authenticate after we receive the sdk ready event
                    authenticate();
                } else if (LbrynetService.ACTION_STOP_SERVICE.equals(action)) {
                    finish();
                    if (MainActivity.instance != null) {
                        MainActivity.instance.finish();
                    }
                }
            }
        };
        registerReceiver(sdkReceiver, filter);

        CheckInstallIdTask task = new CheckInstallIdTask(this, new CheckInstallIdTask.InstallIdHandler() {
            @Override
            public void onInstallIdChecked(boolean result) {
                // start the sdk from FirstRun
                boolean serviceRunning = MainActivity.isServiceRunning(MainActivity.instance, LbrynetService.class);
                if (!serviceRunning) {
                    Lbry.SDK_READY = false;
                    ServiceHelper.start(MainActivity.instance, "", LbrynetService.class, "lbrynetservice");
                }

                if (result) {
                    // install_id generated and validated, authenticate now
                    authenticate();
                    return;
                }

                // we weren't able to generate the install_id ourselves, depend on the sdk for that
                if (Lbry.SDK_READY) {
                    authenticate();
                    return;
                }
            }
        });
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void onResume() {
        super.onResume();
        LbryAnalytics.setCurrentScreen(this, "First Run", "FirstRun");
    }

    private void registerAuthReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(MainActivity.ACTION_USER_AUTHENTICATION_SUCCESS);
        filter.addAction(MainActivity.ACTION_USER_AUTHENTICATION_FAILED);
        authReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (MainActivity.ACTION_USER_AUTHENTICATION_SUCCESS.equals(intent.getAction())) {
                    handleAuthenticationSuccess();
                } else {
                    handleAuthenticationFailed();
                }
            }
        };
        registerReceiver(authReceiver, filter);
    }

    private void handleAuthenticationSuccess() {
        // first_auth completed event
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        boolean firstAuthCompleted = sp.getBoolean(MainActivity.PREFERENCE_KEY_INTERNAL_FIRST_AUTH_COMPLETED, false);
        if (!firstAuthCompleted) {
            LbryAnalytics.logEvent(LbryAnalytics.EVENT_FIRST_USER_AUTH);
            sp.edit().putBoolean(MainActivity.PREFERENCE_KEY_INTERNAL_FIRST_AUTH_COMPLETED, true).apply();
        }

        findViewById(R.id.welcome_wait_container).setVisibility(View.GONE);
        findViewById(R.id.welcome_display).setVisibility(View.VISIBLE);
        findViewById(R.id.welcome_link_use_lbry).setVisibility(View.VISIBLE);
    }

    private void handleAuthenticationFailed() {
        findViewById(R.id.welcome_progress_bar).setVisibility(View.GONE);
        ((TextView) findViewById(R.id.welcome_wait_text)).setText(R.string.startup_failed);
    }

    private void authenticate() {
        new AuthenticateTask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void finishFirstRun() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        sp.edit().putBoolean(MainActivity.PREFERENCE_KEY_INTERNAL_FIRST_RUN_COMPLETED, true).apply();

        // first_run_completed event
        LbryAnalytics.logEvent(LbryAnalytics.EVENT_FIRST_RUN_COMPLETED);
        finish();
    }

    @Override
    public void onBackPressed() {
        return;
    }

    @Override
    protected void onDestroy() {
        Helper.unregisterReceiver(authReceiver, this);
        Helper.unregisterReceiver(sdkReceiver, this);
        super.onDestroy();
    }

    private void generateIdAndAuthenticate() {

    }

    private static class CheckInstallIdTask extends AsyncTask<Void, Void, Boolean> {
        private Context context;
        private InstallIdHandler handler;
        public CheckInstallIdTask(Context context, InstallIdHandler handler) {
            this.context = context;
            this.handler = handler;
        }
        protected Boolean doInBackground(Void... params) {
            // Load the installation id from the file system
            String lbrynetDir = String.format("%s/%s", Utils.getAppInternalStorageDir(context), "lbrynet");
            File dir = new File(lbrynetDir);
            boolean dirExists = dir.isDirectory();
            if (!dirExists) {
                dirExists = dir.mkdirs();
            }

            if (!dirExists) {
                return false;
            }

            String installIdPath = String.format("%s/install_id", lbrynetDir);
            File file = new File(installIdPath);
            String installId  = null;
            if (!file.exists()) {
                // generate the install_id
                installId = Lbry.generateId();
                BufferedWriter writer = null;
                try {
                    writer = new BufferedWriter(new FileWriter(file));
                    writer.write(installId);
                    android.util.Log.d("LbryMain", "Generated install ID=" + installId);
                } catch (IOException ex) {
                    return false;
                } finally {
                    Helper.closeCloseable(writer);
                }
            } else {
                // read the installation id from the file
                BufferedReader reader = null;
                try {
                    reader = new BufferedReader(new InputStreamReader(new FileInputStream(installIdPath)));
                    installId = reader.readLine();
                } catch (IOException ex) {
                    return false;
                } finally {
                    Helper.closeCloseable(reader);
                }
            }

            if (!Helper.isNullOrEmpty(installId)) {
                Lbry.INSTALLATION_ID = installId;
            }
            return !Helper.isNullOrEmpty(installId);
        }
        protected void onPostExecute(Boolean result) {
            if (handler != null) {
                handler.onInstallIdChecked(result);
            }
        }

        public interface InstallIdHandler {
            void onInstallIdChecked(boolean result);
        }
    }

    private static class AuthenticateTask extends AsyncTask<Void, Void, Void> {
        private Context context;
        public AuthenticateTask(Context context) {
            this.context = context;
        }
        protected Void doInBackground(Void... params) {
            try {
                Lbryio.authenticate(context);
            } catch (AuthTokenInvalidatedException ex) {
                // pass
            }
            return null;
        }
    }
}
