package io.lbry.browser.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatSpinner;
import androidx.core.text.HtmlCompat;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
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
import io.lbry.browser.tasks.GenericTaskHandler;
import io.lbry.browser.tasks.claim.ClaimListResultHandler;
import io.lbry.browser.tasks.claim.ClaimListTask;
import io.lbry.browser.tasks.wallet.SupportCreateTask;
import io.lbry.browser.utils.Helper;
import io.lbry.browser.utils.Lbry;
import lombok.Setter;

public class CreateSupportDialogFragment extends BottomSheetDialogFragment implements WalletBalanceListener {
    public static final String TAG = "CreateSupportDialog";

    private MaterialButton sendButton;
    private View cancelLink;
    private TextInputEditText inputAmount;
    private View inlineBalanceContainer;
    private TextView inlineBalanceValue;
    private ProgressBar sendProgress;

    private InlineChannelSpinnerAdapter channelSpinnerAdapter;
    private AppCompatSpinner channelSpinner;
    private SwitchMaterial switchTip;

    private boolean fetchingChannels;
    private ProgressBar progressLoadingChannels;


    private final CreateSupportListener listener;
    private final Claim claim;

    private CreateSupportDialogFragment(Claim claim, CreateSupportListener listener) {
        super();
        this.claim = claim;
        this.listener = listener;
    }

    public static CreateSupportDialogFragment newInstance(Claim claim, CreateSupportListener listener) {
        return new CreateSupportDialogFragment(claim, listener);
    }

    private void disableControls() {
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.setCanceledOnTouchOutside(false);
        }
        channelSpinner.setEnabled(false);
        switchTip.setEnabled(false);
        sendButton.setEnabled(false);
        cancelLink.setEnabled(false);
    }
    private void enableControls() {
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.setCanceledOnTouchOutside(true);
        }
        channelSpinner.setEnabled(true);
        switchTip.setEnabled(true);
        sendButton.setEnabled(true);
        cancelLink.setEnabled(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_create_support, container, false);

        inputAmount = view.findViewById(R.id.create_support_input_amount);
        inlineBalanceContainer = view.findViewById(R.id.create_support_inline_balance_container);
        inlineBalanceValue = view.findViewById(R.id.create_support_inline_balance_value);
        sendProgress = view.findViewById(R.id.create_support_progress);
        cancelLink = view.findViewById(R.id.create_support_cancel_link);
        sendButton = view.findViewById(R.id.create_support_send);

        channelSpinner = view.findViewById(R.id.create_support_channel_spinner);
        switchTip = view.findViewById(R.id.create_support_make_tip_switch);
        progressLoadingChannels = view.findViewById(R.id.create_support_channel_progress);

        inputAmount.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                inputAmount.setHint(hasFocus ? getString(R.string.zero) : "");
                inlineBalanceContainer.setVisibility(hasFocus ? View.VISIBLE : View.INVISIBLE);
            }
        });
        inputAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                updateSendButtonText();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        updateInfoText();
        updateSendButtonText();

        String channel = null;
        if (Claim.TYPE_CHANNEL.equalsIgnoreCase(claim.getValueType())) {
            channel = claim.getTitleOrName();
        } else if (claim.getSigningChannel() != null) {
            channel = claim.getPublisherTitle();
        }
        TextView titleView = view.findViewById(R.id.create_support_title);
        String tipTitleText = Helper.isNullOrEmpty(channel) ? getString(R.string.send_a_tip) : getString(R.string.send_a_tip_to, channel);
        titleView.setText(tipTitleText);

        switchTip.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if (checked) {
                    // show tip info
                    titleView.setText(tipTitleText);
                    updateSendButtonText();
                } else {
                    // show support info
                    titleView.setText(R.string.support_this_content);
                    sendButton.setText(R.string.send_revocable_support);
                }
                updateInfoText();
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String amountString = Helper.getValue(inputAmount.getText());
                if (Helper.isNullOrEmpty(amountString)) {
                    showError(getString(R.string.invalid_amount));
                    return;
                }

                BigDecimal amount = new BigDecimal(amountString);
                if (amount.doubleValue() > Lbry.walletBalance.getAvailable().doubleValue()) {
                    showError(getString(R.string.insufficient_balance));
                    return;
                }
                if (amount.doubleValue() < Helper.MIN_SPEND) {
                    showError(getString(R.string.min_spend_required));
                    return;
                }

                Claim selectedChannel = (Claim) channelSpinner.getSelectedItem();
                String channelId = !fetchingChannels && selectedChannel != null ? selectedChannel.getClaimId() : null;
                boolean isTip = switchTip.isChecked();
                SupportCreateTask task = new SupportCreateTask(
                        claim.getClaimId(), channelId, amount, isTip, sendProgress, new GenericTaskHandler() {
                    @Override
                    public void beforeStart() {
                        disableControls();
                    }

                    @Override
                    public void onSuccess() {
                        enableControls();
                        if (listener != null) {
                            listener.onSupportCreated(amount, isTip);
                        }

                        dismiss();
                    }

                    @Override
                    public void onError(Exception error) {
                        showError(error.getMessage());
                        enableControls();
                    }
                });
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });

        cancelLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        onWalletBalanceUpdated(Lbry.walletBalance);
        updateInfoText();

        return view;
    }

    private void updateSendButtonText() {
        boolean isTip = switchTip.isChecked();
        if (!isTip) {
            sendButton.setText(R.string.send_revocable_support);
        } else {
            String amountString = Helper.getValue(inputAmount.getText(), "0");
            double parsedAmount = Helper.parseDouble(amountString, 0);
            String text = getResources().getQuantityString(R.plurals.send_lbc_tip, parsedAmount == 1.0 ? 1 : 2, amountString);
            sendButton.setText(text);
        }
    }

    private void updateInfoText() {
        View view = getView();
        if (view != null && switchTip != null) {
            TextView infoText = view.findViewById(R.id.create_support_info);
            boolean isTip = switchTip.isChecked();

            infoText.setMovementMethod(LinkMovementMethod.getInstance());
            if (!isTip) {
                infoText.setText(HtmlCompat.fromHtml(getString(R.string.support_info), HtmlCompat.FROM_HTML_MODE_LEGACY));
            } else if (claim != null) {
                infoText.setText(HtmlCompat.fromHtml(
                        Claim.TYPE_CHANNEL.equalsIgnoreCase(claim.getValueType()) ?
                                getString(R.string.send_tip_info_channel, claim.getTitleOrName()) :
                                getString(R.string.send_tip_info_content, claim.getTitleOrName()),
                        HtmlCompat.FROM_HTML_MODE_LEGACY));
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
                enableChannelSpinner();
                fetchingChannels = false;
            }

            @Override
            public void onError(Exception error) {
                enableChannelSpinner();
                fetchingChannels = false;
            }
        });
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
    private void disableChannelSpinner() {
        Helper.setViewEnabled(channelSpinner, false);
    }
    private void enableChannelSpinner() {
        Helper.setViewEnabled(channelSpinner, true);
    }

    private void updateChannelList(List<Claim> channels) {
        if (channelSpinnerAdapter == null) {
            Context context = getContext();
            if (context != null) {
                channelSpinnerAdapter = new InlineChannelSpinnerAdapter(context, R.layout.spinner_item_channel, new ArrayList<>(channels));
                channelSpinnerAdapter.addAnonymousPlaceholder();
                channelSpinnerAdapter.notifyDataSetChanged();
            }
        } else {
            channelSpinnerAdapter.clear();
            channelSpinnerAdapter.addAll(channels);
            channelSpinnerAdapter.addAnonymousPlaceholder();
            channelSpinnerAdapter.notifyDataSetChanged();
        }

        if (channelSpinner != null) {
            channelSpinner.setAdapter(channelSpinnerAdapter);
        }

        if (channelSpinnerAdapter != null && channelSpinner != null) {
            if (channelSpinnerAdapter.getCount() > 1) {
                channelSpinner.setSelection(1);
            }
        }
    }

    public void onResume() {
        super.onResume();
        Context context = getContext();
        if (context instanceof MainActivity) {
            ((MainActivity) context).addWalletBalanceListener(this);
        }
        updateInfoText();
        fetchChannels();
    }

    public void onPause() {
        Context context = getContext();
        if (context instanceof MainActivity) {
            ((MainActivity) context).removeWalletBalanceListener(this);
        }
        super.onPause();
    }

    @Override
    public void onWalletBalanceUpdated(WalletBalance walletBalance) {
        if (walletBalance != null && inlineBalanceValue != null) {
            inlineBalanceValue.setText(Helper.shortCurrencyFormat(walletBalance.getAvailable().doubleValue()));
        }
    }

    private void showError(String message) {
        Snackbar.make(getView(), message, Snackbar.LENGTH_LONG).
                setBackgroundTint(Color.RED).
                setTextColor(Color.WHITE).
                show();
    }

    public interface CreateSupportListener {
        void onSupportCreated(BigDecimal amount, boolean isTip);
    }
}
