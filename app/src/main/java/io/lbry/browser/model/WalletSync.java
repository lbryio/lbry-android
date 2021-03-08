package io.lbry.browser.model;

import lombok.Data;
import lombok.Getter;

@Data
public class WalletSync {
    @Getter
    private final String hash;
    @Getter
    private final String data;
    @Getter
    private boolean changed;

    public WalletSync(String hash, String data) {
        this.hash = hash;
        this.data = data;
    }

    public WalletSync(String hash, String data, boolean changed) {
        this(hash, data);
        this.changed = changed;
    }

}
