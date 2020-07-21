package io.lbry.browser.model.lbryinc;

import lombok.Data;

@Data
public class Notification {
    private long id;
    private String title;
    private String description;
    private String thumbnailUrl;
    private String targetUrl;
}
