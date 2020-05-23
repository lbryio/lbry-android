package io.lbry.browser.ui.other;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.core.content.FileProvider;
import androidx.preference.PreferenceManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import org.json.JSONObject;

import java.io.File;

import io.lbry.browser.MainActivity;
import io.lbry.browser.R;
import io.lbry.browser.exceptions.ApiCallException;
import io.lbry.browser.listener.SdkStatusListener;
import io.lbry.browser.ui.BaseFragment;
import io.lbry.browser.utils.Helper;
import io.lbry.browser.utils.Lbry;
import io.lbry.browser.utils.LbryAnalytics;
import io.lbry.browser.utils.Lbryio;
import io.lbry.lbrysdk.Utils;

public class AboutFragment extends BaseFragment implements SdkStatusListener {

    private static final String FILE_PROVIDER = "io.lbry.browser.fileprovider";

    private TextView textLinkWhatIsLBRY;
    private TextView textLinkAndroidBasics;
    private TextView textLinkFAQ;
    private TextView textLinkDiscord;
    private TextView textLinkFacebook;
    private TextView textLinkInstagram;
    private TextView textLinkReddit;
    private TextView textLinkTelegram;
    private TextView textLinkTwitter;

    private TextView textConnectedEmail;
    private TextView textAppVersion;
    private TextView textLbrySdkVersion;
    private TextView textPlatform;
    private TextView textInstallationId;
    private TextView textFirebaseToken;
    private View linkSendLog;
    private View linkUpdateMailingPreferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_about, container, false);

        textLinkWhatIsLBRY = root.findViewById(R.id.about_link_what_is_lbry);
        textLinkAndroidBasics = root.findViewById(R.id.about_link_android_basics);
        textLinkFAQ = root.findViewById(R.id.about_link_faq);
        textLinkDiscord = root.findViewById(R.id.about_link_discord);
        textLinkFacebook = root.findViewById(R.id.about_link_facebook);
        textLinkInstagram = root.findViewById(R.id.about_link_instagram);
        textLinkReddit = root.findViewById(R.id.about_link_reddit);
        textLinkTelegram = root.findViewById(R.id.about_link_telegram);
        textLinkTwitter = root.findViewById(R.id.about_link_twitter);

        TextView[] textLinks = {
                textLinkWhatIsLBRY, textLinkAndroidBasics, textLinkFAQ, textLinkDiscord, textLinkFacebook,
                textLinkInstagram, textLinkReddit, textLinkTelegram, textLinkTwitter
        };
        for (TextView view : textLinks) {
            Helper.applyHtmlForTextView(view);
        }

        textConnectedEmail = root.findViewById(R.id.about_connected_email);
        textAppVersion = root.findViewById(R.id.about_app_version);
        textLbrySdkVersion = root.findViewById(R.id.about_lbry_sdk);
        textPlatform = root.findViewById(R.id.about_platform);
        textInstallationId = root.findViewById(R.id.about_installation_id);
        textFirebaseToken = root.findViewById(R.id.about_firebase_token);
        linkSendLog = root.findViewById(R.id.about_send_log);
        linkUpdateMailingPreferences = root.findViewById(R.id.about_update_mailing_preferences);

        if (Lbryio.isSignedIn()) {
            textConnectedEmail.setText(Lbryio.getSignedInEmail());
            textConnectedEmail.setTypeface(null, Typeface.NORMAL);
            linkUpdateMailingPreferences.setVisibility(View.VISIBLE);
        } else {
            linkUpdateMailingPreferences.setVisibility(View.GONE);
        }

        Context context = getContext();
        String appVersion = getString(R.string.unknown);
        if (context != null) {
            try {
                PackageManager manager = context.getPackageManager();
                PackageInfo info = manager.getPackageInfo(context.getPackageName(), PackageManager.GET_ACTIVITIES);
                appVersion = info.versionName;
            } catch (PackageManager.NameNotFoundException ex) {
                // pass
            }
        }
        textAppVersion.setText(appVersion);
        textInstallationId.setText(Lbry.INSTALLATION_ID);
        textPlatform.setText(String.format("Android %s (API %d)", Utils.getAndroidRelease(), Utils.getAndroidSdk()));

        linkUpdateMailingPreferences.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("http://lbry.com/list/edit/%s", Lbryio.AUTH_TOKEN)));
                startActivity(intent);
            }
        });

        linkSendLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareLogFile();
            }
        });

        return root;
    }

    private void shareLogFile() {
        Context context = getContext();
        if (context != null) {
            String logFileName = "lbrynet.log";
            File logFile = new File(String.format("%s/%s", Utils.getAppInternalStorageDir(context), "lbrynet"), logFileName);
            if (!logFile.exists()) {
                Snackbar.make(getView(), R.string.cannot_find_lbrynet_log, Snackbar.LENGTH_LONG).
                        setBackgroundTint(Color.RED).
                        setTextColor(Color.WHITE).
                        show();
                return;
            }

            try {
                Uri fileUri = FileProvider.getUriForFile(getContext(), FILE_PROVIDER, logFile);
                if (fileUri != null) {
                    MainActivity.startingShareActivity = true;
                    Intent shareIntent = new Intent();
                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    shareIntent.setAction(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);

                    Intent sendLogIntent = Intent.createChooser(shareIntent, "Send LBRY log");
                    sendLogIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(sendLogIntent);
                }
            } catch (IllegalArgumentException e) {
                Snackbar.make(getView(), R.string.cannot_share_lbrynet_log, Snackbar.LENGTH_LONG).
                        setBackgroundTint(Color.RED).
                        setTextColor(Color.WHITE).
                        show();
            }
        }
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

            activity.setActionBarTitle(R.string.about_lbry);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Context context = getContext();
        if (context instanceof MainActivity) {
            MainActivity activity = (MainActivity) context;
            LbryAnalytics.setCurrentScreen(activity, "About", "About");

            if (!Lbry.SDK_READY) {
                activity.addSdkStatusListener(this);
            } else {
                onSdkReady();
            }
        }
        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(Task<InstanceIdResult> task) {
                Helper.setViewText(textFirebaseToken, task.isSuccessful() ? task.getResult().getToken() : getString(R.string.unknown));
            }
        });

    }

    @Override
    public void onStop() {
        Context context = getContext();
        if (context instanceof MainActivity) {
            MainActivity activity = (MainActivity) getContext();
            activity.removeSdkStatusListener(this);
            activity.restoreToggle();
            activity.showFloatingWalletBalance();
        }
        super.onStop();
    }

    public void onSdkReady() {
        loadLbryVersion();
    }

    private void loadLbryVersion() {
        (new AsyncTask<Void, Void, String>() {
            protected String doInBackground(Void... params) {
                try {
                    JSONObject result = (JSONObject) Lbry.genericApiCall(Lbry.METHOD_VERSION);
                    return Helper.getJSONString("lbrynet_version", null, result);
                } catch (ApiCallException | ClassCastException ex) {
                    // pass
                    return null;
                }
            }
            protected void onPostExecute(String version) {
                Helper.setViewText(textLbrySdkVersion, Helper.isNullOrEmpty(version) ? getString(R.string.unknown) : version);
            }
        }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}
