package io.lbry.browser.tasks.localdata;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.lbry.browser.data.DatabaseHelper;
import io.lbry.browser.model.UrlSuggestion;
import io.lbry.browser.model.ViewHistory;
import io.lbry.browser.utils.Helper;

public class FetchViewHistoryTask extends AsyncTask<Void, Void, List<ViewHistory>> {
    private DatabaseHelper dbHelper;
    private FetchViewHistoryHandler handler;
    private int pageSize;
    private Date lastDate;
    public FetchViewHistoryTask(Date lastDate, int pageSize, DatabaseHelper dbHelper, FetchViewHistoryHandler handler) {
        this.lastDate = lastDate;
        this.pageSize = pageSize;
        this.dbHelper = dbHelper;
        this.handler = handler;
    }
    protected List<ViewHistory> doInBackground(Void... params) {
        try {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            return DatabaseHelper.getViewHistory(
                    lastDate == null ? null : new SimpleDateFormat(Helper.ISO_DATE_FORMAT_PATTERN).format(lastDate), pageSize, db);
        } catch (SQLiteException ex) {
            return new ArrayList<>();
        }
    }
    protected void onPostExecute(List<ViewHistory> history) {
        if (handler != null) {
            handler.onSuccess(history, history.size() < pageSize);
        }
    }

    public interface FetchViewHistoryHandler {
        void onSuccess(List<ViewHistory> history, boolean hasReachedEnd);
    }
}
