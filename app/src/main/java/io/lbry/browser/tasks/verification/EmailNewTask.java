package io.lbry.browser.tasks.verification;

import android.os.AsyncTask;
import android.view.View;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import io.lbry.browser.exceptions.LbryioRequestException;
import io.lbry.browser.exceptions.LbryioResponseException;
import io.lbry.browser.tasks.GenericTaskHandler;
import io.lbry.browser.utils.Helper;
import io.lbry.browser.utils.Lbryio;

public class EmailNewTask extends AsyncTask<Void, Void, Boolean> {
    private String email;
    private View progressView;
    private EmailNewHandler handler;
    private Exception error;

    public EmailNewTask(String email, View progressView, EmailNewHandler handler) {
        this.email = email;
        this.progressView = progressView;
        this.handler = handler;
    }
    protected void onPreExecute() {
        Helper.setViewVisibility(progressView, View.VISIBLE);
        if (handler != null) {
            handler.beforeStart();
        }
    }
    protected Boolean doInBackground(Void... params) {
        try {
            Map<String, String> options = new HashMap<>();
            options.put("email", email);
            options.put("send_verification_email", "true");
            Lbryio.parseResponse(Lbryio.call("user_email", "new", options, Helper.METHOD_POST, null));
        } catch (LbryioResponseException ex) {
            if (ex.getStatusCode() == 409) {
                if (handler != null) {
                    handler.onEmailExists();
                }

                // email already exists
                Map<String, String> options = new HashMap<>();
                options.put("email", email);
                options.put("only_if_expired", "true");
                try {
                    Lbryio.parseResponse(Lbryio.call("user_email", "resend_token", options, Helper.METHOD_POST, null));
                } catch (LbryioRequestException | LbryioResponseException e) {
                    error = e;
                    return false;
                }
            } else {
                error = ex;
                return false;
            }
        } catch (LbryioRequestException ex) {
            error = ex;
            return false;
        }

        return true;
    }
    protected void onPostExecute(Boolean result) {
        Helper.setViewVisibility(progressView, View.GONE);
        if (handler != null) {
            if (result) {
                handler.onSuccess();
            } else {
                handler.onError(error);
            }
        }
    }

    public interface EmailNewHandler extends GenericTaskHandler {
        void onEmailExists();
    }
}
