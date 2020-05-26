package io.lbry.browser.ui.verification;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import io.lbry.browser.R;
import io.lbry.browser.listener.SignInListener;
import io.lbry.browser.tasks.GenericTaskHandler;
import io.lbry.browser.tasks.verification.CheckUserEmailVerifiedTask;
import io.lbry.browser.tasks.verification.EmailNewTask;
import io.lbry.browser.tasks.verification.EmailResendTask;
import io.lbry.browser.utils.Helper;
import lombok.Setter;

public class EmailVerificationFragment extends Fragment {

    @Setter
    private SignInListener listener;
    private View layoutCollect;
    private View layoutVerify;
    private ProgressBar emailAddProgress;
    private TextView textAddedEmail;
    private TextInputEditText inputEmail;
    private TextInputLayout inputLayoutEmail;
    private MaterialButton buttonContinue;
    private MaterialButton buttonResend;
    private View buttonEdit;

    private String currentEmail;

    private ScheduledExecutorService emailVerifyCheckScheduler;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_verification_email, container, false);

        layoutCollect = root.findViewById(R.id.verification_email_collect_container);
        layoutVerify = root.findViewById(R.id.verification_email_verify_container);
        inputEmail = root.findViewById(R.id.verification_email_input);
        inputLayoutEmail = root.findViewById(R.id.verification_email_input_layout);
        emailAddProgress = root.findViewById(R.id.verification_email_add_progress);
        textAddedEmail = root.findViewById(R.id.verification_email_added_address);
        buttonContinue = root.findViewById(R.id.verification_email_continue_button);
        buttonResend = root.findViewById(R.id.verification_email_resend_button);
        buttonEdit = root.findViewById(R.id.verification_email_edit_button);

        layoutCollect.setVisibility(View.VISIBLE);

        inputEmail.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                String layoutHint = !hasFocus ? "" : getString(R.string.email);
                inputLayoutEmail.setHint(layoutHint);
            }
        });
        buttonContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addEmail();
            }
        });
        buttonEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editEmail();
            }
        });
        buttonResend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resendEmail();
            }
        });

        return root;
    }

    private void addEmail() {
        currentEmail = Helper.getValue(inputEmail.getText());
        if (Helper.isNullOrEmpty(currentEmail) || currentEmail.indexOf("@") == -1) {
            Snackbar.make(getView(), R.string.provide_valid_email, Snackbar.LENGTH_LONG).
                    setBackgroundTint(Color.RED).setTextColor(Color.WHITE).show();
            return;
        }

        Context context = getContext();
        if (context != null) {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(inputEmail.getWindowToken(), 0);
        }

        EmailNewTask task = new EmailNewTask(currentEmail, emailAddProgress, new EmailNewTask.EmailNewHandler() {
            @Override
            public void beforeStart() {
                Helper.setViewVisibility(buttonContinue, View.INVISIBLE);
            }

            @Override
            public void onSuccess() {
                layoutCollect.setVisibility(View.GONE);
                layoutVerify.setVisibility(View.VISIBLE);
                Helper.setViewText(textAddedEmail, currentEmail);
                if (listener != null) {
                    listener.onEmailAdded(currentEmail);
                }
                scheduleEmailVerify();

                Helper.setViewVisibility(buttonContinue, View.VISIBLE);
            }

            @Override
            public void onEmailExists() {
                // TODO: Update wording based on email already existing
            }

            @Override
            public void onError(Exception error) {
                Snackbar.make(getView(), error.getMessage(), Snackbar.LENGTH_LONG).
                        setBackgroundTint(Color.RED).setTextColor(Color.WHITE).show();
                Helper.setViewVisibility(buttonContinue, View.VISIBLE);
            }
        });
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void scheduleEmailVerify() {
        emailVerifyCheckScheduler = Executors.newSingleThreadScheduledExecutor();
        emailVerifyCheckScheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                checkEmailVerified();
            }
        }, 5, 5, TimeUnit.SECONDS);
    }

    private void checkEmailVerified() {
        CheckUserEmailVerifiedTask task = new CheckUserEmailVerifiedTask(new CheckUserEmailVerifiedTask.CheckUserEmailVerifiedHandler() {
            @Override
            public void onUserEmailVerified() {
                if (listener != null) {
                    listener.onEmailVerified();
                }
                layoutCollect.setVisibility(View.GONE);
                layoutVerify.setVisibility(View.GONE);
                if (emailVerifyCheckScheduler != null) {
                    emailVerifyCheckScheduler.shutdownNow();
                    emailVerifyCheckScheduler = null;
                }
            }
        });
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void editEmail() {
        if (emailVerifyCheckScheduler != null) {
            emailVerifyCheckScheduler.shutdownNow();
            emailVerifyCheckScheduler = null;
        }

        if (listener != null) {
            listener.onEmailEdit();
        }
        layoutVerify.setVisibility(View.GONE);
        layoutCollect.setVisibility(View.VISIBLE);
    }

    private void resendEmail() {
        EmailResendTask task = new EmailResendTask(currentEmail, null, new GenericTaskHandler() {
            @Override
            public void beforeStart() {
                Helper.setViewEnabled(buttonResend, false);
            }

            @Override
            public void onSuccess() {
                Snackbar.make(getView(), R.string.please_follow_instructions, Snackbar.LENGTH_LONG).show();
                Helper.setViewEnabled(buttonResend, true);
            }

            @Override
            public void onError(Exception error) {
                Snackbar.make(getView(), error.getMessage(), Snackbar.LENGTH_LONG).
                        setBackgroundTint(Color.RED).setTextColor(Color.WHITE).show();
                Helper.setViewEnabled(buttonResend, true);
            }
        });
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}
