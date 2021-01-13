package io.lbry.browser.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

public class ClaimSearchCacheValue {
    @Getter
    private final List<Claim> claims;
    @Getter
    private final long timestamp;

    public ClaimSearchCacheValue(List<Claim> claims, long timestamp) {
        this.claims = new ArrayList<>(claims);
        this.timestamp = timestamp;
    }

    public boolean isExpired(long ttl) {
        return System.currentTimeMillis() - timestamp > ttl;
    }
}
