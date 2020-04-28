package io.lbry.browser.model;

import lombok.Data;

@Data
public class WalletSync {
    private String hash;
    private String data;
    private boolean changed;

    public WalletSync(String hash, String data, boolean changed) {
        this.hash = hash;
        this.data = data;
        this.changed = changed;
    }
}
