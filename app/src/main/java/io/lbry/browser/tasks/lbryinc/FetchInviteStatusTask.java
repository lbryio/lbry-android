package io.lbry.browser.tasks.lbryinc;

import android.os.AsyncTask;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.lbry.browser.exceptions.LbryioRequestException;
import io.lbry.browser.exceptions.LbryioResponseException;
import io.lbry.browser.model.lbryinc.Invitee;
import io.lbry.browser.utils.Helper;
import io.lbry.browser.utils.Lbryio;

public class FetchInviteStatusTask extends AsyncTask<Void, Void, List<Invitee>> {
    private FetchInviteStatusHandler handler;
    private View progressView;
    private Exception error;

    public FetchInviteStatusTask(View progressView, FetchInviteStatusHandler handler) {
        this.progressView = progressView;
        this.handler = handler;
    }

    protected void onPreExecute() {
        Helper.setViewVisibility(progressView, View.VISIBLE);
    }

    protected List<Invitee> doInBackground(Void... params) {
        List<Invitee> invitees = null;
        try {
            JSONObject status = (JSONObject) Lbryio.parseResponse(Lbryio.call("user", "invite_status", null, null));
            JSONArray inviteesArray = status.getJSONArray("invitees");
            invitees = new ArrayList<>();
            for (int i = 0; i < inviteesArray.length(); i++) {
                JSONObject inviteeObject = inviteesArray.getJSONObject(i);
                Invitee invitee = new Invitee();
                invitee.setEmail(Helper.getJSONString("email", null, inviteeObject));
                invitee.setInviteRewardClaimable(Helper.getJSONBoolean("invite_reward_claimable", false, inviteeObject));
                invitee.setInviteRewardClaimed(Helper.getJSONBoolean("invite_reward_claimed", false, inviteeObject));

                if (!Helper.isNullOrEmpty(invitee.getEmail())) {
                    invitees.add(invitee);
                }
            }
        } catch (ClassCastException | LbryioRequestException | LbryioResponseException | JSONException ex) {
            error = ex;
        }

        return invitees;
    }

    protected void onPostExecute(List<Invitee> invitees) {
        Helper.setViewVisibility(progressView, View.GONE);
        if (handler != null) {
            if (invitees != null) {
                handler.onSuccess(invitees);
            } else {
                handler.onError(error);
            }
        }
    }

    public interface FetchInviteStatusHandler {
        void onSuccess(List<Invitee> invitees);
        void onError(Exception error);
    }
}
