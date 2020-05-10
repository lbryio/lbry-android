package io.lbry.browser.ui.verification;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import io.lbry.browser.R;
import io.lbry.browser.listener.SignInListener;
import io.lbry.browser.utils.Helper;
import lombok.Setter;

public class ManualVerificationFragment extends Fragment {
    @Setter
    private SignInListener listener;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_verification_manual, container, false);

        Helper.applyHtmlForTextView((TextView) root.findViewById(R.id.verification_manual_discord_verify));
        root.findViewById(R.id.verification_manual_continue_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.onManualVerifyContinue();
                }
            }
        });

        return root;
    }
}
