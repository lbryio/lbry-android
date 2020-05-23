package io.lbry.browser.tasks.claim;

import java.util.List;

import io.lbry.browser.model.Claim;

public interface ClaimListResultHandler {
    void onSuccess(List<Claim> claims);
    void onError(Exception error);
}
