package io.lbry.browser.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.math.BigDecimal;
import java.text.DecimalFormat;

import io.lbry.browser.MainActivity;
import io.lbry.browser.R;
import io.lbry.browser.listener.WalletBalanceListener;
import io.lbry.browser.model.Claim;
import io.lbry.browser.model.WalletBalance;
import io.lbry.browser.tasks.GenericTaskHandler;
import io.lbry.browser.tasks.wallet.SupportCreateTask;
import io.lbry.browser.utils.Helper;
import io.lbry.browser.utils.Lbry;
import lombok.Setter;

public class SendTipDialogFragment extends BottomSheetDialogFragment implements WalletBalanceListener {
    public static final String TAG = "SendTipDialog";

    private MaterialButton sendButton;
    private View cancelLink;
    private TextInputEditText inputAmount;
    private View inlineBalanceContainer;
    private TextView inlineBalanceValue;
    private ProgressBar sendProgress;

    @Setter
    private SendTipListener listener;
    @Setter
    private Claim claim;

    public static SendTipDialogFragment newInstance() {
        return new SendTipDialogFragment();
    }

    private void disableControls() {
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.setCanceledOnTouchOutside(false);
        }
        sendButton.setEnabled(false);
        cancelLink.setEnabled(false);
    }
    private void enableControls() {
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.setCanceledOnTouchOutside(true);
        }
        sendButton.setEnabled(true);
        cancelLink.setEnabled(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_send_tip, container, false);

        inputAmount = view.findViewById(R.id.tip_input_amount);
        inlineBalanceContainer = view.findViewById(R.id.tip_inline_balance_container);
        inlineBalanceValue = view.findViewById(R.id.tip_inline_balance_value);
        sendProgress = view.findViewById(R.id.tip_send_progress);
        cancelLink = view.findViewById(R.id.tip_cancel_link);
        sendButton = view.findViewById(R.id.tip_send);

        inputAmount.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                inputAmount.setHint(hasFocus ? getString(R.string.zero) : "");
                inlineBalanceContainer.setVisibility(hasFocus ? View.VISIBLE : View.INVISIBLE);
            }
        });

        TextView infoText = view.findViewById(R.id.tip_info);
        infoText.setMovementMethod(LinkMovementMethod.getInstance());
        infoText.setText(HtmlCompat.fromHtml(
                Claim.TYPE_CHANNEL.equalsIgnoreCase(claim.getValueType()) ?
                        getString(R.string.send_tip_info_channel, claim.getTitleOrName()) :
                        getString(R.string.send_tip_info_content, claim.getTitleOrName()),
                HtmlCompat.FROM_HTML_MODE_LEGACY));

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

                SupportCreateTask task = new SupportCreateTask(claim.getClaimId(), amount, true, sendProgress, new GenericTaskHandler() {
                    @Override
                    public void beforeStart() {
                        disableControls();
                    }

                    @Override
                    public void onSuccess() {
                        enableControls();
                        if (listener != null) {
                            listener.onTipSent(amount);
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

        String channel = null;
        if (Claim.TYPE_CHANNEL.equalsIgnoreCase(claim.getValueType())) {
            channel = claim.getTitleOrName();
        } else if (claim.getSigningChannel() != null) {
            channel = claim.getPublisherTitle();
        }
        ((TextView) view.findViewById(R.id.tip_send_title)).setText(
                Helper.isNullOrEmpty(channel) ? getString(R.string.send_a_tip) : getString(R.string.send_a_tip_to, channel)
        );

        onWalletBalanceUpdated(Lbry.walletBalance);

        return view;
    }

    public void onResume() {
        super.onResume();
        Context context = getContext();
        if (context instanceof MainActivity) {
            ((MainActivity) context).addWalletBalanceListener(this);
        }
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

    public interface SendTipListener {
        void onTipSent(BigDecimal amount);
    }
}
