package io.lbry.browser.model.lbryinc;

import java.util.List;

import lombok.Data;

@Data
public class User {
    private String createdAt;
    private String familyName;
    private String givenName;
    private List<String> groups;
    private boolean hasVerifiedEmail;
    private long id;
    private boolean inviteRewardClaimed;
    private String invitedAt;
    private long inivtedById;
    private int invitesRemaining;
    private boolean isEmailEnabled;
    private boolean isIdentityVerified;
    private boolean isRewardApproved;
    private String language;
    private long manualApprovalUserId;
    private String primaryEmail;
    private String rewardStatusChangeTrigger;
    private String updatedAt;
    private List<YoutubeChannel> youtubeChannels;
    private List<String> deviceTypes;

    @Data
    public static class YoutubeChannel {
        String ytChannelName;
        String lbryChannelName;
        String channelClaimId;
        String syncStatus;
        String statusToken;
        boolean transferable;
        String transferState;
        List<String> publishToAddress;
        String publicKey;
    }
}
