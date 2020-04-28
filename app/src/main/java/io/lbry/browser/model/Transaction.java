package io.lbry.browser.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.Date;

import io.lbry.browser.R;
import io.lbry.browser.utils.Helper;
import lombok.Data;

@Data
public class Transaction {
    private int confirmations;
    private Date txDate;
    private String date;
    private String claim;
    private String txid;
    private BigDecimal value;
    private BigDecimal fee;
    private long timestamp;
    private int descriptionStringId;
    private TransactionInfo abandonInfo;
    private TransactionInfo claimInfo;
    private TransactionInfo supportInfo;

    public static Transaction fromJSONObject(JSONObject jsonObject) {
        Transaction transaction = new Transaction();
        transaction.setConfirmations(Helper.getJSONInt("confirmations", -1, jsonObject));
        transaction.setDate(Helper.getJSONString("date", null, jsonObject));
        transaction.setTxid(Helper.getJSONString("txid", null, jsonObject));
        transaction.setValue(new BigDecimal(Helper.getJSONString("value", "0", jsonObject)));
        transaction.setFee(new BigDecimal(Helper.getJSONString("fee", "0", jsonObject)));
        transaction.setTimestamp(Helper.getJSONLong("timestamp", 0, jsonObject) * 1000);
        transaction.setTxDate(new Date(transaction.getTimestamp()));

        int descStringId = -1;
        TransactionInfo info = null;
        try {
            if (jsonObject.has("abandon_info")) {
                info = TransactionInfo.fromJSONObject(jsonObject.getJSONObject("abandon_info"));
                descStringId = R.string.abandon;
                transaction.setAbandonInfo(info);
            } else if (jsonObject.has("claim_info")) {
                info = TransactionInfo.fromJSONObject(jsonObject.getJSONObject("claim_info"));
                descStringId = info.getClaimName().startsWith("@") ? R.string.channel : R.string.publish;
                transaction.setClaimInfo(info);
            } else if (jsonObject.has("support_info")) {
                info = TransactionInfo.fromJSONObject(jsonObject.getJSONObject("support_info"));
                descStringId = info.isTip() ? R.string.tip : R.string.support;
                transaction.setSupportInfo(info);
            }

            if (info != null) {
                transaction.setClaim(info.getClaimName());
            }
        } catch (JSONException ex) {
            // pass
        }

        if (descStringId == -1) {
            descStringId = transaction.getValue().signum() == -1 || transaction.getFee().signum() == -1 ? R.string.spend : R.string.receive;
        }
        transaction.setDescriptionStringId(descStringId);

        return transaction;
    }

    @Data
    public static class TransactionInfo {
        private String address;
        private BigDecimal amount;
        private String claimId;
        private String claimName;
        private boolean isTip;
        private int nout;

        public static TransactionInfo fromJSONObject(JSONObject jsonObject) {
            TransactionInfo info = new TransactionInfo();

            info.setAddress(Helper.getJSONString("address", null, jsonObject));
            info.setAmount(new BigDecimal(Helper.getJSONString("amount", "0", jsonObject)));
            info.setClaimId(Helper.getJSONString("claim_id", null, jsonObject));
            info.setClaimName(Helper.getJSONString("claim_name", null, jsonObject));
            info.setTip(Helper.getJSONBoolean("is_tip", false, jsonObject));
            info.setNout(Helper.getJSONInt("nout", -1, jsonObject));

            return info;
        }
    }
}
