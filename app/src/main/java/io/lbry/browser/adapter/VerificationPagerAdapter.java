package io.lbry.browser.adapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import io.lbry.browser.listener.SignInListener;
import io.lbry.browser.listener.WalletSyncListener;
import io.lbry.browser.ui.verification.EmailVerificationFragment;
import io.lbry.browser.ui.verification.WalletVerificationFragment;
import lombok.SneakyThrows;

/**
 * 4 fragments
 * - Email collect / verify (sign in)
 * - Phone number collect / verify (rewards)
 * - Wallet password
 * - Manual verification page
 */
public class VerificationPagerAdapter extends FragmentStateAdapter {
    private FragmentActivity activity;

    public VerificationPagerAdapter(FragmentActivity activity) {
        super(activity);
        this.activity = activity;
    }

    @SneakyThrows
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
            default:
                EmailVerificationFragment evFragment = EmailVerificationFragment.class.newInstance();
                if (activity instanceof SignInListener) {
                    evFragment.setListener((SignInListener) activity);
                }
                return evFragment;
            case 1:
                WalletVerificationFragment wvFragment = WalletVerificationFragment.class.newInstance();
                if (activity instanceof WalletSyncListener) {
                    wvFragment.setListener((WalletSyncListener) activity);
                }
                return wvFragment;
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
