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

    public void onResume() {
        super.onResume();
        Context context = getContext();
        if (context instanceof MainActivity) {
            MainActivity activity = (MainActivity) context;
            activity.setSelectedMenuItemForFragment(this);

            if (this instanceof FileViewFragment) {
                activity.hideGlobalNowPlaying();
            } else {
                activity.checkNowPlaying();
            }
        }
    }

}
