package io.lbry.browser.tasks.localdata;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import io.lbry.browser.MainActivity;
import io.lbry.browser.data.DatabaseHelper;
import io.lbry.browser.model.UrlSuggestion;
import io.lbry.browser.tasks.GenericTaskHandler;

public class SaveUrlHistoryTask extends AsyncTask<Void, Void, Boolean> {
    private DatabaseHelper dbHelper;
    private UrlSuggestion suggestion;
    private SaveUrlHistoryHandler handler;
    private Exception error;

    public SaveUrlHistoryTask(UrlSuggestion suggestion, DatabaseHelper dbHelper, SaveUrlHistoryHandler handler) {
        this.suggestion = suggestion;
        this.dbHelper = dbHelper;
        this.handler = handler;

    }
    protected Boolean doInBackground(Void... params) {
        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
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
                handler.onSuccess(suggestion);
            } else {
                handler.onError(error);
            }
        }
    }

    public interface SaveUrlHistoryHandler {
        void onSuccess(UrlSuggestion item);
        void onError(Exception error);
    }
}
