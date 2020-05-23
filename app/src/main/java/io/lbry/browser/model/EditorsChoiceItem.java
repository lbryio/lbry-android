package io.lbry.browser.model;

import lombok.Data;

@Data
public class EditorsChoiceItem {
    private boolean header;
    private String title;
    private String parent;
    private String description;
    private String thumbnailUrl;
    private String permanentUrl;

    public static EditorsChoiceItem fromClaim(Claim claim) {
        EditorsChoiceItem item = new EditorsChoiceItem();
        item.setTitle(claim.getTitle());
        item.setDescription(claim.getDescription());
        item.setThumbnailUrl(claim.getThumbnailUrl());
        item.setPermanentUrl(claim.getPermanentUrl());

        return item;
    }
}
