package io.lbry.browser.model.lbryinc;

import lombok.Data;

@Data
public class RewardVerified {
    private long userId;
    private boolean isRewardApproved;
}
