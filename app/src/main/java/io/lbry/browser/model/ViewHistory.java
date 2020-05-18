package io.lbry.browser.model;

import java.math.BigDecimal;
import java.util.Date;

import io.lbry.browser.exceptions.LbryUriException;
import io.lbry.browser.utils.LbryUri;
import io.lbry.browser.utils.Lbryio;
import lombok.Data;

@Data
public class ViewHistory {
    private LbryUri uri;
    private String claimId;
    private String claimName;
    private BigDecimal cost;
    private String currency;
    private String title;
    private String publisherClaimId;
    private String publisherName;
    private String publisherTitle;
    private String thumbnailUrl;
    private String device;
    private long releaseTime;
    private Date timestamp;

    public static ViewHistory fromClaimWithUrlAndDeviceName(Claim claim, String url, String deviceName) {
        ViewHistory history = new ViewHistory();
        LbryUri uri = LbryUri.tryParse(url);
        if (uri == null) {
            uri = LbryUri.tryParse(claim.getPermanentUrl());
        }
        history.setUri(uri);
        history.setClaimId(claim.getClaimId());
        history.setClaimName(claim.getName());
        history.setTitle(claim.getTitle());
        history.setThumbnailUrl(claim.getThumbnailUrl());

        Claim.GenericMetadata metadata = claim.getValue();
        if (metadata instanceof Claim.StreamMetadata) {
            Claim.StreamMetadata value = (Claim.StreamMetadata) metadata;
            history.setReleaseTime(value.getReleaseTime());
            if (value.getFee() != null) {
                Fee fee = value.getFee();
                history.setCost(new BigDecimal(fee.getAmount()));
                history.setCurrency(fee.getCurrency());
            }
        }
        if (history.getReleaseTime() == 0) {
            history.setReleaseTime(claim.getTimestamp());
        }

        Claim signingChannel = claim.getSigningChannel();
        if (signingChannel != null) {
            history.setPublisherClaimId(signingChannel.getClaimId());
            history.setPublisherName(signingChannel.getName());
            history.setPublisherTitle(signingChannel.getTitle());
        }

        history.setDevice(deviceName);

        return history;
    }
}
