package io.lbry.browser.tasks.localdata;

import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import io.lbry.browser.data.DatabaseHelper;
import io.lbry.browser.model.ViewHistory;

public class SaveViewHistoryTask extends AsyncTask<Void, Void, Boolean> {
    private DatabaseHelper dbHelper;
    private ViewHistory history;
    private SaveViewHistoryHandler handler;
    private Exception error;

    public SaveViewHistoryTask(ViewHistory history, DatabaseHelper dbHelper, SaveViewHistoryHandler handler) {
        this.history = history;
        this.dbHelper = dbHelper;
        this.handler = handler;
    }
    protected Boolean doInBackground(Void... params) {
        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            DatabaseHelper.createOrUpdateViewHistoryItem(history, db);
        } catch (Exception ex) {
            error = ex;
            return false;
        }

        return true;
    }
    protected void onPostExecute(Boolean result) {
        if (handler != null) {
            if (result) {
                handler.onSuccess(history);
            } else {
                handler.onError(error);
            }
        }
    }

    public interface SaveViewHistoryHandler {
        void onSuccess(ViewHistory item);
        void onError(Exception error);
    }
}
