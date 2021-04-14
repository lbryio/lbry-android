package io.lbry.browser.ui.wallet;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.transition.TransitionManager;
import android.util.Base64;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import io.lbry.browser.MainActivity;
import io.lbry.browser.R;
import io.lbry.browser.adapter.TransactionListAdapter;
import io.lbry.browser.adapter.WalletDetailAdapter;
import io.lbry.browser.listener.SdkStatusListener;
import io.lbry.browser.listener.WalletBalanceListener;
import io.lbry.browser.model.NavMenuItem;
import io.lbry.browser.model.Transaction;
import io.lbry.browser.model.WalletBalance;
import io.lbry.browser.model.WalletDetailItem;
import io.lbry.browser.tasks.wallet.TransactionListTask;
import io.lbry.browser.tasks.wallet.WalletAddressUnusedTask;
import io.lbry.browser.tasks.wallet.WalletSendTask;
import io.lbry.browser.ui.BaseFragment;
import io.lbry.browser.utils.Helper;
import io.lbry.browser.utils.Lbry;
import io.lbry.browser.utils.LbryAnalytics;
import io.lbry.browser.utils.LbryUri;
import io.lbry.browser.utils.Lbryio;
import io.lbry.browser.views.CreditsBalanceView;

public class WalletFragment extends BaseFragment implements SdkStatusListener, WalletBalanceListener {

    private static final String MOONPAY_KEY = "c2tfbGl2ZV9ueVJqVXNDbE5pcnVSdnlCMkJLWW5JcFA5VnA3dWU=";
    private static final String MOONPAY_URL_FORMAT =
            "https://buy.moonpay.io?apiKey=pk_live_xNFffrN5NWKy6fu0ggbV8VQIwRieRzy&colorCode=%%232F9176&currencyCode=LBC&showWalletAddressForm=true&walletAddress=%s&externalCustomerId=%s";

    private View layoutAccountRecommended;
    private View layoutSdkInitializing;
    private View linkSkipAccount;
    private CreditsBalanceView walletTotalBalanceView;
    private CreditsBalanceView walletSpendableBalanceView;
    private CreditsBalanceView walletSupportingBalanceView;
    private TextView textWalletBalanceUSD;
    private TextView textWalletBalanceDesc;
    private TextView buttonViewMore;
    private ListView detailListView;
    List<WalletDetailItem> detailRows;
    private WalletDetailAdapter detailAdapter;

    private ProgressBar walletSendProgress;

    private View loadingRecentContainer;
    private View inlineBalanceContainer;
    private TextView textWalletInlineBalance;
    private MaterialButton buttonSignUp;
    private MaterialButton buttonBuyLBC;
    private RecyclerView recentTransactionsList;
    private View linkViewAll;
    private TextView textConvertCredits;
    private TextView textConvertCreditsBittrex;
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

        walletTotalBalanceView = root.findViewById(R.id.wallet_total_balance);
        walletSpendableBalanceView = root.findViewById(R.id.wallet_spendable_balance);
        walletSupportingBalanceView = root.findViewById(R.id.wallet_supporting_balance);
        textWalletBalanceUSD = root.findViewById(R.id.wallet_balance_usd_value);
        textWalletBalanceDesc = root.findViewById(R.id.total_balance_desc);
        textWalletHintSyncStatus = root.findViewById(R.id.wallet_hint_sync_status);
        buttonViewMore = root.findViewById(R.id.view_more_button);
        detailListView = root.findViewById(R.id.balance_detail_listview);

        recentTransactionsList = root.findViewById(R.id.wallet_recent_transactions_list);
        linkViewAll = root.findViewById(R.id.wallet_link_view_all);
        textNoRecentTransactions = root.findViewById(R.id.wallet_no_recent_transactions);
        buttonBuyLBC = root.findViewById(R.id.wallet_buy_lbc_button);
        textConvertCredits = root.findViewById(R.id.wallet_hint_convert_credits);
        textConvertCreditsBittrex = root.findViewById(R.id.wallet_hint_convert_credits_bittrex);
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

        detailRows = new ArrayList(3);

        detailAdapter = new WalletDetailAdapter((MainActivity) context, detailRows);
        detailListView.setAdapter(detailAdapter);

        buttonViewMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Context context = getContext();
                if (context instanceof MainActivity) {
                    View walletDetail = ((MainActivity) context).findViewById(R.id.balance_detail_listview);

                    if (walletDetail.getVisibility() == View.GONE) {
                        TransitionManager.beginDelayedTransition((ViewGroup) walletDetail.getParent());
                        walletDetail.setVisibility(View.VISIBLE);
                        buttonViewMore.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_dropup, 0);
                    } else {
                        walletDetail.setVisibility(View.GONE);
                        buttonViewMore.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_dropdown, 0);
                    }
                }
            }
        });

        ExecutorService executor = Executors.newSingleThreadExecutor();

        // This will return /true/ if user IP is **not** on the US or the request fails
        Future<Boolean> localeFuture = executor.submit(() -> {
            Request request  = new Request.Builder().url("https://api.lbry.com/locale/get").build();
            OkHttpClient okHttpClient = new OkHttpClient();

            try (Response response = okHttpClient.newCall(request).execute()){
                ResponseBody responseBody = response.body();
                JSONObject responseJson;

                if (responseBody != null)
                    responseJson = new JSONObject(responseBody.string());
                else
                    return false;

                if (responseJson.has("data") && responseJson.getBoolean("success")) {
                    JSONObject dataJson = (JSONObject) responseJson.get("data");
                    return !dataJson.getString("country").equals("US");
                } else {
                    return false;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        });

        try {
            Boolean isNotUS = localeFuture.get();
            if (isNotUS)
                textConvertCreditsBittrex.setVisibility(View.VISIBLE);
            else
                textConvertCreditsBittrex.setVisibility(View.GONE);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

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

        buttonBuyLBC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchMoonpayFlow();
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
        View view = getView();
        String recipientAddress = Helper.getValue(inputSendAddress.getText());
        String amountString = Helper.getValue(inputSendAmount.getText());
        String amount = null;
        try {
            amount = new DecimalFormat(Helper.SDK_AMOUNT_FORMAT, new DecimalFormatSymbols(Locale.US)).
                    format(new BigDecimal(amountString).doubleValue());
        } catch (NumberFormatException ex) {
            if (view != null) {
                Snackbar.make(view, R.string.invalid_amount, Snackbar.LENGTH_LONG).
                        setBackgroundTint(Color.RED).setTextColor(Color.WHITE).show();
            }
            return;
        }


        double actualSendAmount = Double.valueOf(amount);
        if (actualSendAmount < Helper.MIN_SPEND) {
            if (view != null) {
                Snackbar.make(view, R.string.min_spend_required, Snackbar.LENGTH_LONG).
                        setBackgroundTint(Color.RED).setTextColor(Color.WHITE).show();
            }
            return;
        }

        disableSendControls();
        WalletSendTask task = new WalletSendTask(recipientAddress, amount, walletSendProgress, new WalletSendTask.WalletSendHandler() {
            @Override
            public void onSuccess() {
                double sentAmount = actualSendAmount;
                String message = getResources().getQuantityString(
                        R.plurals.you_sent_credits, sentAmount == 1.0 ? 1 : 2,
                        new DecimalFormat("#,###.####").format(sentAmount));
                Helper.setViewText(inputSendAddress, null);
                Helper.setViewText(inputSendAmount, null);
                if (view != null) {
                    Snackbar.make(view, message, Snackbar.LENGTH_LONG).show();
                }
                enableSendControls();
            }

            @Override
            public void onError(Exception error) {
                if (view != null) {
                    Snackbar.make(view, R.string.send_credit_error, Snackbar.LENGTH_LONG).
                            setBackgroundTint(Color.RED).setTextColor(Color.WHITE).show();
                }
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

    public void launchMoonpayFlow() {
        Context context = getContext();
        String receiveAddress = null;
        if (context != null) {
            try {
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
                receiveAddress = sp.getString(MainActivity.PREFERENCE_KEY_INTERNAL_WALLET_RECEIVE_ADDRESS, null);
                if (Helper.isNullOrEmpty(receiveAddress)) {
                    showError(getString(R.string.receive_address_not_set));
                    return;
                }

                long userId = Lbryio.currentUser != null ? Lbryio.currentUser.getId() : 0;
                String url = String.format(MOONPAY_URL_FORMAT, receiveAddress,
                        URLEncoder.encode(String.format("android-%d", userId), StandardCharsets.UTF_8.name()));
                String email = Lbryio.getSignedInEmail();
                if (!Helper.isNullOrEmpty(email)) {
                    url = String.format("%s&email=%s", url, URLEncoder.encode(email, StandardCharsets.UTF_8.name()));
                }
                // Sign the URL
                String query = url.substring(url.indexOf("?"));
                Mac hmacSHA256 = Mac.getInstance("HmacSHA256");

                SecretKeySpec secretKey = new SecretKeySpec(
                        new String(Base64.decode(MOONPAY_KEY, Base64.NO_WRAP), StandardCharsets.UTF_8.name()).getBytes(), "HmacSHA256");
                hmacSHA256.init(secretKey);
                String signature = new String(
                        Base64.encode(hmacSHA256.doFinal(query.getBytes(StandardCharsets.UTF_8.name())), Base64.NO_WRAP),
                        StandardCharsets.UTF_8.name());
                url = String.format("%s&signature=%s", url, URLEncoder.encode(signature, StandardCharsets.UTF_8.name()));

                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder().setToolbarColor(
                        ContextCompat.getColor(context, R.color.lbryGreen)
                );
                CustomTabsIntent intent = builder.build();
                intent.launchUrl(context, Uri.parse(url));
            } catch (UnsupportedEncodingException | NoSuchAlgorithmException | InvalidKeyException ex) {
                showError(getString(R.string.hash_not_supported));
            }
        }
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
                Context context = getContext();
                if (context != null) {
                    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
                    sp.edit().putString(MainActivity.PREFERENCE_KEY_INTERNAL_WALLET_RECEIVE_ADDRESS, newAddress).apply();
                }
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
        double totalBalance = walletBalance.getTotal().doubleValue();
        double spendableBalance = walletBalance.getAvailable().doubleValue();
        double supportingBalance = walletBalance.getClaims().doubleValue() + walletBalance.getTips().doubleValue() + walletBalance.getSupports().doubleValue();
        double usdBalance = totalBalance * Lbryio.LBCUSDRate;
        double tipsBalance = walletBalance.getTips().doubleValue();

        if (detailRows == null)
            detailRows = new ArrayList<>(3);

        if (detailAdapter == null) {
            detailAdapter = new WalletDetailAdapter((MainActivity) getContext(), detailRows);
            detailListView.setAdapter(detailAdapter);
        }

        WalletDetailItem earnedBalance = new WalletDetailItem(getResources().getString(R.string.earned_from_others), getResources().getString(R.string.unlock_to_spend), Helper.SIMPLE_CURRENCY_FORMAT.format(tipsBalance), tipsBalance != 0, ((MainActivity) getContext()).isUnlockingTips());
        WalletDetailItem initialPublishes = new WalletDetailItem(getResources().getString(R.string.on_initial_publishes), getResources().getString(R.string.delete_or_edit_past_content), Helper.SIMPLE_CURRENCY_FORMAT.format(walletBalance.getClaims().doubleValue()), false, false);
        WalletDetailItem supportingContent = new WalletDetailItem(getResources().getString(R.string.supporting_content), getResources().getString(R.string.delete_supports_to_spend), Helper.SIMPLE_CURRENCY_FORMAT.format(walletBalance.getSupports().doubleValue()), false, false);

        boolean needNotifyAdapter = false;
        boolean firstDatasetNotification;

        if (detailRows.size() == 0) {
            detailRows.add(0, earnedBalance);
            detailRows.add(1, initialPublishes);
            detailRows.add(2, supportingContent);
            needNotifyAdapter = true;
            firstDatasetNotification = true;
        } else {
            firstDatasetNotification = false;
            if (!detailRows.get(0).detailAmount.equals(earnedBalance.detailAmount)
                 || detailRows.get(0).isInProgress != earnedBalance.isInProgress
                 || detailRows.get(0).isUnlockable != earnedBalance.isUnlockable) {
                detailRows.set(0, earnedBalance);
                needNotifyAdapter = true;
            }
            if (!detailRows.get(1).detailAmount.equals(initialPublishes.detailAmount)) {
                detailRows.set(1, initialPublishes);
                needNotifyAdapter = true;
            }
            if (!detailRows.get(2).detailAmount.equals(supportingContent.detailAmount)) {
                detailRows.set(2, supportingContent);
                needNotifyAdapter = true;
            }
        }

        if (needNotifyAdapter) {
            // notifyDatasetChanged() doesn't work, so simply reset the adapter to the list
            // to update the view
            detailListView.setAdapter(detailAdapter);

            if (firstDatasetNotification) {
                int listHeight = Math.round(getResources().getDisplayMetrics().density);

                for (int i = 0; i < detailRows.size(); i++) {
                    View item = detailAdapter.getView(i, null, detailListView);
                    item.measure(0, 0);
                    listHeight += item.getMeasuredHeight();
                }

                // Avoid scroll bars being displayed
                ViewGroup.LayoutParams params = detailListView.getLayoutParams();
                params.height = listHeight + (detailListView.getCount() + 1) * detailListView.getDividerHeight();
                detailListView.setLayoutParams(params);
                detailListView.setVerticalScrollBarEnabled(false);
                detailListView.requestLayout();
            }
        }

        String formattedTotalBalance = Helper.REDUCED_LBC_CURRENCY_FORMAT.format(totalBalance);
        String formattedSpendableBalance = Helper.SIMPLE_CURRENCY_FORMAT.format(spendableBalance);
        String formattedSupportingBalance = Helper.SIMPLE_CURRENCY_FORMAT.format(supportingBalance);
        Helper.setViewText(walletTotalBalanceView, totalBalance > 0 && formattedTotalBalance.equals("0") ? Helper.FULL_LBC_CURRENCY_FORMAT.format(totalBalance) : formattedTotalBalance);
        Helper.setViewText(walletSpendableBalanceView, spendableBalance > 0 && formattedSpendableBalance.equals("0") ? Helper.FULL_LBC_CURRENCY_FORMAT.format(spendableBalance) : formattedSpendableBalance);
        Helper.setViewText(walletSupportingBalanceView, supportingBalance > 0 && formattedSupportingBalance.equals("0") ? Helper.FULL_LBC_CURRENCY_FORMAT.format(supportingBalance) : formattedSupportingBalance);
        Helper.setViewText(textWalletInlineBalance, Helper.shortCurrencyFormat(spendableBalance));
        if (Lbryio.LBCUSDRate > 0) {
            // only update display usd values if the rate is loaded
            Helper.setViewText(textWalletBalanceUSD, String.format("≈$%s", Helper.SIMPLE_CURRENCY_FORMAT.format(usdBalance)));
        }

        textWalletBalanceDesc.setText(spendableBalance == totalBalance ? getResources().getString(R.string.your_total_balance) : getResources().getString(R.string.all_of_this_is_yours));

        checkTips();
        checkRewardsDriver();
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
