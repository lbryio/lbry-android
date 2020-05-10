package io.lbry.browser.tasks.verification;

import android.os.AsyncTask;
import android.view.View;

import java.util.HashMap;
import java.util.Map;

import io.lbry.browser.exceptions.LbryioRequestException;
import io.lbry.browser.exceptions.LbryioResponseException;
import io.lbry.browser.tasks.GenericTaskHandler;
import io.lbry.browser.utils.Helper;
import io.lbry.browser.utils.Lbryio;

public class PhoneNewVerifyTask extends AsyncTask<Void, Void, Boolean> {
    private String countryCode;
    private String phoneNumber;
    private String verificationCode;
    private View progressView;
    private GenericTaskHandler handler;
    private Exception error;

    public PhoneNewVerifyTask(String countryCode, String phoneNumber, String verificationCode, View progressView, GenericTaskHandler handler) {
        this.countryCode = countryCode;
        this.phoneNumber = phoneNumber;
        this.verificationCode = verificationCode;
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
            boolean isVerify = !Helper.isNullOrEmpty(verificationCode);
            Map<String, String> options = new HashMap<>();
            options.put("country_code", countryCode);
            options.put("phone_number", phoneNumber.replace(" ", "").replace("-", ""));
            if (isVerify) {
                options.put("verification_code", verificationCode);
            }

            String action = isVerify ? "phone_number_confirm" : "phone_number_new";
            Lbryio.parseResponse(Lbryio.call("user", action, options, Helper.METHOD_POST, null));
        } catch (LbryioResponseException | LbryioRequestException ex) {
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
}
