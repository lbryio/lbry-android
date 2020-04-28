package io.lbry.browser;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;

import androidx.fragment.app.FragmentActivity;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.snackbar.Snackbar;

import java.util.Arrays;

import io.lbry.browser.adapter.VerificationPagerAdapter;
import io.lbry.browser.listener.SignInListener;
import io.lbry.browser.listener.WalletSyncListener;
import io.lbry.browser.model.lbryinc.User;
import io.lbry.browser.tasks.FetchCurrentUserTask;
import io.lbry.browser.utils.Lbryio;

public class VerificationActivity extends FragmentActivity implements SignInListener, WalletSyncListener {

    public static final int VERIFICATION_FLOW_SIGN_IN = 1;
    public static final int VERIFICATION_FLOW_REWARDS = 2;
    public static final int VERIFICATION_FLOW_WALLET = 3;

    private String email;
    private boolean signedIn;
    private int flow;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        signedIn = Lbryio.isSignedIn();
        Intent intent = getIntent();
        if (intent != null) {
            flow = intent.getIntExtra("flow", -1);
            if (flow == -1 || (flow == VERIFICATION_FLOW_SIGN_IN && signedIn)) {
                // no flow specified (or user is already signed in), just exit
                setResult(signedIn ? RESULT_OK : RESULT_CANCELED);
                finish();
                return;
            }
        }

        if (!Arrays.asList(VERIFICATION_FLOW_SIGN_IN, VERIFICATION_FLOW_REWARDS, VERIFICATION_FLOW_WALLET).contains(flow)) {
            // invalid flow specified
            setResult(RESULT_CANCELED);
            finish();
            return;
        }

        setContentView(R.layout.activity_verification);
        ViewPager2 viewPager = findViewById(R.id.verification_pager);
        viewPager.setUserInputEnabled(false);
        viewPager.setSaveEnabled(false);
        viewPager.setAdapter(new VerificationPagerAdapter(this));

        if (Lbryio.isSignedIn() && flow == VERIFICATION_FLOW_WALLET) {
            viewPager.setCurrentItem(1);
        }

        findViewById(R.id.verification_close_button).setVisibility(View.VISIBLE);
        findViewById(R.id.verification_close_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        // ignore back press
        return;
    }

    public void onEmailAdded(String email) {
        this.email = email;
        findViewById(R.id.verification_close_button).setVisibility(View.GONE);
    }
    public void onEmailEdit() {
        findViewById(R.id.verification_close_button).setVisibility(View.VISIBLE);
    }
    public void onEmailVerified() {
        Snackbar.make(findViewById(R.id.verification_pager), R.string.sign_in_successful, Snackbar.LENGTH_LONG).show();
        sendBroadcast(new Intent(MainActivity.ACTION_USER_SIGN_IN_SUCCESS));

        if (flow == VERIFICATION_FLOW_SIGN_IN) {
            final Intent resultIntent = new Intent();
            resultIntent.putExtra("flow", VERIFICATION_FLOW_SIGN_IN);
            resultIntent.putExtra("email", email);

            // only sign in required, don't do anything else
            FetchCurrentUserTask task = new FetchCurrentUserTask(new FetchCurrentUserTask.FetchUserTaskHandler() {
                @Override
                public void onSuccess(User user) {
                    Lbryio.currentUser = user;
                    setResult(RESULT_OK, resultIntent);
                    finish();
                }

                @Override
                public void onError(Exception error) {
                    setResult(RESULT_CANCELED);
                    finish();
                }
            });
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            // change pager view depending on flow
            FetchCurrentUserTask task = new FetchCurrentUserTask(new FetchCurrentUserTask.FetchUserTaskHandler() {
                @Override
                public void onSuccess(User user) { Lbryio.currentUser = user; }
                @Override
                public void onError(Exception error) { }
            });
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

            ViewPager2 viewPager = findViewById(R.id.verification_pager);
            // for rewards, (show phone verification if not done, or manual verification if required)

            // for wallet sync, if password unlock is required, show password entry page
            viewPager.setCurrentItem(1);
        }
    }

    @Override
    public void onWalletSyncProcessing() {
        findViewById(R.id.verification_close_button).setVisibility(View.GONE);
    }
    @Override
    public void onWalletSyncWaitingForInput() {
        findViewById(R.id.verification_close_button).setVisibility(View.VISIBLE);
    }

    @Override
    public void onWalletSyncEnabled() {
        findViewById(R.id.verification_close_button).setVisibility(View.VISIBLE);
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void onWalletSyncFailed(Exception error) {
        findViewById(R.id.verification_close_button).setVisibility(View.VISIBLE);
    }
}
