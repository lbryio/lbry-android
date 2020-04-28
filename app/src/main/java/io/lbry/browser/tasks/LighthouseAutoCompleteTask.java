package io.lbry.browser.tasks;

import android.os.AsyncTask;
import android.view.View;

import java.util.List;

import io.lbry.browser.exceptions.LbryRequestException;
import io.lbry.browser.exceptions.LbryResponseException;
import io.lbry.browser.model.UrlSuggestion;
import io.lbry.browser.utils.Helper;
import io.lbry.browser.utils.Lighthouse;

public class LighthouseAutoCompleteTask extends AsyncTask<Void, Void, List<UrlSuggestion>> {
    private String text;
    private AutoCompleteResultHandler handler;
    private View progressView;
    private Exception error;

    public LighthouseAutoCompleteTask(String text, View progressView, AutoCompleteResultHandler handler) {
        this.text = text;
        this.progressView = progressView;
        this.handler = handler;
    }
    protected void onPreExecute() {
        Helper.setViewVisibility(progressView, View.VISIBLE);
    }
    protected List<UrlSuggestion> doInBackground(Void... params) {
        try {
            return Lighthouse.autocomplete(text);
        } catch (LbryRequestException | LbryResponseException ex) {
            error = ex;
            return null;
        }
    }
    protected void onPostExecute(List<UrlSuggestion> suggestions) {
        Helper.setViewVisibility(progressView, View.GONE);
        if (handler != null) {
            if (suggestions != null) {
                handler.onSuccess(suggestions);
            } else {
                handler.onError(error);
            }
        }
    }

    public interface AutoCompleteResultHandler {
        void onSuccess(List<UrlSuggestion> suggestions);
        void onError(Exception error);
    }
}
