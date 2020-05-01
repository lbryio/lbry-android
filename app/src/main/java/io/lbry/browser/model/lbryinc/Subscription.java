package io.lbry.browser.model.lbryinc;

import lombok.Getter;
import lombok.Setter;

public class Subscription {
    @Getter
    @Setter
    private String channelName;
    @Getter
    @Setter
    private String url;

    public Subscription() {

    }
    public Subscription(String channelName, String url) {
        this.channelName = channelName;
        this.url = url;
    }

    public boolean equals(Object o) {
        return (o instanceof Subscription) && url.equalsIgnoreCase(((Subscription) o).getUrl());
    }
    public int hashCode() {
        return url.toLowerCase().hashCode();
    }
}
