package io.lbry.browser.tasks.verification;

import android.os.AsyncTask;

import io.lbry.browser.model.lbryinc.User;
import io.lbry.browser.utils.Lbryio;

public class CheckUserEmailVerifiedTask extends AsyncTask<Void, Void, Boolean> {
    private CheckUserEmailVerifiedHandler handler;

    public CheckUserEmailVerifiedTask(CheckUserEmailVerifiedHandler handler) {
        this.handler = handler;
    }

    protected Boolean doInBackground(Void... params) {
        User user = Lbryio.fetchCurrentUser(null);
        return user != null && user.isHasVerifiedEmail();
    }

    protected void onPostExecute(Boolean result) {
        if (handler != null && result) {
            // we only care if the user has actually verified their email
            handler.onUserEmailVerified();
        }
    }

    public interface CheckUserEmailVerifiedHandler {
        void onUserEmailVerified();
    }
}
