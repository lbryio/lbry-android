package io.lbry.browser.model.lbryinc;

import java.util.Date;

import lombok.Data;

@Data
public class LbryNotification {
    private long id;
    private String title;
    private String description;
    private String thumbnailUrl;
    private String targetUrl;
    private boolean read;
    private Date timestamp;
}
