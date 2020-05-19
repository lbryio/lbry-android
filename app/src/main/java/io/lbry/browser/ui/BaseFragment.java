package io.lbry.browser.ui;

import android.content.Context;

import androidx.fragment.app.Fragment;

import java.util.Map;

import io.lbry.browser.MainActivity;
import io.lbry.browser.ui.following.FileViewFragment;
import lombok.Getter;
import lombok.Setter;

public class BaseFragment extends Fragment {
    @Getter
    @Setter
    private Map<String, Object> params;

    public boolean shouldHideGlobalPlayer() {
        return false;
    }

    public boolean shouldSuspendGlobalPlayer() {
        return false;
    }

    public void onStart() {
        super.onStart();
        if (shouldSuspendGlobalPlayer()) {
            Context context = getContext();
            if (context instanceof MainActivity) {
                MainActivity.suspendGlobalPlayer(context);
            }
        }
    }

    public void onStop() {
        if (shouldSuspendGlobalPlayer()) {
            Context context = getContext();
            if (context instanceof MainActivity) {
                MainActivity.resumeGlobalPlayer(context);
            }
        }
        super.onStop();
    }

    public void onResume() {
        super.onResume();
        Context context = getContext();
        if (context instanceof MainActivity) {
            MainActivity activity = (MainActivity) context;
            activity.setSelectedMenuItemForFragment(this);

            if (shouldHideGlobalPlayer()) {
                activity.hideGlobalNowPlaying();
            } else {
                activity.checkNowPlaying();
            }
        }
    }
}
