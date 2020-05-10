package io.lbry.browser.ui.verification;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.hbb20.CountryCodePicker;

import io.lbry.browser.R;
import io.lbry.browser.listener.SignInListener;
import io.lbry.browser.tasks.GenericTaskHandler;
import io.lbry.browser.tasks.verification.PhoneNewVerifyTask;
import io.lbry.browser.utils.Helper;
import lombok.Setter;

public class PhoneVerificationFragment extends Fragment {
    @Setter
    private SignInListener listener;

    private View layoutCollect;
    private View layoutVerify;
    private MaterialButton continueButton;
    private MaterialButton verifyButton;
    private View editButton;
    private TextView textVerifyParagraph;
    private CountryCodePicker countryCodePicker;
    private EditText inputPhoneNumber;
    private EditText inputVerificationCode;
    private ProgressBar newLoading;
    private ProgressBar verifyLoading;

    private String currentCountryCode;
    private String currentPhoneNumber;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_verification_phone, container, false);

        layoutCollect = root.findViewById(R.id.verification_phone_collect_container);
        layoutVerify = root.findViewById(R.id.verification_phone_verify_container);
        continueButton = root.findViewById(R.id.verification_phone_continue_button);
        verifyButton = root.findViewById(R.id.verification_phone_verify_button);
        editButton = root.findViewById(R.id.verification_phone_edit_button);
        textVerifyParagraph = root.findViewById(R.id.verification_phone_verify_paragraph);

        countryCodePicker = root.findViewById(R.id.verification_phone_country_code);
        inputPhoneNumber = root.findViewById(R.id.verification_phone_input);
        inputVerificationCode = root.findViewById(R.id.verification_phone_code_input);

        newLoading = root.findViewById(R.id.verification_phone_new_progress);
        verifyLoading = root.findViewById(R.id.verification_phone_verify_progress);

        Context context = getContext();
        countryCodePicker.setTypeFace(ResourcesCompat.getFont(context, R.font.inter_light));
        countryCodePicker.registerCarrierNumberEditText(inputPhoneNumber);

        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentCountryCode = countryCodePicker.getSelectedCountryCode();
                currentPhoneNumber = Helper.getValue(inputPhoneNumber.getText());

                if (Helper.isNullOrEmpty(currentPhoneNumber) || !countryCodePicker.isValidFullNumber()) {
                    Snackbar.make(getView(), R.string.please_enter_valid_phone, Snackbar.LENGTH_LONG).setBackgroundTint(Color.RED).show();
                    return;
                }

                addPhoneNumber();
            }
        });

        verifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String code = Helper.getValue(inputVerificationCode.getText());
                if (Helper.isNullOrEmpty(code)) {
                    Snackbar.make(getView(), R.string.please_enter_verification_code, Snackbar.LENGTH_LONG).setBackgroundTint(Color.RED).show();
                    return;
                }
                verifyPhoneNumber(code);
            }
        });

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                layoutVerify.setVisibility(View.GONE);
                layoutCollect.setVisibility(View.VISIBLE);
            }
        });

        return root;
    }

    private void addPhoneNumber() {
        PhoneNewVerifyTask task = new PhoneNewVerifyTask(currentCountryCode, currentPhoneNumber, null, newLoading, new GenericTaskHandler() {
            @Override
            public void beforeStart() {
                continueButton.setEnabled(false);
                continueButton.setVisibility(View.GONE);
            }

            @Override
            public void onSuccess() {
                if (listener != null) {
                    listener.onPhoneAdded(currentCountryCode, currentPhoneNumber);
                }

                textVerifyParagraph.setText(getString(R.string.enter_phone_verify_code, countryCodePicker.getFullNumberWithPlus()));
                layoutCollect.setVisibility(View.GONE);
                layoutVerify.setVisibility(View.VISIBLE);
                continueButton.setEnabled(true);
                continueButton.setVisibility(View.VISIBLE);
            }

            @Override
            public void onError(Exception error) {
                Snackbar.make(getView(), error.getMessage(), Snackbar.LENGTH_LONG).setBackgroundTint(Color.RED).show();
                continueButton.setEnabled(true);
                continueButton.setVisibility(View.VISIBLE);
            }
        });
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void verifyPhoneNumber(String verificationCode) {
        PhoneNewVerifyTask task = new PhoneNewVerifyTask(currentCountryCode, currentPhoneNumber, verificationCode, verifyLoading, new GenericTaskHandler() {
            @Override
            public void beforeStart() {
                verifyButton.setEnabled(false);
                editButton.setEnabled(false);
            }

            @Override
            public void onSuccess() {
                if (listener != null) {
                    listener.onPhoneVerified();
                }
                verifyButton.setEnabled(true);
                editButton.setEnabled(true);
            }

            @Override
            public void onError(Exception error) {
                Snackbar.make(getView(), error.getMessage(), Snackbar.LENGTH_LONG).setBackgroundTint(Color.RED).show();
                verifyButton.setEnabled(true);
                editButton.setEnabled(true);
            }
        });
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}
