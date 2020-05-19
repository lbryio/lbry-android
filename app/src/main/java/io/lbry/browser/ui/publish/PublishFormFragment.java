package io.lbry.browser.ui.publish;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import io.lbry.browser.R;
import io.lbry.browser.ui.BaseFragment;

public class PublishFormFragment extends BaseFragment {

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_publishes, container, false);

        return root;
    }

    @Override
    public boolean shouldHideGlobalPlayer() {
        return true;
    }

    @Override
    public boolean shouldSuspendGlobalPlayer() {
        return true;
    }
}
