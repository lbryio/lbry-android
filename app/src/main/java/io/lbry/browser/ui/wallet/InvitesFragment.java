package io.lbry.browser.ui.wallet;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import io.lbry.browser.BuildConfig;
import io.lbry.browser.MainActivity;
import io.lbry.browser.R;
import io.lbry.browser.adapter.InlineChannelSpinnerAdapter;
import io.lbry.browser.adapter.InviteeListAdapter;
import io.lbry.browser.listener.SdkStatusListener;
import io.lbry.browser.listener.WalletBalanceListener;
import io.lbry.browser.model.Claim;
import io.lbry.browser.model.WalletBalance;
import io.lbry.browser.model.lbryinc.Invitee;
import io.lbry.browser.tasks.claim.ClaimListResultHandler;
import io.lbry.browser.tasks.claim.ClaimListTask;
import io.lbry.browser.tasks.GenericTaskHandler;
import io.lbry.browser.tasks.claim.ChannelCreateUpdateTask;
import io.lbry.browser.tasks.claim.ClaimResultHandler;
import io.lbry.browser.tasks.lbryinc.LogPublishTask;
import io.lbry.browser.tasks.lbryinc.FetchInviteStatusTask;
import io.lbry.browser.tasks.lbryinc.FetchReferralCodeTask;
import io.lbry.browser.tasks.lbryinc.InviteByEmailTask;
import io.lbry.browser.ui.BaseFragment;
import io.lbry.browser.utils.Helper;
import io.lbry.browser.utils.Lbry;
import io.lbry.browser.utils.LbryAnalytics;
import io.lbry.browser.utils.LbryUri;
import io.lbry.browser.utils.Lbryio;

public class InvitesFragment extends BaseFragment implements SdkStatusListener, WalletBalanceListener {

    private static final String INVITE_LINK_FORMAT = "https://lbry.tv/$/invite/%s:%s";

    private boolean fetchingChannels;
    private View layoutAccountDriver;
    private View layoutSdkInitializing;
    private TextView textLearnMoreLink;
    private MaterialButton buttonGetStarted;

    private View buttonCopyInviteLink;
    private TextView textInviteLink;
    private TextInputLayout layoutInputEmail;
    private TextInputEditText inputEmail;
    private MaterialButton buttonInviteByEmail;

    private RecyclerView inviteHistoryList;
    private InviteeListAdapter inviteHistoryAdapter;
    private InlineChannelSpinnerAdapter channelSpinnerAdapter;
    private AppCompatSpinner channelSpinner;
    private View progressLoadingChannels;
    private View progressLoadingInviteByEmail;
    private View progressLoadingStatus;

    private CardView rewardDriverCard;
    private View inlineChannelCreator;
    private TextInputEditText inlineChannelCreatorInputName;
    private TextInputEditText inlineChannelCreatorInputDeposit;
    private View inlineChannelCreatorInlineBalance;
    private TextView inlineChannelCreatorInlineBalanceValue;
    private View inlineChannelCreatorCancelLink;
    private View inlineChannelCreatorProgress;
    private MaterialButton inlineChannelCreatorCreateButton;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_invites, container, false);

        layoutAccountDriver = root.findViewById(R.id.invites_account_driver_container);
        layoutSdkInitializing = root.findViewById(R.id.container_sdk_initializing);
        textLearnMoreLink = root.findViewById(R.id.invites_account_driver_learn_more);
        buttonGetStarted = root.findViewById(R.id.invites_get_started_button);
        rewardDriverCard = root.findViewById(R.id.reward_driver_card);

        textInviteLink = root.findViewById(R.id.invites_invite_link);
        buttonCopyInviteLink = root.findViewById(R.id.invites_copy_invite_link);
        layoutInputEmail = root.findViewById(R.id.invites_email_input_layout);
        inputEmail = root.findViewById(R.id.invites_email_input);
        buttonInviteByEmail = root.findViewById(R.id.invites_email_button);

        progressLoadingChannels = root.findViewById(R.id.invites_loading_channels_progress);
        progressLoadingInviteByEmail = root.findViewById(R.id.invites_loading_invite_by_email_progress);
        progressLoadingStatus = root.findViewById(R.id.invites_loading_status_progress);

        inviteHistoryList = root.findViewById(R.id.invite_history_list);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        inviteHistoryList.setLayoutManager(llm);

        channelSpinner = root.findViewById(R.id.invites_channel_spinner);

        inlineChannelCreator = root.findViewById(R.id.container_inline_channel_form_create);
        inlineChannelCreatorInputName = root.findViewById(R.id.inline_channel_form_input_name);
        inlineChannelCreatorInputDeposit = root.findViewById(R.id.inline_channel_form_input_deposit);
        inlineChannelCreatorInlineBalance = root.findViewById(R.id.inline_channel_form_inline_balance_container);
        inlineChannelCreatorInlineBalanceValue = root.findViewById(R.id.inline_channel_form_inline_balance_value);
        inlineChannelCreatorProgress = root.findViewById(R.id.inline_channel_form_create_progress);
        inlineChannelCreatorCancelLink = root.findViewById(R.id.inline_channel_form_cancel_link);
        inlineChannelCreatorCreateButton = root.findViewById(R.id.inline_channel_form_create_button);

        initUi();

        return root;
    }

    private void initUi() {
        layoutAccountDriver.setVisibility(Lbryio.isSignedIn() ? View.GONE : View.VISIBLE);
        layoutSdkInitializing.setVisibility(Lbry.SDK_READY ? View.GONE : View.VISIBLE);
        Helper.applyHtmlForTextView(textLearnMoreLink);

        rewardDriverCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Context context = getContext();
                if (context instanceof MainActivity) {
                    ((MainActivity) context).openRewards();
                }
            }
        });

        inputEmail.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                layoutInputEmail.setHint(hasFocus ? getString(R.string.email) :
                        Helper.getValue(inputEmail.getText()).length() > 0 ?
                                getString(R.string.email) : getString(R.string.invite_email_placeholder));
            }
        });
        inputEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Helper.setViewEnabled(buttonInviteByEmail, charSequence.length() > 0);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        buttonInviteByEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = Helper.getValue(inputEmail.getText());
                if (email.indexOf("@") == -1) {
                    showError(getString(R.string.provide_valid_email));
                    return;
                }

                InviteByEmailTask task = new InviteByEmailTask(email, progressLoadingInviteByEmail, new GenericTaskHandler() {
                    @Override
                    public void beforeStart() {
                        Helper.setViewEnabled(buttonInviteByEmail, false);
                    }

                    @Override
                    public void onSuccess() {
                        Snackbar.make(getView(), getString(R.string.invite_sent_to, email), Snackbar.LENGTH_LONG).show();
                        Helper.setViewText(inputEmail, null);
                        Helper.setViewEnabled(buttonInviteByEmail, true);
                        fetchInviteStatus();
                    }

                    @Override
                    public void onError(Exception error) {
                        showError(error.getMessage());
                        Helper.setViewEnabled(buttonInviteByEmail, true);
                    }
                });
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });

        buttonGetStarted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Context context = getContext();
                if (context instanceof MainActivity) {
                    ((MainActivity) context).simpleSignIn();
                }
            }
        });

        channelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                Object item = adapterView.getItemAtPosition(position);
                if (item instanceof Claim) {
                    Claim claim = (Claim) item;
                    if (claim.isPlaceholder()) {
                        if (!fetchingChannels) {
                            showInlineChannelCreator();
                        }
                    } else {
                        hideInlineChannelCreator();
                        // build invite link
                        updateInviteLink(claim);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        textInviteLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                copyInviteLink();
            }
        });
        buttonCopyInviteLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                copyInviteLink();
            }
        });

        setupInlineChannelCreator(
                inlineChannelCreator,
                inlineChannelCreatorInputName,
                inlineChannelCreatorInputDeposit,
                inlineChannelCreatorInlineBalance,
                inlineChannelCreatorInlineBalanceValue,
                inlineChannelCreatorCancelLink,
                inlineChannelCreatorCreateButton,
                inlineChannelCreatorProgress
        );
    }

    private void updateInviteLink(Claim claim) {
        LbryUri canonical = LbryUri.tryParse(claim.getCanonicalUrl());
        String link = String.format(INVITE_LINK_FORMAT,
                canonical != null ? String.format("@%s", canonical.getChannelName()) : claim.getName(),
                canonical != null ? canonical.getChannelClaimId() : claim.getClaimId());
        textInviteLink.setText(link);
    }
    private void copyInviteLink() {
        Context context = getContext();
        if (context != null && textInviteLink != null) {
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData data = ClipData.newPlainText("inviteLink", textInviteLink.getText());
            clipboard.setPrimaryClip(data);
        }
        Snackbar.make(getView(), R.string.invite_link_copied, Snackbar.LENGTH_SHORT).show();
    }

    private void updateChannelList(List<Claim> channels) {
        if (channelSpinnerAdapter == null) {
            Context context = getContext();
            channelSpinnerAdapter = new InlineChannelSpinnerAdapter(context, R.layout.spinner_item_channel, new ArrayList<>(channels));
            channelSpinnerAdapter.addPlaceholder(false);
            channelSpinnerAdapter.notifyDataSetChanged();
        } else {
            channelSpinnerAdapter.clear();
            channelSpinnerAdapter.addAll(channels);
            channelSpinnerAdapter.addPlaceholder(false);
            channelSpinnerAdapter.notifyDataSetChanged();
        }

        if (channelSpinner != null) {
            channelSpinner.setAdapter(channelSpinnerAdapter);
        }

        if (channelSpinnerAdapter.getCount() > 1) {
            channelSpinner.setSelection(1);
        }
    }

    public void onResume() {
        super.onResume();
        layoutAccountDriver.setVisibility(Lbryio.isSignedIn() ? View.GONE : View.VISIBLE);
        checkRewardsDriver();

        Context context = getContext();
        if (context instanceof MainActivity) {
            MainActivity activity = (MainActivity) context;
            LbryAnalytics.setCurrentScreen(activity, "Invites", "Invites");
        }

        fetchInviteStatus();
        if (!Lbry.SDK_READY) {
            if (context instanceof MainActivity) {
                MainActivity activity = (MainActivity) context;
                activity.addSdkStatusListener(this);
                activity.addWalletBalanceListener(this);
            }
        } else {
            onSdkReady();
        }
    }

    public void onSdkReady() {
        Helper.setViewVisibility(layoutSdkInitializing, View.GONE);
        fetchChannels();
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

    public void clearInputFocus() {
        inputEmail.clearFocus();
        inlineChannelCreatorInputName.clearFocus();
        inlineChannelCreatorInputDeposit.clearFocus();
    }

    public void onStop() {
        clearInputFocus();

        Context context = getContext();
        if (context instanceof MainActivity) {
            MainActivity activity = (MainActivity) context;
            activity.removeSdkStatusListener(this);
            activity.removeWalletBalanceListener(this);
            activity.showFloatingWalletBalance();
        }
        super.onStop();
    }

    private void showInlineChannelCreator() {
        Helper.setViewVisibility(inlineChannelCreator, View.VISIBLE);
    }
    private void hideInlineChannelCreator() {
        Helper.setViewVisibility(inlineChannelCreator, View.GONE);
    }

    private void fetchDefaultInviteLink() {
        FetchReferralCodeTask task = new FetchReferralCodeTask(null, new FetchReferralCodeTask.FetchReferralCodeHandler() {
            @Override
            public void onSuccess(String referralCode) {
                String previousLink = Helper.getValue(textInviteLink.getText());
                if (Helper.isNullOrEmpty(previousLink)) {
                    Helper.setViewText(textInviteLink, String.format("https://lbry.tv/$/invite/%s", referralCode));
                }
            }

            @Override
            public void onError(Exception error) {
                // pass
            }
        });
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void disableChannelSpinner() {
        Helper.setViewEnabled(channelSpinner, false);
        hideInlineChannelCreator();
    }
    private void enableChannelSpinner() {
        Helper.setViewEnabled(channelSpinner, true);
        Claim selectedClaim = (Claim) channelSpinner.getSelectedItem();
        if (selectedClaim != null) {
            if (selectedClaim.isPlaceholder()) {
                showInlineChannelCreator();
            } else {
                hideInlineChannelCreator();
            }
        }
    }

    private void fetchChannels() {
        if (Lbry.ownChannels != null && Lbry.ownChannels.size() > 0) {
            updateChannelList(Lbry.ownChannels);
            return;
        }

        fetchingChannels = true;
        disableChannelSpinner();
        ClaimListTask task = new ClaimListTask(Claim.TYPE_CHANNEL, progressLoadingChannels, new ClaimListResultHandler() {
            @Override
            public void onSuccess(List<Claim> claims) {
                Lbry.ownChannels = new ArrayList<>(claims);
                updateChannelList(Lbry.ownChannels);
                if (Lbry.ownChannels == null || Lbry.ownChannels.size() == 0) {
                    fetchDefaultInviteLink();
                }
                enableChannelSpinner();
                fetchingChannels = false;
            }

            @Override
            public void onError(Exception error) {
                fetchDefaultInviteLink();
                enableChannelSpinner();
                fetchingChannels = false;
            }
        });
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void fetchInviteStatus() {
        FetchInviteStatusTask task = new FetchInviteStatusTask(progressLoadingStatus, new FetchInviteStatusTask.FetchInviteStatusHandler() {
            @Override
            public void onSuccess(List<Invitee> invitees) {
                if (inviteHistoryAdapter == null) {
                    inviteHistoryAdapter = new InviteeListAdapter(invitees, getContext());
                    inviteHistoryAdapter.addHeader();
                } else {
                    inviteHistoryAdapter.addInvitees(invitees);
                }
                if (inviteHistoryList != null) {
                    inviteHistoryList.setAdapter(inviteHistoryAdapter);
                }
                Helper.setViewVisibility(inviteHistoryList,
                        inviteHistoryAdapter == null || inviteHistoryAdapter.getItemCount() < 2 ? View.GONE : View.VISIBLE
                );
            }

            @Override
            public void onError(Exception error) {
                Helper.setViewVisibility(inviteHistoryList,
                        inviteHistoryAdapter == null || inviteHistoryAdapter.getItemCount() < 2 ? View.GONE : View.VISIBLE
                );
            }
        });
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void setupInlineChannelCreator(
            View container,
            TextInputEditText inputChannelName,
            TextInputEditText inputDeposit,
            View inlineBalanceView,
            TextView inlineBalanceValue,
            View linkCancel,
            MaterialButton buttonCreate,
            View progressView) {
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
                        channelSpinnerAdapter.add(claimResult);
                        channelSpinner.setSelection(channelSpinnerAdapter.getCount() - 1);

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

    @Override
    public void onWalletBalanceUpdated(WalletBalance walletBalance) {
        if (walletBalance != null && inlineChannelCreatorInlineBalanceValue != null) {
            inlineChannelCreatorInlineBalanceValue.setText(Helper.shortCurrencyFormat(walletBalance.getAvailable().doubleValue()));
        }
        checkRewardsDriver();
    }

    private void showError(String message) {
        Context context = getContext();
        if (context != null) {
            Snackbar.make(getView(), message, Snackbar.LENGTH_LONG).
                    setBackgroundTint(Color.RED).setTextColor(Color.WHITE).show();
        }
    }

    private void checkRewardsDriver() {
        Context ctx = getContext();
        View root = getView();
        if (ctx != null && root != null) {
            Helper.setViewText(root.findViewById(R.id.reward_driver_text), R.string.earn_credits_for_inviting);
        }
    }
}
