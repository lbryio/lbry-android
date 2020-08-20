package io.lbry.browser.model.lbryinc;

import java.util.Comparator;
import java.util.Date;

import io.lbry.browser.model.Claim;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class LbryNotification implements Comparator<LbryNotification> {
    private long id;
    @EqualsAndHashCode.Include
    private long remoteId;
    private String title;
    private String description;
    private String thumbnailUrl;
    private String rule;
    private String targetUrl;
    private boolean read;
    private boolean seen;
    private Date timestamp;

    // only for comment notifications
    private String authorUrl;
    private Claim commentAuthor;

    public int compare(LbryNotification a, LbryNotification b) {
        long t1 = a.getTimestamp() != null ? a.getTimestamp().getTime() : 0;
        long t2 = b.getTimestamp() != null ? b.getTimestamp().getTime() : 0;
        if (t1 < t2) {
            return -1;
        }
        if (t1 > t2) {
            return 1;
        }
        return 0;
    }
}
