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
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;
import androidx.preference.PreferenceManager;

import io.lbry.browser.utils.Helper;
import io.lbry.browser.utils.Lbry;
import io.lbry.browser.utils.LbryAnalytics;
import io.lbry.browser.utils.Lbryio;

public class FirstRunActivity extends AppCompatActivity {

    private BroadcastReceiver sdkReadyReceiver;
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
        if (!Lbry.SDK_READY) {
            findViewById(R.id.welcome_wait_container).setVisibility(View.VISIBLE);
            IntentFilter filter = new IntentFilter();
            filter.addAction(MainActivity.ACTION_SDK_READY);
            sdkReadyReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    // authenticate after we receive the sdk ready event
                    authenticate();
                }
            };
            registerReceiver(sdkReadyReceiver, filter);
        } else {
            authenticate();
        }
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
        Helper.unregisterReceiver(sdkReadyReceiver, this);
        super.onDestroy();
    }

    private static class AuthenticateTask extends AsyncTask<Void, Void, Void> {
        private Context context;
        public AuthenticateTask(Context context) {
            this.context = context;
        }
        protected Void doInBackground(Void... params) {
            Lbryio.authenticate(context);
            return null;
        }
    }
}
