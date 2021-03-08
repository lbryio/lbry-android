package io.lbry.browser.model;

import org.json.JSONObject;

import java.math.BigDecimal;

import io.lbry.browser.utils.Helper;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class WalletBalance {
    @Setter
    @Getter
    private BigDecimal available;
    @Setter
    @Getter
    private BigDecimal reserved;
    @Setter
    @Getter
    private BigDecimal claims;
    @Setter
    @Getter
    private BigDecimal supports;
    @Setter
    @Getter
    private BigDecimal tips;
    @Setter
    @Getter
    private BigDecimal total;

    public WalletBalance() {
        available = new BigDecimal(0);
        reserved = new BigDecimal(0);
        claims = new BigDecimal(0);
        supports = new BigDecimal(0);
        tips = new BigDecimal(0);
        total = new BigDecimal(0);
    }

    public static WalletBalance fromJSONObject(JSONObject json) {
        WalletBalance balance = new WalletBalance();
        JSONObject reservedSubtotals = Helper.getJSONObject("reserved_subtotals", json);
        balance.setAvailable(new BigDecimal(Helper.getJSONString("available", "0", json)));
        balance.setReserved(new BigDecimal(Helper.getJSONString("reserved", "0", json)));
        balance.setTotal(new BigDecimal(Helper.getJSONString("total", "0", json)));
        if (reservedSubtotals != null) {
            balance.setClaims(new BigDecimal(Helper.getJSONString("claims", "0", reservedSubtotals)));
            balance.setSupports(new BigDecimal(Helper.getJSONString("supports", "0", reservedSubtotals)));
            balance.setTips(new BigDecimal(Helper.getJSONString("tips", "0", reservedSubtotals)));
        }
        return balance;
    }
}
