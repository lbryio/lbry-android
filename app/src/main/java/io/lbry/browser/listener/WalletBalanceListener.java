package io.lbry.browser.listener;

import io.lbry.browser.model.WalletBalance;

public interface WalletBalanceListener {
    void onWalletBalanceUpdated(WalletBalance walletBalance);
}
