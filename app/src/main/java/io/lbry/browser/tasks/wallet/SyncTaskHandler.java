package io.lbry.browser.tasks.wallet;

import io.lbry.browser.model.WalletSync;

public interface SyncTaskHandler {
    void onSyncGetSuccess(WalletSync walletSync);
    void onSyncGetWalletNotFound();
    void onSyncGetError(Exception error);
    void onSyncSetSuccess(String hash);
    void onSyncSetError(Exception error);
    void onSyncApplySuccess(String hash, String data);
    void onSyncApplyError(Exception error);
}
