package io.lbry.browser.tasks.claim;

import io.lbry.browser.model.Claim;

public interface ClaimResultHandler {
    void beforeStart();
    void onSuccess(Claim claimResult);
    void onError(Exception error);
}
