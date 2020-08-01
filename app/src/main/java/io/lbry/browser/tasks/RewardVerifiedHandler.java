package io.lbry.browser.tasks;

import io.lbry.browser.model.lbryinc.RewardVerified;

public interface RewardVerifiedHandler {
    void onSuccess(RewardVerified rewardVerified);
    void onError(Exception error);
}
