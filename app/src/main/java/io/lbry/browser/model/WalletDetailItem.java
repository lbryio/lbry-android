package io.lbry.browser.model;

public class WalletDetailItem {
    public String detail;
    public String detailDesc;
    public String detailAmount;
    public boolean isUnlockable;
    public boolean isInProgress;

    public WalletDetailItem(String detail, String detailDesc, String detailAmount, boolean isUnlockable, boolean isInProgress) {
        this.detail = detail;
        this.detailDesc = detailDesc;
        this.detailAmount = detailAmount;
        this.isUnlockable = isUnlockable;
        this.isInProgress = isInProgress;
    }
}
