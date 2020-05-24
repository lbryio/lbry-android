package io.lbry.browser.ui.wallet;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

import io.lbry.browser.MainActivity;
import io.lbry.browser.R;
import io.lbry.browser.adapter.TransactionListAdapter;
import io.lbry.browser.listener.SdkStatusListener;
import io.lbry.browser.listener.WalletBalanceListener;
import io.lbry.browser.model.NavMenuItem;
import io.lbry.browser.model.Transaction;
import io.lbry.browser.model.WalletBalance;
import io.lbry.browser.tasks.wallet.TransactionListTask;
import io.lbry.browser.tasks.wallet.WalletAddressUnusedTask;
import io.lbry.browser.tasks.wallet.WalletSendTask;
import io.lbry.browser.ui.BaseFragment;
import io.lbry.browser.ui.publish.PublishFragment;
import io.lbry.browser.utils.Helper;
import io.lbry.browser.utils.Lbry;
import io.lbry.browser.utils.LbryAnalytics;
import io.lbry.browser.utils.LbryUri;
import io.lbry.browser.utils.Lbryio;

public class WalletFragment extends BaseFragment implements SdkStatusListener, WalletBalanceListener {

    private View layoutAccountRecommended;
    private View layoutSdkInitializing;
    private View linkSkipAccount;
    private TextView textWalletBalance;
    private TextView textWalletBalanceUSD;
    private TextView textTipsBalance;
    private TextView textTipsBalanceUSD;
    private TextView textClaimsBalance;
    private TextView textSupportsBalance;
    private ProgressBar walletSendProgress;

    private TextView linkUnlockTips;
    private ProgressBar progressUnlockTips;

    private View loadingRecentContainer;
    private View inlineBalanceContainer;
    private TextView textWalletInlineBalance;
    private MaterialButton buttonSignUp;
    private RecyclerView recentTransactionsList;
    private View linkViewAll;
    private TextView textConvertCredits;
    private TextView textConvertCreditsBittrex;
    private TextView textEarnMoreTips;
    private TextView textWhatSyncMeans;
    private TextView textWalletReceiveAddress;
    private TextView textWalletHintSyncStatus;
    private ImageButton buttonCopyReceiveAddress;
    private MaterialButton buttonGetNewAddress;
    private TextInputEditText inputSendAddress;
    private TextInputEditText inputSendAmount;
    private MaterialButton buttonSend;
    private TextView textConnectedEmail;
    private SwitchMaterial switchSyncStatus;
    private TextView linkManualBackup;
    private TextView linkSyncFAQ;
    private TextView textNoRecentTransactions;

    private boolean hasFetchedRecentTransactions = false;
    private TransactionListAdapter recentTransactionsAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_wallet, container, false);

        loadingRecentContainer = root.findViewById(R.id.wallet_loading_recent_container);
        layoutAccountRecommended = root.findViewById(R.id.wallet_account_recommended_container);
        layoutSdkInitializing = root.findViewById(R.id.container_sdk_initializing);
        linkSkipAccount = root.findViewById(R.id.wallet_skip_account_link);
        buttonSignUp = root.findViewById(R.id.wallet_sign_up_button);

        inlineBalanceContainer = root.findViewById(R.id.wallet_inline_balance_container);
        textWalletInlineBalance = root.findViewById(R.id.wallet_inline_balance_value);
        walletSendProgress = root.findViewById(R.id.wallet_send_progress);

        textWalletBalance = root.findViewById(R.id.wallet_balance_value);
        textWalletBalanceUSD = root.findViewById(R.id.wallet_balance_usd_value);
        textTipsBalance = root.findViewById(R.id.wallet_balance_tips);
        textTipsBalanceUSD = root.findViewById(R.id.wallet_balance_tips_usd_value);
        textClaimsBalance = root.findViewById(R.id.wallet_balance_staked_publishes);
        textSupportsBalance = root.findViewById(R.id.wallet_balance_staked_supports);
        textWalletHintSyncStatus = root.findViewById(R.id.wallet_hint_sync_status);

        linkUnlockTips = root.findViewById(R.id.wallet_unlock_tips_link);
        progressUnlockTips = root.findViewById(R.id.wallet_unlock_tips_progress);

        recentTransactionsList = root.findViewById(R.id.wallet_recent_transactions_list);
        linkViewAll = root.findViewById(R.id.wallet_link_view_all);
        textNoRecentTransactions = root.findViewById(R.id.wallet_no_recent_transactions);
        textConvertCredits = root.findViewById(R.id.wallet_hint_convert_credits);
        textConvertCreditsBittrex = root.findViewById(R.id.wallet_hint_convert_credits_bittrex);
        textEarnMoreTips = root.findViewById(R.id.wallet_hint_earn_more_tips);
        textWhatSyncMeans = root.findViewById(R.id.wallet_hint_what_sync_means);
        textWalletReceiveAddress = root.findViewById(R.id.wallet_receive_address);
        buttonCopyReceiveAddress = root.findViewById(R.id.wallet_copy_receive_address);
        buttonGetNewAddress = root.findViewById(R.id.wallet_get_new_address);
        inputSendAddress = root.findViewById(R.id.wallet_input_send_address);
        inputSendAmount = root.findViewById(R.id.wallet_input_amount);
        buttonSend = root.findViewById(R.id.wallet_send);
        textConnectedEmail = root.findViewById(R.id.wallet_connected_email);
        switchSyncStatus = root.findViewById(R.id.wallet_switch_sync_status);
        linkManualBackup = root.findViewById(R.id.wallet_link_manual_backup);
        linkSyncFAQ = root.findViewById(R.id.wallet_link_sync_faq);

        initUi();

        return root;
    }

    private void copyReceiveAddress() {
        Context context = getContext();
        if (context != null && textWalletReceiveAddress != null) {
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData data = ClipData.newPlainText("address", textWalletReceiveAddress.getText());
            clipboard.setPrimaryClip(data);
        }
        Snackbar.make(getView(), R.string.address_copied, Snackbar.LENGTH_SHORT).show();
    }

    private void fetchRecentTransactions() {
        if (hasFetchedRecentTransactions) {
            return;
        }

        Helper.setViewVisibility(textNoRecentTransactions, View.GONE);
        TransactionListTask task = new TransactionListTask(1, 5, loadingRecentContainer, new TransactionListTask.TransactionListHandler() {
            @Override
            public void onSuccess(List<Transaction> transactions, boolean hasReachedEnd) {
                hasFetchedRecentTransactions = true;
                recentTransactionsAdapter = new TransactionListAdapter(transactions, getContext());
                recentTransactionsAdapter.setListener(new TransactionListAdapter.TransactionClickListener() {
                    @Override
                    public void onTransactionClicked(Transaction transaction) {

                    }

                    @Override
                    public void onClaimUrlClicked(LbryUri uri) {
                        Context context = getContext();
                        if (uri != null && context instanceof MainActivity) {
                            MainActivity activity = (MainActivity) context;
                            if (uri.isChannel()) {
                                activity.openChannelUrl(uri.toString());
                            } else {
                                activity.openFileUrl(uri.toString());
                            }
                        }
                    }
                });
                recentTransactionsList.setAdapter(recentTransactionsAdapter);
                displayNoRecentTransactions();
            }

            @Override
            public void onError(Exception error) {
                hasFetchedRecentTransactions = true;
                displayNoRecentTransactions();
            }
        });
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void displayNoRecentTransactions() {
        boolean showNoTransactionsView = hasFetchedRecentTransactions &&
                (recentTransactionsAdapter == null || recentTransactionsAdapter.getItemCount() == 0);
        Helper.setViewVisibility(textNoRecentTransactions, showNoTransactionsView ? View.VISIBLE : View.GONE);
    }

    private boolean validateSend() {
        String recipientAddress = Helper.getValue(inputSendAddress.getText());
        String amountString = Helper.getValue(inputSendAmount.getText());
        if (!recipientAddress.matches(LbryUri.REGEX_ADDRESS)) {
            Snackbar.make(getView(), R.string.invalid_recipient_address, Snackbar.LENGTH_LONG).
                    setBackgroundTint(Color.RED).setTextColor(Color.WHITE).show();
            return false;
        }

        if (!Helper.isNullOrEmpty(amountString)) {
            try {
                double amountValue = Double.valueOf(amountString);
                double availableAmount = Lbry.walletBalance.getAvailable().doubleValue();
                if (availableAmount < amountValue) {
                    Snackbar.make(getView(), R.string.insufficient_balance, Snackbar.LENGTH_LONG).
                            setBackgroundTint(Color.RED).setTextColor(Color.WHITE).show();
                    return false;
                }
            } catch (NumberFormatException ex) {
                // pass
                Snackbar.make(getView(), R.string.invalid_amount, Snackbar.LENGTH_LONG).
                        setBackgroundTint(Color.RED).setTextColor(Color.WHITE).show();
                return false;
            }
        }

        return true;
    }

    @SuppressWarnings("ClickableViewAccessibility")
    private void initUi() {
        onWalletBalanceUpdated(Lbry.walletBalance);

        Helper.applyHtmlForTextView(textConvertCredits);
        Helper.applyHtmlForTextView(textConvertCreditsBittrex);
        Helper.applyHtmlForTextView(textWhatSyncMeans);
        Helper.applyHtmlForTextView(linkManualBackup);
        Helper.applyHtmlForTextView(linkSyncFAQ);

        Context context = getContext();
        LinearLayoutManager llm = new LinearLayoutManager(context);
        recentTransactionsList.setLayoutManager(llm);
        DividerItemDecoration itemDecoration = new DividerItemDecoration(context, DividerItemDecoration.VERTICAL);
        itemDecoration.setDrawable(ContextCompat.getDrawable(context, R.drawable.thin_divider));
        recentTransactionsList.addItemDecoration(itemDecoration);

        linkUnlockTips.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (context != null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context).
                            setTitle(R.string.unlock_tips).
                            setMessage(R.string.confirm_unlock_tips)
                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    unlockTips();
                                }
                            }).setNegativeButton(R.string.no, null);
                    builder.show();
                }
            }
        });

        textEarnMoreTips.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (context instanceof MainActivity) {
                    ((MainActivity) context).openFragment(PublishFragment.class, true, NavMenuItem.ID_ITEM_NEW_PUBLISH);
                }
            }
        });

        buttonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Context context = getContext();
                if (context instanceof MainActivity) {
                    ((MainActivity) context).walletSyncSignIn();
                }
            }
        });
        buttonGetNewAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                generateNewAddress();
            }
        });
        textWalletReceiveAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                copyReceiveAddress();
            }
        });
        buttonCopyReceiveAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                copyReceiveAddress();
            }
        });
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validateSend()) {
                    sendCredits();
                }
            }
        });

        inputSendAddress.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                inputSendAddress.setHint(hasFocus ? getString(R.string.recipient_address_placeholder) : "");
            }
        });
        inputSendAmount.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                inputSendAmount.setHint(hasFocus ? getString(R.string.zero) : "");
                inlineBalanceContainer.setVisibility(hasFocus ? View.VISIBLE : View.INVISIBLE);
            }
        });

        layoutSdkInitializing.setVisibility(Lbry.SDK_READY ? View.GONE : View.VISIBLE);
        layoutAccountRecommended.setVisibility(hasSkippedAccount() || Lbryio.isSignedIn() ? View.GONE : View.VISIBLE);
        linkSkipAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
                sp.edit().putBoolean(MainActivity.PREFERENCE_KEY_INTERNAL_SKIP_WALLET_ACCOUNT, true).apply();
                layoutAccountRecommended.setVisibility(View.GONE);
            }
        });

        linkViewAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Context context = getContext();
                if (context instanceof MainActivity) {
                    ((MainActivity) context).openFragment(TransactionHistoryFragment.class, true, NavMenuItem.ID_ITEM_WALLET);
                }
            }
        });

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        final boolean walletSyncEnabled = sp.getBoolean(MainActivity.PREFERENCE_KEY_INTERNAL_WALLET_SYNC_ENABLED, false);
        switchSyncStatus.setChecked(walletSyncEnabled);
        switchSyncStatus.setText(walletSyncEnabled ? R.string.on : R.string.off);
        textWalletHintSyncStatus.setText(walletSyncEnabled ? R.string.backup_synced : R.string.backup_notsynced);
        textConnectedEmail.setText(walletSyncEnabled ? Lbryio.getSignedInEmail() : null);
        GestureDetector.SimpleOnGestureListener gestureListener = new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                if (switchSyncStatus.isChecked()) {
                    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
                    sp.edit().putBoolean(MainActivity.PREFERENCE_KEY_INTERNAL_WALLET_SYNC_ENABLED, false).apply();
                    switchSyncStatus.setText(R.string.off);
                    switchSyncStatus.setChecked(false);
                } else {
                    // launch verification activity for wallet sync flow
                    Context context = getContext();
                    if (context instanceof MainActivity) {
                        ((MainActivity) context).walletSyncSignIn();
                    }
                }
                return true;
            }
        };
        GestureDetector detector = new GestureDetector(getContext(), gestureListener);

        switchSyncStatus.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                detector.onTouchEvent(motionEvent);
                return true;
            }
        });
    }

    public void onWalletSyncEnabled() {
        switchSyncStatus.setText(R.string.on);
        switchSyncStatus.setChecked(true);
        textWalletHintSyncStatus.setText(R.string.backup_synced);
        textConnectedEmail.setText(Lbryio.getSignedInEmail());
        fetchRecentTransactions();
    }

    private void disableSendControls() {
        inputSendAddress.clearFocus();
        inputSendAmount.clearFocus();
        Helper.setViewEnabled(buttonSend, false);
        Helper.setViewEnabled(inputSendAddress, false);
        Helper.setViewEnabled(inputSendAmount, false);
    }

    private void enableSendControls() {
        Helper.setViewEnabled(buttonSend, true);
        Helper.setViewEnabled(inputSendAddress, true);
        Helper.setViewEnabled(inputSendAmount, true);
    }

    private void sendCredits() {
        // wallet_send task
        String recipientAddress = Helper.getValue(inputSendAddress.getText());
        String amountString = Helper.getValue(inputSendAmount.getText());
        String amount = new DecimalFormat(Helper.SDK_AMOUNT_FORMAT, new DecimalFormatSymbols(Locale.US)).
                format(new BigDecimal(amountString).doubleValue());

        disableSendControls();
        WalletSendTask task = new WalletSendTask(recipientAddress, amount, walletSendProgress, new WalletSendTask.WalletSendHandler() {
            @Override
            public void onSuccess() {
                double sentAmount = Double.valueOf(amount);
                String message = getResources().getQuantityString(
                        R.plurals.you_sent_credits, sentAmount == 1.0 ? 1 : 2,
                        new DecimalFormat("#,###.##").format(sentAmount));
                Snackbar.make(getView(), message, Snackbar.LENGTH_LONG).show();
                inputSendAddress.setText(null);
                inputSendAmount.setText(null);
                enableSendControls();
            }

            @Override
            public void onError(Exception error) {
                Snackbar.make(getView(), R.string.send_credit_error, Snackbar.LENGTH_LONG).
                        setBackgroundTint(Color.RED).setTextColor(Color.WHITE).show();
                enableSendControls();
            }
        });
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void checkReceiveAddress() {
        Context context = getContext();
        String receiveAddress = null;
        if (context != null) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            receiveAddress = sp.getString(MainActivity.PREFERENCE_KEY_INTERNAL_WALLET_RECEIVE_ADDRESS, null);
        }
        if (Helper.isNullOrEmpty(receiveAddress)) {
            if (Lbry.SDK_READY) {
                generateNewAddress();
            }
        } else if (textWalletReceiveAddress != null) {
            textWalletReceiveAddress.setText(receiveAddress);
        }
    }

    private boolean hasSkippedAccount() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        return sp.getBoolean(MainActivity.PREFERENCE_KEY_INTERNAL_SKIP_WALLET_ACCOUNT, false);
    }

    public void onResume() {
        super.onResume();
        Context context = getContext();
        Helper.setWunderbarValue(null, context);
        if (context instanceof MainActivity) {
            MainActivity activity = (MainActivity) context;
            LbryAnalytics.setCurrentScreen(activity, "Wallet", "Wallet");
        }

        Helper.setViewVisibility(layoutAccountRecommended, hasSkippedAccount() || Lbryio.isSignedIn() ? View.GONE : View.VISIBLE);
        if (!Lbry.SDK_READY) {
            if (context instanceof MainActivity) {
                MainActivity activity = (MainActivity) context;
                activity.addSdkStatusListener(this);
            }

            checkReceiveAddress();
        } else {
            onSdkReady();
        }
    }

    public void onPause() {
        hasFetchedRecentTransactions = false;
        super.onPause();
    }
    public void onStart() {
        super.onStart();
        Context context = getContext();
        if (context instanceof MainActivity) {
            MainActivity activity = (MainActivity) context;
            activity.setWunderbarValue(null);
            activity.addWalletBalanceListener(this);
            activity.hideFloatingWalletBalance();
        }
    }

    public void onStop() {
        Context context = getContext();
        if (context instanceof MainActivity) {
            MainActivity activity = (MainActivity) context;
            activity.removeWalletBalanceListener(this);
            activity.showFloatingWalletBalance();
        }
        super.onStop();
    }

    public void onSdkReady() {
        Context context = getContext();
        if (context instanceof MainActivity) {
            MainActivity activity = (MainActivity) context;
            activity.syncWalletAndLoadPreferences();
        }

        // update view
        if (layoutSdkInitializing != null) {
            layoutSdkInitializing.setVisibility(View.GONE);
        }

        checkReceiveAddress();
        checkRewardsDriver();
        checkTips();
        fetchRecentTransactions();
    }

    public void generateNewAddress() {
        WalletAddressUnusedTask task = new WalletAddressUnusedTask(new WalletAddressUnusedTask.WalletAddressUnusedHandler() {
            @Override
            public void beforeStart() {
                Helper.setViewEnabled(buttonGetNewAddress, false);
            }
            @Override
            public void onSuccess(String newAddress) {
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
                sp.edit().putString(MainActivity.PREFERENCE_KEY_INTERNAL_WALLET_RECEIVE_ADDRESS, newAddress).apply();
                Helper.setViewText(textWalletReceiveAddress, newAddress);
                Helper.setViewEnabled(buttonGetNewAddress, true);
            }

            @Override
            public void onError(Exception error) {
                Helper.setViewEnabled(buttonGetNewAddress, true);
            }
        });
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void onWalletBalanceUpdated(WalletBalance walletBalance) {
        double balance = walletBalance.getAvailable().doubleValue();
        double usdBalance = balance * Lbryio.LBCUSDRate;
        double tipsBalance = walletBalance.getTips().doubleValue();
        double tipsUsdBalance = tipsBalance * Lbryio.LBCUSDRate;

        String formattedBalance = Helper.SIMPLE_CURRENCY_FORMAT.format(balance);
        Helper.setViewText(textWalletBalance, balance > 0 && formattedBalance.equals("0") ? Helper.FULL_LBC_CURRENCY_FORMAT.format(balance) : formattedBalance);
        Helper.setViewText(textTipsBalance, Helper.shortCurrencyFormat(tipsBalance));
        Helper.setViewText(textClaimsBalance, Helper.shortCurrencyFormat(walletBalance.getClaims().doubleValue()));
        Helper.setViewText(textSupportsBalance, Helper.shortCurrencyFormat(walletBalance.getSupports().doubleValue()));
        Helper.setViewText(textWalletInlineBalance, Helper.shortCurrencyFormat(balance));
        if (Lbryio.LBCUSDRate > 0) {
            // only update display usd values if the rate is loaded
            Helper.setViewText(textWalletBalanceUSD, String.format("≈$%s", Helper.SIMPLE_CURRENCY_FORMAT.format(usdBalance)));
            Helper.setViewText(textTipsBalanceUSD, String.format("≈$%s", Helper.SIMPLE_CURRENCY_FORMAT.format(tipsUsdBalance)));
        }

        checkTips();
        checkRewardsDriver();
    }

    private void unlockTips() {
        Context context = getContext();
        if (context instanceof MainActivity) {
            linkUnlockTips.setVisibility(View.GONE);
            progressUnlockTips.setVisibility(View.VISIBLE);
            ((MainActivity) context).unlockTips();
        }
    }

    public void checkTips() {
        checkTips(false);
    }

    public void checkTips(boolean forceHideLink) {
        WalletBalance walletBalance = Lbry.walletBalance;
        double tipBalance = walletBalance == null ? 0 : walletBalance.getTips().doubleValue();
        boolean unlocking = false;
        Context context = getContext();
        if (context instanceof MainActivity) {
            MainActivity activity = (MainActivity) context;
            unlocking = activity.isUnlockingTips();
        }

        Helper.setViewVisibility(linkUnlockTips, !forceHideLink && tipBalance > 0 && !unlocking ? View.VISIBLE : View.GONE);
        Helper.setViewVisibility(progressUnlockTips, unlocking ? View.VISIBLE : View.GONE);
    }

    private void checkRewardsDriver() {
        // check rewards driver
        Context ctx = getContext();
        if (ctx != null) {
            String rewardsDriverText = getString(R.string.free_credits_available);
            if (Lbryio.totalUnclaimedRewardAmount > 0) {
                rewardsDriverText = getResources().getQuantityString(
                        Lbryio.isSignedIn() ? R.plurals.wallet_signed_in_free_credits : R.plurals.wallet_get_free_credits,
                        Lbryio.totalUnclaimedRewardAmount == 1 ? 1 : 2,
                        Helper.shortCurrencyFormat(Lbryio.totalUnclaimedRewardAmount));
            }
            checkRewardsDriverCard(rewardsDriverText, 0);
        }
    }
}
