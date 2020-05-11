package io.lbry.browser.tasks.lbryinc;

import android.os.AsyncTask;

import io.lbry.browser.model.lbryinc.User;
import io.lbry.browser.utils.Lbryio;

public class FetchCurrentUserTask extends AsyncTask<Void, Void, User> {
    private Exception error;
    private FetchUserTaskHandler handler;

    public FetchCurrentUserTask(FetchUserTaskHandler handler) {
        this.handler = handler;
    }
    protected User doInBackground(Void... params) {
        try {
            return Lbryio.fetchCurrentUser(null);
        } catch (Exception ex) {
            error = ex;
            return null;
        }
    }

    protected void onPostExecute(User result) {
        if (handler != null) {
            if (result != null) {
                handler.onSuccess(result);
            } else {
                handler.onError(error);
            }
        }
    }

    public interface FetchUserTaskHandler {
        void onSuccess(User user);
        void onError(Exception error);
    }
}
