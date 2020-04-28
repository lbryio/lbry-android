package io.lbry.browser.model;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class WalletBalance {
    private BigDecimal available;
    private BigDecimal reserved;
    private BigDecimal claims;
    private BigDecimal supports;
    private BigDecimal tips;
    private BigDecimal total;

    public WalletBalance() {
        available = new BigDecimal(0);
        reserved = new BigDecimal(0);
        claims = new BigDecimal(0);
        supports = new BigDecimal(0);
        tips = new BigDecimal(0);
        total = new BigDecimal(0);
    }
}
