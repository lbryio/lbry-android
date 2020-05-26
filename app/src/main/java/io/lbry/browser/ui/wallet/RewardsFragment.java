package io.lbry.browser.ui.wallet;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import java.text.DecimalFormat;
import java.util.List;

import io.lbry.browser.MainActivity;
import io.lbry.browser.R;
import io.lbry.browser.adapter.RewardListAdapter;
import io.lbry.browser.listener.SdkStatusListener;
import io.lbry.browser.model.lbryinc.Reward;
import io.lbry.browser.tasks.lbryinc.ClaimRewardTask;
import io.lbry.browser.tasks.lbryinc.FetchRewardsTask;
import io.lbry.browser.ui.BaseFragment;
import io.lbry.browser.utils.Helper;
import io.lbry.browser.utils.Lbry;
import io.lbry.browser.utils.LbryAnalytics;
import io.lbry.browser.utils.Lbryio;

public class RewardsFragment extends BaseFragment implements RewardListAdapter.RewardClickListener, SdkStatusListener {

    private boolean rewardClaimInProgress;
    private View layoutAccountDriver;
    private View layoutSdkInitializing;
    private View linkNotInterested;
    private TextView textAccountDriverTitle;
    private TextView textFreeCreditsWorth;
    private TextView textLearnMoreLink;
    private MaterialButton buttonGetStarted;

    private ProgressBar rewardsLoading;
    private RewardListAdapter adapter;
    private RecyclerView rewardList;
    private TextView linkFilterUnclaimed;
    private TextView linkFilterAll;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_rewards, container, false);

        layoutAccountDriver = root.findViewById(R.id.rewards_account_driver_container);
        layoutSdkInitializing = root.findViewById(R.id.container_sdk_initializing);
        linkNotInterested = root.findViewById(R.id.rewards_not_interested_link);
        textAccountDriverTitle = root.findViewById(R.id.rewards_account_driver_title);
        textFreeCreditsWorth = root.findViewById(R.id.rewards_account_driver_credits_worth);
        textLearnMoreLink = root.findViewById(R.id.rewards_account_driver_learn_more);
        buttonGetStarted = root.findViewById(R.id.rewards_get_started_button);

        linkFilterUnclaimed = root.findViewById(R.id.rewards_filter_link_unclaimed);
        linkFilterAll = root.findViewById(R.id.rewards_filter_link_all);
        rewardList = root.findViewById(R.id.rewards_list);
        rewardsLoading = root.findViewById(R.id.rewards_list_loading);

        Context context = getContext();
        LinearLayoutManager llm = new LinearLayoutManager(context);
        rewardList.setLayoutManager(llm);
        adapter = new RewardListAdapter(Lbryio.allRewards, context);
        adapter.setClickListener(this);
        adapter.setDisplayMode(RewardListAdapter.DISPLAY_MODE_UNCLAIMED);
        rewardList.setAdapter(adapter);

        initUi();

        return root;
    }


    public void onResume() {
        super.onResume();
        checkRewardsStatus();
        fetchRewards();

        Context context = getContext();
        if (context instanceof MainActivity) {
            MainActivity activity = (MainActivity) context;
            LbryAnalytics.setCurrentScreen(activity, "Rewards", "Rewards");
        }

        if (!Lbry.SDK_READY) {
            if (context instanceof MainActivity) {
                MainActivity activity = (MainActivity) context;
                activity.addSdkStatusListener(this);
            }
        } else {
            onSdkReady();
        }
    }

    public void onSdkReady() {
        Helper.setViewVisibility(layoutSdkInitializing, View.GONE);
    }

    public void onStart() {
        super.onStart();
        Context context = getContext();
        if (context instanceof MainActivity) {
            MainActivity activity = (MainActivity) context;
            activity.setWunderbarValue(null);
            activity.hideFloatingWalletBalance();
        }
    }

    public void onStop() {
        Context context = getContext();
        if (context instanceof MainActivity) {
            MainActivity activity = (MainActivity) context;
            activity.removeSdkStatusListener(this);
            activity.showFloatingWalletBalance();
        }
        super.onStop();
    }

    private void fetchRewards() {
        Helper.setViewVisibility(rewardList, View.INVISIBLE);
        FetchRewardsTask task = new FetchRewardsTask(rewardsLoading, new FetchRewardsTask.FetchRewardsHandler() {
            @Override
            public void onSuccess(List<Reward> rewards) {
                Lbryio.updateRewardsLists(rewards);
                updateUnclaimedRewardsValue();

                Context context = getContext();
                if (context instanceof MainActivity) {
                    ((MainActivity) context).showFloatingUnclaimedRewards();
                }

                if (adapter == null) {
                    adapter = new RewardListAdapter(rewards, getContext());
                    adapter.setClickListener(RewardsFragment.this);
                    adapter.setDisplayMode(RewardListAdapter.DISPLAY_MODE_UNCLAIMED);
                    rewardList.setAdapter(adapter);
                } else {
                    adapter.setRewards(rewards);
                }
                Helper.setViewVisibility(rewardList, View.VISIBLE);
            }

            @Override
            public void onError(Exception error) {
                // pass
                Helper.setViewVisibility(rewardList, View.VISIBLE);
            }
        });
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void initUi() {
        layoutSdkInitializing.setVisibility(Lbry.SDK_READY ? View.GONE : View.VISIBLE);

        linkNotInterested.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                Context context = getContext();

                if (context instanceof MainActivity) {
                    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
                    sp.edit().putBoolean(MainActivity.PREFERENCE_KEY_INTERNAL_REWARDS_NOT_INTERESTED, true).apply();

                    MainActivity activity = (MainActivity) context;
                    activity.hideFloatingRewardsValue();
                    activity.onBackPressed();
                }
            }
        });
        buttonGetStarted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Context context = getContext();
                if (context instanceof MainActivity) {
                    ((MainActivity) context).rewardsSignIn();
                }
            }
        });

        linkFilterAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                linkFilterUnclaimed.setTypeface(null, Typeface.NORMAL);
                linkFilterAll.setTypeface(null, Typeface.BOLD);
                adapter.setDisplayMode(RewardListAdapter.DISPLAY_MODE_ALL);
                if (adapter.getItemCount() == 1) {
                    fetchRewards();
                }
            }
        });
        linkFilterUnclaimed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                linkFilterUnclaimed.setTypeface(null, Typeface.BOLD);
                linkFilterAll.setTypeface(null, Typeface.NORMAL);
                adapter.setDisplayMode(RewardListAdapter.DISPLAY_MODE_UNCLAIMED);
                if (adapter.getItemCount() == 1) {
                    fetchRewards();
                }
            }
        });

        updateUnclaimedRewardsValue();
        layoutAccountDriver.setVisibility(Lbryio.currentUser != null && Lbryio.currentUser.isRewardApproved() ? View.GONE : View.VISIBLE);
        Helper.applyHtmlForTextView(textLearnMoreLink);
    }

    private void checkRewardsStatus() {
        Helper.setViewVisibility(layoutAccountDriver, Lbryio.currentUser != null && Lbryio.currentUser.isRewardApproved() ? View.GONE : View.VISIBLE);
    }

    public void updateUnclaimedRewardsValue() {
        try {
            String accountDriverTitle = getResources().getQuantityString(
                    R.plurals.available_credits,
                    Lbryio.totalUnclaimedRewardAmount == 1 ? 1 : 2,
                    Helper.shortCurrencyFormat(Lbryio.totalUnclaimedRewardAmount));
            double unclaimedRewardAmountUsd = Lbryio.totalUnclaimedRewardAmount * Lbryio.LBCUSDRate;
            Helper.setViewText(textAccountDriverTitle, accountDriverTitle);
            Helper.setViewText(textFreeCreditsWorth, getString(R.string.free_credits_worth, Helper.SIMPLE_CURRENCY_FORMAT.format(unclaimedRewardAmountUsd)));
        } catch (IllegalStateException ex) {
            // pass
        }

        Context context = getContext();
        if (context instanceof MainActivity) {
            ((MainActivity) context).updateRewardsUsdVale();
        }
    }

    @Override
    public void onRewardClicked(Reward reward, View loadingView) {
        if (rewardClaimInProgress || reward.isCustom()) {
            return;
        }
        claimReward(reward.getRewardType(), null, null, null, loadingView);
    }

    @Override
    public void onCustomClaimButtonClicked(String code, EditText inputCustomCode, MaterialButton buttonClaim, View loadingView) {
        if (rewardClaimInProgress) {
            return;
        }
        claimReward(Reward.TYPE_REWARD_CODE, code, inputCustomCode, buttonClaim, loadingView);
    }

    private void claimReward(String type, String code, EditText inputClaimCode, MaterialButton buttonClaim, View loadingView) {
        rewardClaimInProgress = true;
        Helper.setViewEnabled(buttonClaim, false);
        Helper.setViewEnabled(inputClaimCode, false);
        ClaimRewardTask task = new ClaimRewardTask(type, code, loadingView, getContext(), new ClaimRewardTask.ClaimRewardHandler() {
            @Override
            public void onSuccess(double amountClaimed, String message) {
                if (Helper.isNullOrEmpty(message)) {
                    message = getResources().getQuantityString(
                            R.plurals.claim_reward_message,
                            amountClaimed == 1 ? 1 : 2,
                            new DecimalFormat(Helper.LBC_CURRENCY_FORMAT_PATTERN).format(amountClaimed));
                }
                View view = getView();
                if (view != null) {
                    Snackbar.make(view, message, Snackbar.LENGTH_LONG).show();
                }
                Helper.setViewEnabled(buttonClaim, true);
                Helper.setViewEnabled(inputClaimCode, true);
                rewardClaimInProgress = false;

                fetchRewards();
            }

            @Override
            public void onError(Exception error) {
                View view = getView();
                if (view != null && error != null && !Helper.isNullOrEmpty(error.getMessage())) {
                    Snackbar.make(view, error.getMessage(), Snackbar.LENGTH_LONG).setBackgroundTint(Color.RED).setTextColor(Color.WHITE).show();
                }
                Helper.setViewEnabled(buttonClaim, true);
                Helper.setViewEnabled(inputClaimCode, true);
                rewardClaimInProgress = false;
            }
        });
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}
