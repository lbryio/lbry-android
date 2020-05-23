package io.lbry.browser.tasks;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import io.lbry.browser.MainActivity;
import io.lbry.browser.data.DatabaseHelper;
import io.lbry.browser.model.Tag;
import io.lbry.browser.utils.Lbry;

public class FollowUnfollowTagTask extends AsyncTask<Void, Void, Boolean> {
    private Tag tag;
    private boolean unfollowing;
    private Context context;
    private FollowUnfollowTagHandler handler;
    private Exception error;

    public FollowUnfollowTagTask(Tag tag, boolean unfollowing, Context context, FollowUnfollowTagHandler handler) {
        this.tag = tag;
        this.context = context;
        this.unfollowing = unfollowing;
        this.handler = handler;
    }
    public Boolean doInBackground(Void... params) {
        try {
            SQLiteDatabase db = null;
            if (context instanceof MainActivity) {
                db = ((MainActivity) context).getDbHelper().getWritableDatabase();
                if (db != null) {
                    if (!Lbry.knownTags.contains(tag)) {
                        DatabaseHelper.createOrUpdateTag(tag, db);
                        Lbry.addKnownTag(tag);
                    }

                    tag.setFollowed(!unfollowing);
                    DatabaseHelper.createOrUpdateTag(tag, db);
                    if (unfollowing) {
                        Lbry.removeFollowedTag(tag);
                    } else {
                        Lbry.addFollowedTag(tag);
                    }
                    return true;
                }
            }
        } catch (Exception ex) {
            error = ex;
        }
        return false;
    }
    protected void onPostExecute(Boolean result) {
        if (handler != null) {
            if (result) {
                handler.onSuccess(tag, unfollowing);
            } else {
                handler.onError(error);
            }
        }
    }

    public interface FollowUnfollowTagHandler {
        void onSuccess(Tag tag, boolean unfollowing);
        void onError(Exception error);
    }
}
