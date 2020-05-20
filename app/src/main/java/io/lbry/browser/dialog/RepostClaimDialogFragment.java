package io.lbry.browser.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatSpinner;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import io.lbry.browser.MainActivity;
import io.lbry.browser.R;
import io.lbry.browser.adapter.InlineChannelSpinnerAdapter;
import io.lbry.browser.listener.WalletBalanceListener;
import io.lbry.browser.model.Claim;
import io.lbry.browser.model.WalletBalance;
import io.lbry.browser.tasks.claim.ClaimListResultHandler;
import io.lbry.browser.tasks.claim.ClaimListTask;
import io.lbry.browser.tasks.claim.ClaimResultHandler;
import io.lbry.browser.tasks.claim.StreamRepostTask;
import io.lbry.browser.utils.Helper;
import io.lbry.browser.utils.Lbry;
import io.lbry.browser.utils.LbryUri;
import lombok.Setter;

public class RepostClaimDialogFragment extends BottomSheetDialogFragment implements WalletBalanceListener {
    public static final String TAG = "RepostClaimDialog";

    private MaterialButton buttonRepost;
    private View linkCancel;
    private TextInputEditText inputDeposit;
    private View inlineBalanceContainer;
    private TextView inlineBalanceValue;
    private ProgressBar repostProgress;
    private TextView textTitle;

    private AppCompatSpinner channelSpinner;
    private InlineChannelSpinnerAdapter channelSpinnerAdapter;
    private TextView textNamePrefix;
    private EditText inputName;
    private TextView linkToggleAdvanced;
    private View advancedContainer;

    @Setter
    private RepostClaimListener listener;
    @Setter
    private Claim claim;

    public static RepostClaimDialogFragment newInstance() {
        return new RepostClaimDialogFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_repost_claim, container, false);

        buttonRepost = view.findViewById(R.id.repost_button);
        linkCancel = view.findViewById(R.id.repost_cancel_link);
        inputDeposit = view.findViewById(R.id.repost_input_deposit);
        inlineBalanceContainer = view.findViewById(R.id.repost_inline_balance_container);
        inlineBalanceValue = view.findViewById(R.id.repost_inline_balance_value);
        repostProgress = view.findViewById(R.id.repost_progress);
        textTitle = view.findViewById(R.id.repost_title);

        channelSpinner = view.findViewById(R.id.repost_channel_spinner);
        textNamePrefix = view.findViewById(R.id.repost_name_prefix);
        inputName = view.findViewById(R.id.repost_name_input);
        linkToggleAdvanced = view.findViewById(R.id.repost_toggle_advanced);
        advancedContainer = view.findViewById(R.id.repost_advanced_container);

        textTitle.setText(getString(R.string.repost_title, claim.getTitle()));
        inputName.setText(claim.getName());
        inputDeposit.setText(R.string.min_deposit);
        channelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                Object item = adapterView.getItemAtPosition(position);
                if (item instanceof Claim) {
                    Claim claim = (Claim) item;
                    textNamePrefix.setText(String.format("%s%s/", LbryUri.PROTO_DEFAULT, claim.getName()));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        inputDeposit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                inputDeposit.setHint(hasFocus ? getString(R.string.zero) : "");
                inlineBalanceContainer.setVisibility(hasFocus ? View.VISIBLE : View.INVISIBLE);
            }
        });

        linkCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        linkToggleAdvanced.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (advancedContainer.getVisibility() != View.VISIBLE) {
                    advancedContainer.setVisibility(View.VISIBLE);
                    linkToggleAdvanced.setText(R.string.hide_advanced);
                } else {
                    advancedContainer.setVisibility(View.GONE);
                    linkToggleAdvanced.setText(R.string.show_advanced);
                }
            }
        });

        buttonRepost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateAndRepostClaim();
            }
        });

        onWalletBalanceUpdated(Lbry.walletBalance);

        return view;
    }

    public void onResume() {
        super.onResume();
        Context context = getContext();
        if (context instanceof MainActivity) {
            ((MainActivity) context).addWalletBalanceListener(this);
        }
        fetchChannels();
    }

    public void onPause() {
        Context context = getContext();
        if (context instanceof MainActivity) {
            ((MainActivity) context).removeWalletBalanceListener(this);
        }
        inputDeposit.clearFocus();
        super.onPause();
    }


    private void fetchChannels() {
        if (Lbry.ownChannels == null || Lbry.ownChannels.size() == 0) {
            startLoading();
            ClaimListTask task = new ClaimListTask(Claim.TYPE_CHANNEL, repostProgress, new ClaimListResultHandler() {
                @Override
                public void onSuccess(List<Claim> claims) {
                    Lbry.ownChannels = new ArrayList<>(claims);
                    loadChannels(claims);
                    finishLoading();
                }

                @Override
                public void onError(Exception error) {
                    // could not fetch channels
                    Context context = getContext();
                    if (context instanceof MainActivity) {
                        ((MainActivity) context).showError(error.getMessage());
                    }
                    dismiss();
                }
            });
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            loadChannels(Lbry.ownChannels);
        }
    }

    private void loadChannels(List<Claim> channels) {
        if (channelSpinnerAdapter == null) {
            Context context = getContext();
            channelSpinnerAdapter = new InlineChannelSpinnerAdapter(context, R.layout.spinner_item_channel, channels);
            channelSpinnerAdapter.notifyDataSetChanged();
        } else {
            channelSpinnerAdapter.clear();
            channelSpinnerAdapter.addAll(channels);
            channelSpinnerAdapter.notifyDataSetChanged();
        }
        if (channelSpinner != null) {
            channelSpinner.setAdapter(channelSpinnerAdapter);
        }
    }

    @Override
    public void onWalletBalanceUpdated(WalletBalance walletBalance) {
        if (walletBalance != null && inlineBalanceValue != null) {
            inlineBalanceValue.setText(Helper.shortCurrencyFormat(walletBalance.getAvailable().doubleValue()));
        }
    }

    private void validateAndRepostClaim() {
        String name = Helper.getValue(inputName.getText());
        if (Helper.isNullOrEmpty(name) || !LbryUri.isNameValid(name)) {
            showError(getString(R.string.repost_name_invalid_characters));
            return;
        }

        String depositString = Helper.getValue(inputDeposit.getText());
        if (Helper.isNullOrEmpty(depositString)) {
            showError(getString(R.string.invalid_amount));
            return;
        }

        BigDecimal bid = new BigDecimal(depositString);
        if (bid.doubleValue() > Lbry.walletBalance.getAvailable().doubleValue()) {
            showError(getString(R.string.insufficient_balance));
            return;
        }

        Claim channel = (Claim) channelSpinner.getSelectedItem();
        StreamRepostTask task = new StreamRepostTask(name, bid, claim.getClaimId(), channel.getClaimId(), repostProgress, new ClaimResultHandler() {
            @Override
            public void beforeStart() {
                startLoading();
            }

            @Override
            public void onSuccess(Claim claimResult) {
                if (listener != null) {
                    listener.onClaimReposted(claimResult);
                }
                finishLoading();
                dismiss();
            }

            @Override
            public void onError(Exception error) {
                showError(error.getMessage());
                finishLoading();
            }
        });
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void showError(String message) {
        Snackbar.make(getView(), message, Snackbar.LENGTH_LONG).
                setBackgroundTint(Color.RED).
                setTextColor(Color.WHITE).
                show();
    }

    private void startLoading() {
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.setCanceledOnTouchOutside(false);
        }
        linkCancel.setEnabled(false);
        buttonRepost.setEnabled(false);
        inputName.setEnabled(false);
        channelSpinner.setEnabled(false);
        linkToggleAdvanced.setVisibility(View.INVISIBLE);
    }
    private void finishLoading() {
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.setCanceledOnTouchOutside(true);
        }
        linkCancel.setEnabled(true);
        buttonRepost.setEnabled(true);
        inputName.setEnabled(true);
        channelSpinner.setEnabled(true);
        linkToggleAdvanced.setVisibility(View.VISIBLE);
    }

    public interface RepostClaimListener {
        void onClaimReposted(Claim claim);
    }
}
