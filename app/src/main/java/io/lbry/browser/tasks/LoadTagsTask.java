package io.lbry.browser.tasks;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;

import java.util.List;

import io.lbry.browser.MainActivity;
import io.lbry.browser.data.DatabaseHelper;
import io.lbry.browser.model.Tag;

public class LoadTagsTask extends AsyncTask<Void, Void, List<Tag>> {
    private final Context context;
    private final LoadTagsHandler handler;
    private Exception error;

    public LoadTagsTask(Context context, LoadTagsHandler handler) {
        this.context = context;
        this.handler = handler;
    }
    protected List<Tag> doInBackground(Void... params) {
        List<Tag> tags = null;
        SQLiteDatabase db = null;
        try {
            if (context instanceof MainActivity) {
                db = ((MainActivity) context).getDbHelper().getReadableDatabase();
                if (db != null) {
                    tags = DatabaseHelper.getTags(db);
                }
            }
        } catch (SQLiteException ex) {
            error = ex;
        }

        return tags;
    }
    protected void onPostExecute(List<Tag> tags) {
        if (handler != null) {
            if (tags != null) {
                handler.onSuccess(tags);
            } else {
                handler.onError(error);
            }
        }
    }

    public interface LoadTagsHandler {
        void onSuccess(List<Tag> tags);
        void onError(Exception error);
    }
}
