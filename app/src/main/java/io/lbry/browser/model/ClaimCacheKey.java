package io.lbry.browser.model;

import androidx.annotation.Nullable;

import io.lbry.browser.utils.Helper;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Class to represent a key to check equality with another object
 */
@ToString
public class ClaimCacheKey {
    @Getter
    @Setter
    private String claimId;
    @Getter
    @Setter
    private String url;

    public static ClaimCacheKey fromClaimShortUrl(Claim claim) {
        ClaimCacheKey key = new ClaimCacheKey();
        key.setUrl(claim.getShortUrl());
        return key;
    }

    public static ClaimCacheKey fromClaimPermanentUrl(Claim claim) {
        ClaimCacheKey key = new ClaimCacheKey();
        key.setUrl(claim.getPermanentUrl());
        return key;
    }

    public static ClaimCacheKey fromClaim(Claim claim) {
        ClaimCacheKey key = new ClaimCacheKey();
        key.setClaimId(claim.getClaimId());
        key.setUrl(!Helper.isNullOrEmpty(claim.getShortUrl()) ? claim.getShortUrl() : claim.getPermanentUrl());
        return key;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null || !(obj instanceof ClaimCacheKey)) {
            return false;
        }
        ClaimCacheKey key = (ClaimCacheKey) obj;
        if (!Helper.isNullOrEmpty(claimId) && !Helper.isNullOrEmpty(key.getClaimId())) {
            return claimId.equalsIgnoreCase(key.getClaimId());
        }
        if (!Helper.isNullOrEmpty(url) && !Helper.isNullOrEmpty(key.getUrl())) {
            return url.equalsIgnoreCase(key.getUrl());
        }
        return false;
    }

    @Override
    public int hashCode() {
        if (!Helper.isNullOrEmpty(url)) {
            return url.hashCode();
        }
        if (!Helper.isNullOrEmpty(claimId)) {
            return claimId.hashCode();
        }

        return super.hashCode();
    }
}

