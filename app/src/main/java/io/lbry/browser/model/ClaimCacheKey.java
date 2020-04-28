package io.lbry.browser.model;

import androidx.annotation.Nullable;

import io.lbry.browser.utils.Helper;
import lombok.Getter;
import lombok.Setter;

/**
 * Class to represent a key to check equality with another object
 */
public class ClaimCacheKey {
    @Getter
    @Setter
    private String claimId;
    @Getter
    @Setter
    private String canonicalUrl;
    @Getter
    @Setter
    private String permanentUrl;
    @Getter
    @Setter
    private String shortUrl;
    @Getter
    @Setter
    private String vanityUrl;

    public static ClaimCacheKey fromClaim(Claim claim) {
        ClaimCacheKey key = new ClaimCacheKey();
        key.setClaimId(claim.getClaimId());
        key.setCanonicalUrl(claim.getCanonicalUrl());
        key.setPermanentUrl(claim.getPermanentUrl());
        key.setShortUrl(claim.getShortUrl());

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
        if (!Helper.isNullOrEmpty(permanentUrl) && !Helper.isNullOrEmpty(key.getPermanentUrl())) {
            return permanentUrl.equalsIgnoreCase(key.getPermanentUrl());
        }
        if (!Helper.isNullOrEmpty(canonicalUrl) && !Helper.isNullOrEmpty(key.getCanonicalUrl())) {
            return canonicalUrl.equalsIgnoreCase(key.getCanonicalUrl());
        }
        if (!Helper.isNullOrEmpty(shortUrl) && !Helper.isNullOrEmpty(key.getShortUrl())) {
            return shortUrl.equalsIgnoreCase(key.getShortUrl());
        }
        if (!Helper.isNullOrEmpty(vanityUrl) && !Helper.isNullOrEmpty(key.getVanityUrl())) {
            return vanityUrl.equalsIgnoreCase(key.getVanityUrl());
        }

        return false;
    }

    @Override
    public int hashCode() {
        if (!Helper.isNullOrEmpty(claimId)) {
            return claimId.hashCode();
        }
        if (!Helper.isNullOrEmpty(permanentUrl)) {
            return permanentUrl.hashCode();
        }
        if (!Helper.isNullOrEmpty(canonicalUrl)) {
            return canonicalUrl.hashCode();
        }
        if (!Helper.isNullOrEmpty(shortUrl)) {
            return shortUrl.hashCode();
        }
        if (!Helper.isNullOrEmpty(vanityUrl)) {
            return vanityUrl.hashCode();
        }

        return super.hashCode();
    }
}

