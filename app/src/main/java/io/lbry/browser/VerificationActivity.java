package io.lbry.browser;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;

import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.snackbar.Snackbar;

import java.util.Arrays;

import io.lbry.browser.adapter.VerificationPagerAdapter;
import io.lbry.browser.listener.SignInListener;
import io.lbry.browser.listener.WalletSyncListener;
import io.lbry.browser.model.lbryinc.User;
import io.lbry.browser.tasks.lbryinc.FetchCurrentUserTask;
import io.lbry.browser.utils.LbryAnalytics;
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

        findViewById(R.id.verification_close_button).setVisibility(View.VISIBLE);
        findViewById(R.id.verification_close_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
    }

    public void onResume() {
        super.onResume();
        LbryAnalytics.setCurrentScreen(this, "Verification", "Verification");
        checkFlow();
    }

    public void checkFlow() {
        ViewPager2 viewPager = findViewById(R.id.verification_pager);
        if (Lbryio.isSignedIn()) {
            boolean flowHandled = false;
            if (flow == VERIFICATION_FLOW_WALLET) {
                viewPager.setCurrentItem(VerificationPagerAdapter.PAGE_VERIFICATION_WALLET, false);
                flowHandled = true;
            } else if (flow == VERIFICATION_FLOW_REWARDS) {
                User user = Lbryio.currentUser;
                if (!user.isIdentityVerified()) {
                    // phone number verification required
                    viewPager.setCurrentItem(VerificationPagerAdapter.PAGE_VERIFICATION_PHONE, false);
                    flowHandled = true;
                } else if (!user.isRewardApproved()) {
                    // manual verification required
                    viewPager.setCurrentItem(VerificationPagerAdapter.PAGE_VERIFICATION_MANUAL, false);
                    flowHandled = true;
                }
            }

            if (!flowHandled) {
                // user has already been verified and or reward approved
                setResult(RESULT_CANCELED);
                finish();
                return;
            }
        }
    }

    public void showLoading() {
        findViewById(R.id.verification_loading_progress).setVisibility(View.VISIBLE);
        findViewById(R.id.verification_pager).setVisibility(View.INVISIBLE);
        findViewById(R.id.verification_close_button).setVisibility(View.GONE);
    }

    public void hideLoading() {
        findViewById(R.id.verification_loading_progress).setVisibility(View.GONE);
        findViewById(R.id.verification_pager).setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        // ignore back press
        return;
    }

    public void onEmailAdded(String email) {
        this.email = email;
        findViewById(R.id.verification_close_button).setVisibility(View.GONE);

        Bundle bundle = new Bundle();
        bundle.putString("email", email);
        LbryAnalytics.logEvent(LbryAnalytics.EVENT_EMAIL_ADDED, bundle);
    }
    public void onEmailEdit() {
        findViewById(R.id.verification_close_button).setVisibility(View.VISIBLE);
    }
    public void onEmailVerified() {
        Snackbar.make(findViewById(R.id.verification_pager), R.string.sign_in_successful, Snackbar.LENGTH_LONG).show();
        sendBroadcast(new Intent(MainActivity.ACTION_USER_SIGN_IN_SUCCESS));

        Bundle bundle = new Bundle();
        bundle.putString("email", email);
        LbryAnalytics.logEvent(LbryAnalytics.EVENT_EMAIL_VERIFIED, bundle);

        if (flow == VERIFICATION_FLOW_SIGN_IN) {
            final Intent resultIntent = new Intent();
            resultIntent.putExtra("flow", VERIFICATION_FLOW_SIGN_IN);
            resultIntent.putExtra("email", email);

            // only sign in required, don't do anything else
            showLoading();
            FetchCurrentUserTask task = new FetchCurrentUserTask(new FetchCurrentUserTask.FetchUserTaskHandler() {
                @Override
                public void onSuccess(User user) {
                    Lbryio.currentUser = user;
                    setResult(RESULT_OK, resultIntent);
                    finish();
                }

                @Override
                public void onError(Exception error) {
                    showFetchUserError(error.getMessage());
                    hideLoading();
                }
            });
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            // change pager view depending on flow
            showLoading();
            FetchCurrentUserTask task = new FetchCurrentUserTask(new FetchCurrentUserTask.FetchUserTaskHandler() {
                @Override
                public void onSuccess(User user) {
                    hideLoading();
                    findViewById(R.id.verification_close_button).setVisibility(View.VISIBLE);

                    Lbryio.currentUser = user;
                    ViewPager2 viewPager = findViewById(R.id.verification_pager);
                    // for rewards, (show phone verification if not done, or manual verification if required)
                    if (flow == VERIFICATION_FLOW_REWARDS) {
                        if (!user.isIdentityVerified()) {
                            // phone number verification required
                            viewPager.setCurrentItem(VerificationPagerAdapter.PAGE_VERIFICATION_PHONE, false);
                        } else if (!user.isRewardApproved()) {
                            // manual verification required
                            viewPager.setCurrentItem(VerificationPagerAdapter.PAGE_VERIFICATION_MANUAL, false);
                        } else {
                            // fully verified
                            setResult(RESULT_OK);
                            finish();
                        }
                    } else if (flow == VERIFICATION_FLOW_WALLET) {
                        // for wallet sync, if password unlock is required, show password entry page
                        viewPager.setCurrentItem(VerificationPagerAdapter.PAGE_VERIFICATION_WALLET, false);
                    }
                }
                @Override
                public void onError(Exception error) {
                    showFetchUserError(error.getMessage());
                    hideLoading();
                }
            });
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    @Override
    public void onPhoneAdded(String countryCode, String phoneNumber) {

    }

    @Override
    public void onPhoneVerified() {
        showLoading();
        FetchCurrentUserTask task = new FetchCurrentUserTask(new FetchCurrentUserTask.FetchUserTaskHandler() {
            @Override
            public void onSuccess(User user) {
                Lbryio.currentUser = user;
                if (user.isIdentityVerified() && user.isRewardApproved()) {
                    // verified for rewards
                    LbryAnalytics.logEvent(LbryAnalytics.EVENT_REWARD_ELIGIBILITY_COMPLETED);

                    setResult(RESULT_OK);
                    finish();
                    return;
                }

                // show manual verification page if the user is still not reward approved
                ViewPager2 viewPager = findViewById(R.id.verification_pager);
                viewPager.setCurrentItem(VerificationPagerAdapter.PAGE_VERIFICATION_MANUAL, false);
                hideLoading();
            }

            @Override
            public void onError(Exception error) {
                showFetchUserError(error.getMessage());
                hideLoading();
            }
        });
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void showFetchUserError(String message) {
        Snackbar.make(findViewById(R.id.verification_pager), message, Snackbar.LENGTH_LONG).
                setBackgroundTint(Color.RED).setTextColor(Color.WHITE).show();
    }

    @Override
    public void onManualVerifyContinue() {
        setResult(RESULT_OK);
        finish();
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
