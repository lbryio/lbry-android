package io.lbry.browser.tasks.claim;

import java.util.List;

import io.lbry.browser.model.Claim;

public interface ClaimSearchResultHandler {
    void onSuccess(List<Claim> claims, boolean hasReachedEnd);
    void onError(Exception error);
}
