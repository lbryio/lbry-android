package io.lbry.browser.tasks.lbryinc;

import android.os.AsyncTask;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import io.lbry.browser.exceptions.LbryioRequestException;
import io.lbry.browser.exceptions.LbryioResponseException;
import io.lbry.browser.model.Claim;
import io.lbry.browser.utils.Lbryio;
import okhttp3.Response;

public class LogPublishTask extends AsyncTask<Void, Void, Void> {
    private Claim claimResult;
    public LogPublishTask(Claim claimResult) {
        this.claimResult = claimResult;
    }
    protected Void doInBackground(Void... params) {
        try {
            Map<String, String> options = new HashMap<>();
            options.put("uri", claimResult.getPermanentUrl());
            options.put("claim_id", claimResult.getClaimId());
            options.put("outpoint", String.format("%s:%d", claimResult.getTxid(), claimResult.getNout()));
            if (claimResult.getSigningChannel() != null) {
                options.put("channel_claim_id", claimResult.getSigningChannel().getClaimId());
            }
            Lbryio.call("event", "publish", options,  null).close();
        } catch (LbryioRequestException | LbryioResponseException ex) {
            // pass
        }
        return null;
    }
}
