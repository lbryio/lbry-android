package io.lbry.browser.tasks.localdata;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import io.lbry.browser.MainActivity;
import io.lbry.browser.data.DatabaseHelper;
import io.lbry.browser.model.UrlSuggestion;
import io.lbry.browser.tasks.GenericTaskHandler;

public class CreateUrlHistoryTask extends AsyncTask<Void, Void, Boolean> {
    private Context context;
    private UrlSuggestion suggestion;
    private GenericTaskHandler handler;
    private Exception error;

    public CreateUrlHistoryTask(UrlSuggestion suggestion, Context context, GenericTaskHandler handler) {
        this.suggestion = suggestion;
        this.context = context;
        this.handler = handler;

    }
    protected Boolean doInBackground(Void... params) {
        try {
            SQLiteDatabase db = ((MainActivity) context).getDbHelper().getWritableDatabase();
            DatabaseHelper.createOrUpdateUrlHistoryItem(suggestion.getText(), suggestion.getUri().toString(), suggestion.getType(), db);
        } catch (Exception ex) {
            error = ex;
            return false;
        }

        return true;
    }
    protected void onPostExecute(Boolean result) {
        if (handler != null) {
            if (result) {
                handler.onSuccess();
            } else {
                handler.onError(error);
            }
        }
    }
}
