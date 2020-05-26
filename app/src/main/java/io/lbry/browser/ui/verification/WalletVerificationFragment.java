package io.lbry.browser.ui.verification;

import android.app.Activity;
import android.content.Context;
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

import io.lbry.browser.MainActivity;
import io.lbry.browser.R;
import io.lbry.browser.listener.WalletSyncListener;
import io.lbry.browser.model.WalletSync;
import io.lbry.browser.tasks.wallet.DefaultSyncTaskHandler;
import io.lbry.browser.tasks.wallet.SyncApplyTask;
import io.lbry.browser.tasks.wallet.SyncGetTask;
import io.lbry.browser.tasks.wallet.SyncSetTask;
import io.lbry.browser.utils.Helper;
import io.lbry.browser.utils.Lbry;
import io.lbry.browser.utils.Lbryio;
import io.lbry.lbrysdk.Utils;
import lombok.Setter;

public class WalletVerificationFragment extends Fragment {

    @Setter
    private WalletSyncListener listener = null;
    private ProgressBar loading;
    private TextView textLoading;
    private View inputArea;
    private MaterialButton doneButton;
    private TextInputEditText inputPassword;
    private WalletSync currentWalletSync;
    private boolean verificationStarted;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_verification_wallet, container, false);

        loading = root.findViewById(R.id.verification_wallet_loading_progress);
        textLoading = root.findViewById(R.id.verification_wallet_loading_text);
        inputArea = root.findViewById(R.id.verification_wallet_input_area);
        doneButton = root.findViewById(R.id.verification_wallet_done_button);
        inputPassword = root.findViewById(R.id.verification_wallet_password_input);

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String password = Helper.getValue(inputPassword.getText());
                if (Helper.isNullOrEmpty(password)) {
                    showError(getString(R.string.please_enter_your_password));
                    return;
                }

                Context context = getContext();
                if (context != null) {
                    InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(inputPassword.getWindowToken(), 0);
                }

                if (listener != null) {
                    listener.onWalletSyncProcessing();
                }
                processExistingWalletWithPassword(password);
            }
        });

        loading.setVisibility(View.VISIBLE);
        inputArea.setVisibility(View.GONE);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        start();
    }

    public void start() {
        if (verificationStarted) {
            return;
        }
        if (listener != null) {
            listener.onWalletSyncProcessing();
        }

        verificationStarted = true;
        Helper.setViewVisibility(loading, View.VISIBLE);
        Helper.setViewVisibility(textLoading, View.VISIBLE);
        // attempt to load secure value from versions pre-0.15.0
        String prevVersionPassword = Utils.getSecureValue(MainActivity.SECURE_VALUE_FIRST_RUN_PASSWORD, getContext(), Lbry.KEYSTORE);
        String password = Utils.getSecureValue(MainActivity.SECURE_VALUE_KEY_SAVED_PASSWORD, getContext(), Lbry.KEYSTORE);
        // start verification process
        SyncGetTask task = new SyncGetTask(!Helper.isNullOrEmpty(prevVersionPassword) ? prevVersionPassword : password,
                false,
                null,
                new DefaultSyncTaskHandler() {
            @Override
            public void onSyncGetSuccess(WalletSync walletSync) {
                currentWalletSync = walletSync;
                Lbryio.lastRemoteHash = walletSync.getHash();
                processExistingWallet(walletSync);
            }

            @Override
            public void onSyncGetWalletNotFound() {
                // no wallet found, get sync apply data and run the process
                processNewWallet();
            }
            @Override
            public void onSyncGetError(Exception error) {
                // try again
                Helper.setViewVisibility(loading, View.GONE);
                Helper.setViewText(textLoading, error.getMessage());
                showError(error.getMessage());
                if (listener != null) {
                    listener.onWalletSyncFailed(error);
                }
            }
        });
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void processExistingWallet(WalletSync walletSync) {
        // Try first sync apply
        SyncApplyTask applyTask = new SyncApplyTask("", walletSync.getData(), null, new DefaultSyncTaskHandler() {
            @Override
            public void onSyncApplySuccess(String hash, String data) {
                // check if local and remote hash are different, and then run sync set
                Utils.setSecureValue(MainActivity.SECURE_VALUE_KEY_SAVED_PASSWORD, "", getContext(), Lbry.KEYSTORE);
                if (!hash.equalsIgnoreCase(Lbryio.lastRemoteHash) && !Helper.isNullOrEmpty(Lbryio.lastRemoteHash)) {
                    new SyncSetTask(Lbryio.lastRemoteHash, hash, data, null).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
                if (listener != null) {
                    listener.onWalletSyncEnabled();
                }
            }

            @Override
            public void onSyncApplyError(Exception error) {
                // failed, request the user to enter a password
                Helper.setViewVisibility(loading, View.GONE);
                Helper.setViewVisibility(textLoading, View.GONE);
                Helper.setViewVisibility(inputArea, View.VISIBLE);
                if (listener != null) {
                    listener.onWalletSyncWaitingForInput();
                }
            }
        });
        applyTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void processExistingWalletWithPassword(String password) {
        Helper.setViewVisibility(loading, View.VISIBLE);
        Helper.setViewVisibility(textLoading, View.VISIBLE);
        Helper.setViewVisibility(inputArea, View.GONE);

        if (currentWalletSync == null) {
            showError(getString(R.string.wallet_sync_op_failed));
            Helper.setViewText(textLoading, R.string.wallet_sync_op_failed);
            return;
        }

        Helper.setViewText(textLoading, R.string.apply_wallet_data);
        SyncApplyTask applyTask = new SyncApplyTask(password, currentWalletSync.getData(), null, new DefaultSyncTaskHandler() {
            @Override
            public void onSyncApplySuccess(String hash, String data) {
                Utils.setSecureValue(MainActivity.SECURE_VALUE_KEY_SAVED_PASSWORD, password, getContext(), Lbry.KEYSTORE);
                // check if local and remote hash are different, and then run sync set
                if (!hash.equalsIgnoreCase(Lbryio.lastRemoteHash) && !Helper.isNullOrEmpty(Lbryio.lastRemoteHash)) {
                    new SyncSetTask(Lbryio.lastRemoteHash, hash, data, null).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
                if (listener != null) {
                    listener.onWalletSyncEnabled();
                }
            }

            @Override
            public void onSyncApplyError(Exception error) {
                // failed, request the user to enter a password
                showError(error.getMessage());
                Helper.setViewVisibility(loading, View.GONE);
                Helper.setViewVisibility(textLoading, View.GONE);
                Helper.setViewVisibility(inputArea, View.VISIBLE);
                if (listener != null) {
                    listener.onWalletSyncWaitingForInput();
                }
            }
        });
        applyTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void processNewWallet() {
        SyncApplyTask fetchTask = new SyncApplyTask(true, null, new DefaultSyncTaskHandler() {
            @Override
            public void onSyncApplySuccess(String hash, String data) { createNewRemoteSync(hash, data); }
            @Override
            public void onSyncApplyError(Exception error) {
                showError(error.getMessage());
                Helper.setViewVisibility(loading, View.GONE);
                Helper.setViewText(textLoading, R.string.wallet_sync_op_failed);
                if (listener != null) {
                    listener.onWalletSyncFailed(error);
                }
            }
        });
        fetchTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void createNewRemoteSync(String hash, String data) {
        SyncSetTask setTask = new SyncSetTask("", hash, data, new DefaultSyncTaskHandler() {
            @Override
            public void onSyncSetSuccess(String hash) {
                Lbryio.lastRemoteHash = hash;
                if (listener != null) {
                    listener.onWalletSyncEnabled();
                }
            }

            @Override
            public void onSyncSetError(Exception error) {
                showError(error.getMessage());
                Helper.setViewVisibility(loading, View.GONE);
                Helper.setViewText(textLoading, R.string.wallet_sync_op_failed);
                if (listener != null) {
                    listener.onWalletSyncFailed(error);
                }
            }
        });
        setTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void showError(String message) {
        Snackbar.make(getView(), message, Snackbar.LENGTH_LONG).setBackgroundTint(
                getResources().getColor(R.color.red)
        ).show();
    }
}
