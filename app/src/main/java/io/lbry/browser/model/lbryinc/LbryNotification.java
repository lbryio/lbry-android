package io.lbry.browser.model.lbryinc;

import java.util.Date;

import lombok.Data;

@Data
public class LbryNotification {
    private long id;
    private long remoteId;
    private String title;
    private String description;
    private String rule;
    private String thumbnailUrl;
    private String targetUrl;
    private boolean read;
    private boolean seen;
    private Date timestamp;
}
