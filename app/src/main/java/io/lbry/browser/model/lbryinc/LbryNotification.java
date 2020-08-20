package io.lbry.browser.model.lbryinc;

import java.util.Date;

import io.lbry.browser.model.Claim;
import lombok.Data;

@Data
public class LbryNotification {
    private long id;
    private long remoteId;
    private String title;
    private String description;
    private String thumbnailUrl;
    private String rule;
    private String targetUrl;
    private boolean read;
    private boolean seen;
    private Date timestamp;

    // only for comment notifications
    private String authorUrl;
    private Claim commentAuthor;
}
