package io.lbry.browser.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.Date;

import io.lbry.browser.R;
import io.lbry.browser.exceptions.LbryUriException;
import io.lbry.browser.utils.Helper;
import io.lbry.browser.utils.LbryUri;
import lombok.Data;

@Data
public class Transaction {
    private int confirmations;
    private Date txDate;
    private String date;
    private String claim;
    private String claimId;
    private String txid;
    private BigDecimal value;
    private BigDecimal fee;
    private long timestamp;
    private int descriptionStringId;
    private TransactionInfo abandonInfo;
    private TransactionInfo claimInfo;
    private TransactionInfo supportInfo;

    public LbryUri getClaimUrl() {
        if (!Helper.isNullOrEmpty(claim) && !Helper.isNullOrEmpty(claimId)) {
            try {
                return LbryUri.parse(LbryUri.normalize(String.format("%s#%s", claim, claimId)));
            } catch (LbryUriException ex) {
                // pass
            }
        }
        return null;
    }

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
                JSONArray array = jsonObject.getJSONArray("abandon_info");
                if (array.length() > 0) {
                    info = TransactionInfo.fromJSONObject(array.getJSONObject(0));
                    descStringId = R.string.abandon;
                    transaction.setAbandonInfo(info);
                }
            }
            if (info == null && jsonObject.has("claim_info")) {
                JSONArray array = jsonObject.getJSONArray("claim_info");
                if (array.length() > 0) {
                    info = TransactionInfo.fromJSONObject(array.getJSONObject(0));
                    descStringId = info.getClaimName().startsWith("@") ? R.string.channel : R.string.publish;
                    transaction.setClaimInfo(info);
                }
            }
            if (info == null && jsonObject.has("support_info")) {
                JSONArray array = jsonObject.getJSONArray("support_info");
                if (array.length() > 0) {
                    info = TransactionInfo.fromJSONObject(array.getJSONObject(0));
                    descStringId = info.isTip() ? R.string.tip : R.string.support;
                    transaction.setSupportInfo(info);
                }
            }
            if (info != null) {
                transaction.setClaim(info.getClaimName());
                transaction.setClaimId(info.getClaimId());
            }
        } catch (JSONException ex) {
            // pass
        }

        if (transaction.getValue().doubleValue() == 0 && info != null && info.getBalanceDelta().doubleValue() != 0) {
            transaction.setValue(info.getBalanceDelta());
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
        private BigDecimal balanceDelta;
        private String claimId;
        private String claimName;
        private boolean isTip;
        private int nout;

        public static TransactionInfo fromJSONObject(JSONObject jsonObject) {
            TransactionInfo info = new TransactionInfo();

            info.setAddress(Helper.getJSONString("address", null, jsonObject));
            info.setAmount(new BigDecimal(Helper.getJSONString("amount", "0", jsonObject)));
            info.setBalanceDelta(new BigDecimal(Helper.getJSONString("balance_delta", "0", jsonObject)));
            info.setClaimId(Helper.getJSONString("claim_id", null, jsonObject));
            info.setClaimName(Helper.getJSONString("claim_name", null, jsonObject));
            info.setTip(Helper.getJSONBoolean("is_tip", false, jsonObject));
            info.setNout(Helper.getJSONInt("nout", -1, jsonObject));

            return info;
        }
    }
}
