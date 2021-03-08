package io.lbry.browser.ui;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatSpinner;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.math.BigDecimal;
import java.util.Map;

import io.lbry.browser.BuildConfig;
import io.lbry.browser.MainActivity;
import io.lbry.browser.R;
import io.lbry.browser.adapter.InlineChannelSpinnerAdapter;
import io.lbry.browser.model.Claim;
import io.lbry.browser.tasks.claim.ChannelCreateUpdateTask;
import io.lbry.browser.tasks.claim.ClaimResultHandler;
import io.lbry.browser.tasks.lbryinc.LogPublishTask;
import io.lbry.browser.utils.Helper;
import io.lbry.browser.utils.Lbry;
import io.lbry.browser.utils.LbryAnalytics;
import io.lbry.browser.utils.LbryUri;
import lombok.Getter;
import lombok.Setter;

public class BaseFragment extends Fragment {
    @Getter
    @Setter
    private Map<String, Object> params;
    private boolean rewardDriverClickListenerSet;

    public boolean shouldHideGlobalPlayer() {
        return false;
    }

    public boolean shouldSuspendGlobalPlayer() {
        return false;
    }

    public void onStart() {
        super.onStart();
        if (shouldSuspendGlobalPlayer()) {
            Context context = getContext();
            if (context instanceof MainActivity) {
                MainActivity.suspendGlobalPlayer(context);
            }
        }
    }

    public void onStop() {
        if (shouldSuspendGlobalPlayer()) {
            Context context = getContext();
            if (context instanceof MainActivity) {
                MainActivity.resumeGlobalPlayer(context);
            }
        }

        if (params != null && params.containsKey("source") && "notification".equalsIgnoreCase(params.get("source").toString())) {
            Context context = getContext();
            if (context instanceof MainActivity) {
                ((MainActivity) context).navigateBackToNotifications();
            }
        }

        rewardDriverClickListenerSet = false;
        super.onStop();
    }

    public void onResume() {
        super.onResume();
        Context context = getContext();
        if (context instanceof MainActivity) {
            MainActivity activity = (MainActivity) context;
            activity.setSelectedMenuItemForFragment(this);

            if (shouldHideGlobalPlayer()) {
                activity.hideGlobalNowPlaying();
            } else {
                activity.checkNowPlaying();
            }
        }
    }

    public void checkRewardsDriverCard(String rewardDriverText, double minCost) {
        View root = getView();
        if (root != null) {
            View rewardDriverCard = root.findViewById(R.id.reward_driver_card);
            if (rewardDriverCard != null) {
                if (!rewardDriverClickListenerSet) {
                    rewardDriverCard.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Context context = getContext();
                            if (context instanceof MainActivity) {
                                ((MainActivity) context).openRewards();
                            }
                        }
                    });
                    rewardDriverClickListenerSet = true;
                }

                // only apply to fragments that have the card present
                ((TextView) rewardDriverCard.findViewById(R.id.reward_driver_text)).setText(rewardDriverText);
                boolean showRewardsDriver = Lbry.walletBalance == null ||
                        minCost == 0 && Lbry.walletBalance.getAvailable().doubleValue() == 0 |
                        Lbry.walletBalance.getAvailable().doubleValue() < Math.max(minCost, Helper.MIN_DEPOSIT);
                rewardDriverCard.setVisibility(showRewardsDriver ? View.VISIBLE : View.GONE);
            }
        }
    }

    public void showError(String message) {
        Context context = getContext();
        if (context != null) {
            Snackbar.make(getView(), message, Snackbar.LENGTH_LONG).
                    setBackgroundTint(Color.RED).setTextColor(Color.WHITE).show();
        }
    }

    public void setupInlineChannelCreator(
            View container,
            TextInputEditText inputChannelName,
            TextInputEditText inputDeposit,
            View inlineBalanceView,
            TextView inlineBalanceValue,
            View linkCancel,
            MaterialButton buttonCreate,
            View progressView,
            AppCompatSpinner channelSpinner,
            InlineChannelSpinnerAdapter channelSpinnerAdapter) {
        inputDeposit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                Helper.setViewVisibility(inlineBalanceView, hasFocus ? View.VISIBLE : View.INVISIBLE);
            }
        });

        linkCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Helper.setViewText(inputChannelName, null);
                Helper.setViewText(inputDeposit, null);
                Helper.setViewVisibility(container, View.GONE);
            }
        });

        buttonCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // validate deposit and channel name
                String channelNameString = Helper.normalizeChannelName(Helper.getValue(inputChannelName.getText()));
                Claim claimToSave = new Claim();
                claimToSave.setName(channelNameString);
                String channelName = claimToSave.getName().startsWith("@") ? claimToSave.getName().substring(1) : claimToSave.getName();
                String depositString = Helper.getValue(inputDeposit.getText());
                if ("@".equals(channelName) || Helper.isNullOrEmpty(channelName)) {
                    showError(getString(R.string.please_enter_channel_name));
                    return;
                }
                if (!LbryUri.isNameValid(channelName)) {
                    showError(getString(R.string.channel_name_invalid_characters));
                    return;
                }
                if (Helper.channelExists(channelName)) {
                    showError(getString(R.string.channel_name_already_created));
                    return;
                }

                double depositAmount = 0;
                try {
                    depositAmount = Double.valueOf(depositString);
                } catch (NumberFormatException ex) {
                    // pass
                    showError(getString(R.string.please_enter_valid_deposit));
                    return;
                }
                if (depositAmount == 0) {
                    String error = getResources().getQuantityString(R.plurals.min_deposit_required, depositAmount == 1 ? 1 : 2, String.valueOf(Helper.MIN_DEPOSIT));
                    showError(error);
                    return;
                }
                if (Lbry.walletBalance == null || Lbry.walletBalance.getAvailable().doubleValue() < depositAmount) {
                    showError(getString(R.string.deposit_more_than_balance));
                    return;
                }

                ChannelCreateUpdateTask task =  new ChannelCreateUpdateTask(
                        claimToSave, new BigDecimal(depositString), false, progressView, new ClaimResultHandler() {
                    @Override
                    public void beforeStart() {
                        Helper.setViewEnabled(inputChannelName, false);
                        Helper.setViewEnabled(inputDeposit, false);
                        Helper.setViewEnabled(buttonCreate, false);
                        Helper.setViewEnabled(linkCancel, false);
                    }

                    @Override
                    public void onSuccess(Claim claimResult) {
                        if (!BuildConfig.DEBUG) {
                            LogPublishTask logPublishTask = new LogPublishTask(claimResult);
                            logPublishTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        }

                        // channel created
                        Bundle bundle = new Bundle();
                        bundle.putString("claim_id", claimResult.getClaimId());
                        bundle.putString("claim_name", claimResult.getName());
                        LbryAnalytics.logEvent(LbryAnalytics.EVENT_CHANNEL_CREATE, bundle);

                        // add the claim to the channel list and set it as the selected item
                        if (channelSpinnerAdapter != null) {
                            channelSpinnerAdapter.add(claimResult);
                        }
                        if (channelSpinner != null && channelSpinnerAdapter != null) {
                            channelSpinner.setSelection(channelSpinnerAdapter.getCount() - 1);
                        }

                        Helper.setViewEnabled(inputChannelName, true);
                        Helper.setViewEnabled(inputDeposit, true);
                        Helper.setViewEnabled(buttonCreate, true);
                        Helper.setViewEnabled(linkCancel, true);
                    }

                    @Override
                    public void onError(Exception error) {
                        Helper.setViewEnabled(inputChannelName, true);
                        Helper.setViewEnabled(inputDeposit, true);
                        Helper.setViewEnabled(buttonCreate, true);
                        Helper.setViewEnabled(linkCancel, true);
                        showError(error.getMessage());
                    }
                });
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });

        Helper.setViewText(inlineBalanceValue, Helper.shortCurrencyFormat(Lbry.walletBalance.getAvailable().doubleValue()));
    }
}
