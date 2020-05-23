package io.lbry.browser.ui.other;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import java.io.FileOutputStream;
import java.io.PrintStream;

import io.lbry.browser.MainActivity;
import io.lbry.browser.R;
import io.lbry.browser.utils.Helper;
import io.lbry.browser.utils.LbryAnalytics;
import io.lbry.lbrysdk.Utils;

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings, rootKey);
    }

    @Override
    public void onStart() {
        super.onStart();
        MainActivity activity = (MainActivity) getContext();
        if (activity != null) {
            activity.hideSearchBar();
            activity.showNavigationBackIcon();
            activity.lockDrawer();
            activity.hideFloatingWalletBalance();

            activity.setActionBarTitle(R.string.settings);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Context context = getContext();
        if (context instanceof MainActivity) {
            PreferenceManager.getDefaultSharedPreferences(context).registerOnSharedPreferenceChangeListener(this);
            MainActivity activity = (MainActivity) context;
            LbryAnalytics.setCurrentScreen(activity, "Settings", "Settings");
        }
    }
    @Override
    public void onPause() {
        Context context = getContext();
        if (context != null) {
            PreferenceManager.getDefaultSharedPreferences(context).unregisterOnSharedPreferenceChangeListener(this);
        }
        super.onPause();
    }
    @Override
    public void onStop() {
        Context context = getContext();
        if (context instanceof MainActivity) {
            MainActivity activity = (MainActivity) getContext();
            activity.restoreToggle();
            activity.showFloatingWalletBalance();
        }
        super.onStop();
    }

    public void onSharedPreferenceChanged(SharedPreferences sp, String key) {
        if (key.equalsIgnoreCase(MainActivity.PREFERENCE_KEY_DARK_MODE)) {
            boolean darkMode = sp.getBoolean(MainActivity.PREFERENCE_KEY_DARK_MODE, false);
            AppCompatDelegate.setDefaultNightMode(darkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        } else if (key.equalsIgnoreCase(MainActivity.PREFERENCE_KEY_PARTICIPATE_DATA_NETWORK)) {
            boolean dhtEnabled = sp.getBoolean(MainActivity.PREFERENCE_KEY_PARTICIPATE_DATA_NETWORK, false);
            updateDHTFileSetting(dhtEnabled);
        }
    }

    private void updateDHTFileSetting(final boolean enabled) {
        Context context = getContext();
        (new AsyncTask<Void, Void, Void>() {
            protected Void doInBackground(Void... params) {
                PrintStream out = null;
                try {
                    String fileContent = enabled ? "on" : "off";
                    String path = String.format("%s/%s", Utils.getAppInternalStorageDir(context), "dht");
                    out = new PrintStream(new FileOutputStream(path));
                    out.print(fileContent);
                } catch (Exception ex) {
                    // pass
                } finally {
                    Helper.closeCloseable(out);
                }
                return null;
            }
        }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}
