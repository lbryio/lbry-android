package io.lbry.browser.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

public class ClaimSearchCacheValue {
    @Getter
    @Setter
    private List<Claim> claims;
    @Getter
    @Setter
    private long timestamp;

    public ClaimSearchCacheValue(List<Claim> claims, long timestamp) {
        this.claims = new ArrayList<>(claims);
        this.timestamp = timestamp;
    }

    public boolean isExpired(long ttl) {
        return System.currentTimeMillis() - timestamp > ttl;
    }
}
