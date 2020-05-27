package io.lbry.browser.ui;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import java.util.Map;

import io.lbry.browser.MainActivity;
import io.lbry.browser.R;
import io.lbry.browser.model.WalletBalance;
import io.lbry.browser.ui.wallet.RewardsFragment;
import io.lbry.browser.utils.Helper;
import io.lbry.browser.utils.Lbry;
import lombok.Getter;
import lombok.Setter;

public class BaseFragment extends Fragment {
    @Getter
    @Setter
    private Map<String, Object> params;
    private boolean rewardDriverClickListenerSet;

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
        rewardDriverClickListenerSet = false;
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

    public void checkRewardsDriverCard(String rewardDriverText, double minCost) {
        View root = getView();
        if (root != null) {
            View rewardDriverCard = root.findViewById(R.id.reward_driver_card);
            if (rewardDriverCard != null) {
                if (!rewardDriverClickListenerSet) {
                    rewardDriverCard.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Context context = getContext();
                            if (context instanceof MainActivity) {
                                ((MainActivity) context).openRewards();
                            }
                        }
                    });
                    rewardDriverClickListenerSet = true;
                }

                // only apply to fragments that have the card present
                ((TextView) rewardDriverCard.findViewById(R.id.reward_driver_text)).setText(rewardDriverText);
                boolean showRewardsDriver = Lbry.walletBalance == null ||
                        minCost == 0 && Lbry.walletBalance.getAvailable().doubleValue() == 0 |
                        Lbry.walletBalance.getAvailable().doubleValue() < Math.max(minCost, Helper.MIN_DEPOSIT);
                rewardDriverCard.setVisibility(showRewardsDriver ? View.VISIBLE : View.GONE);
            }
        }
    }
}
