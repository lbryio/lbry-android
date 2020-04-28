package io.lbry.browser.model.lbryinc;

import lombok.Data;

@Data
public class Subscription {
    private String channelName;
    private String url;

    public Subscription() {

    }
    public Subscription(String channelName, String url) {
        this.channelName = channelName;
        this.url = url;
    }
}
