package io.lbry.browser;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.lbry.browser.adapter.VerificationPagerAdapter;
import io.lbry.browser.listener.SignInListener;
import io.lbry.browser.listener.WalletSyncListener;
import io.lbry.browser.model.lbryinc.RewardVerified;
import io.lbry.browser.model.lbryinc.User;
import io.lbry.browser.tasks.RewardVerifiedHandler;
import io.lbry.browser.tasks.lbryinc.FetchCurrentUserTask;
import io.lbry.browser.utils.Helper;
import io.lbry.browser.utils.LbryAnalytics;
import io.lbry.browser.utils.Lbryio;
import io.lbry.lbrysdk.LbrynetService;

public class VerificationActivity extends FragmentActivity implements SignInListener, WalletSyncListener {

    public static final int VERIFICATION_FLOW_SIGN_IN = 1;
    public static final int VERIFICATION_FLOW_REWARDS = 2;
    public static final int VERIFICATION_FLOW_WALLET = 3;

    private BillingClient billingClient;
    private BroadcastReceiver sdkReceiver;
    private String email;
    private boolean signedIn;
    private int flow;

    private PurchasesUpdatedListener purchasesUpdatedListener = new PurchasesUpdatedListener() {
        @Override
        public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> purchases) {
            int responseCode = billingResult.getResponseCode();
            if (responseCode == BillingClient.BillingResponseCode.OK && purchases != null)
            {
                for (Purchase purchase : purchases) {
                    if (MainActivity.SKU_SKIP.equalsIgnoreCase(purchase.getSku())) {
                        showLoading();
                        MainActivity.handleBillingPurchase(
                                purchase,
                                billingClient,
                                VerificationActivity.this, null, new RewardVerifiedHandler() {
                                    @Override
                                    public void onSuccess(RewardVerified rewardVerified) {
                                        if (Lbryio.currentUser != null) {
                                            Lbryio.currentUser.setRewardApproved(rewardVerified.isRewardApproved());
                                        }

                                        if (!rewardVerified.isRewardApproved()) {
                                            // show pending purchase message (possible slow card tx)
                                            Snackbar.make(findViewById(R.id.verification_pager), R.string.purchase_request_pending, Snackbar.LENGTH_LONG).show();
                                        } else  {
                                            Snackbar.make(findViewById(R.id.verification_pager), R.string.reward_verification_successful, Snackbar.LENGTH_LONG).show();
                                        }

                                        setResult(RESULT_OK);
                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                finish();
                                            }
                                        }, 3000);
                                    }

                                    @Override
                                    public void onError(Exception error) {
                                        showFetchUserError(getString(R.string.purchase_request_failed_error));
                                        hideLoading();
                                    }
                                });
                    }
                }
            }
        }
    };

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

        IntentFilter filter = new IntentFilter();
        filter.addAction(LbrynetService.ACTION_STOP_SERVICE);
        sdkReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (LbrynetService.ACTION_STOP_SERVICE.equals(action)) {
                    finish();
                }
            }
        };
        registerReceiver(sdkReceiver, filter);

        billingClient = BillingClient.newBuilder(this)
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases()
                .build();
        establishBillingClientConnection();

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

    private void establishBillingClientConnection() {
        if (billingClient != null) {
            billingClient.startConnection(new BillingClientStateListener() {
                @Override
                public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        // no need to do anything here. purchases are always checked server-side
                    }
                }

                @Override
                public void onBillingServiceDisconnected() {
                    establishBillingClientConnection();
                }
            });
        }
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
                // disable phone verification for now
                /*if (!user.isIdentityVerified()) {
                    // phone number verification required
                    viewPager.setCurrentItem(VerificationPagerAdapter.PAGE_VERIFICATION_PHONE, false);
                    flowHandled = true;
                } else */
                if (!user.isRewardApproved()) {
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
            FetchCurrentUserTask task = new FetchCurrentUserTask(this, new FetchCurrentUserTask.FetchUserTaskHandler() {
                @Override
                public void onSuccess(User user) {
                    Lbryio.currentUser = user;
                    setResult(RESULT_OK, resultIntent);
                    finish();
                }

                @Override
                public void onError(Exception error) {
                    showFetchUserError(error != null ? error.getMessage() : getString(R.string.fetch_current_user_error));
                    hideLoading();
                }
            });
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            // change pager view depending on flow
            showLoading();
            FetchCurrentUserTask task = new FetchCurrentUserTask(this, new FetchCurrentUserTask.FetchUserTaskHandler() {
                @Override
                public void onSuccess(User user) {
                    hideLoading();
                    findViewById(R.id.verification_close_button).setVisibility(View.VISIBLE);

                    Lbryio.currentUser = user;
                    ViewPager2 viewPager = findViewById(R.id.verification_pager);
                    // for rewards, (show phone verification if not done, or manual verification if required)
                    if (flow == VERIFICATION_FLOW_REWARDS) {
                        // skipping phone verification
                        /*if (!user.isIdentityVerified()) {
                            // phone number verification required
                            viewPager.setCurrentItem(VerificationPagerAdapter.PAGE_VERIFICATION_PHONE, false);
                        } else
                        */
                        if (!user.isRewardApproved()) {

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
                    showFetchUserError(error != null ? error.getMessage() : getString(R.string.fetch_current_user_error));
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
        FetchCurrentUserTask task = new FetchCurrentUserTask(this, new FetchCurrentUserTask.FetchUserTaskHandler() {
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
                showFetchUserError(error != null ? error.getMessage() : getString(R.string.fetch_current_user_error));
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

    @Override
    public void onSkipQueueAction() {
        if (billingClient != null) {
            List<String> skuList = new ArrayList<>();
            skuList.add(MainActivity.SKU_SKIP);

            SkuDetailsParams detailsParams = SkuDetailsParams.newBuilder().
                    setType(BillingClient.SkuType.INAPP).
                    setSkusList(skuList).build();
            billingClient.querySkuDetailsAsync(detailsParams, new SkuDetailsResponseListener() {
                @Override
                public void onSkuDetailsResponse(@NonNull BillingResult billingResult, @Nullable List<SkuDetails> list) {
                    if (list != null && list.size() > 0) {
                        // we only queried one product, so it should be the first item in the list
                        SkuDetails skuDetails = list.get(0);

                        // launch the billing flow for skip queue
                        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder().
                                setSkuDetails(skuDetails).build();
                        billingClient.launchBillingFlow(VerificationActivity.this, billingFlowParams);
                    }
                }
            });
        }
    }

    @Override
    public void onTwitterVerified() {
        Snackbar.make(findViewById(R.id.verification_pager), R.string.reward_verification_successful, Snackbar.LENGTH_LONG).show();

        setResult(RESULT_OK);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 3000);
    }

    @Override
    public void onManualProgress(boolean progress) {
        if (progress) {
            findViewById(R.id.verification_close_button).setVisibility(View.GONE);
        } else {
            findViewById(R.id.verification_close_button).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroy() {
        Helper.unregisterReceiver(sdkReceiver, this);
        super.onDestroy();
    }
}
