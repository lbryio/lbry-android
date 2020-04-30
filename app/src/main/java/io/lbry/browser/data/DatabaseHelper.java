package io.lbry.browser.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.lbry.browser.exceptions.LbryUriException;
import io.lbry.browser.model.Tag;
import io.lbry.browser.model.UrlSuggestion;
import io.lbry.browser.model.lbryinc.Subscription;
import io.lbry.browser.utils.Helper;
import io.lbry.browser.utils.LbryUri;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "LbryApp.db";

    private static final String[] SQL_CREATE_TABLES = {
            // local subscription store
            "CREATE TABLE subscriptions (url TEXT PRIMARY KEY NOT NULL, channel_name TEXT NOT NULL)",
            // url entry / suggestion history
            "CREATE TABLE history (id INTEGER PRIMARY KEY NOT NULL, value TEXT NOT NULL, url TEXT, type INTEGER NOT NULL, timestamp TEXT NOT NULL)",
            // tags (known and followed)
            "CREATE TABLE tags (id INTEGER PRIMARY KEY NOT NULL, name TEXT NOT NULL, is_followed INTEGER NOT NULL)"
            // local claim cache store for quick load / refresh (or offline mode)?

    };
    private static final String[] SQL_CREATE_INDEXES = {
            "CREATE UNIQUE INDEX idx_subscription_url ON subscriptions (url)",
            "CREATE UNIQUE INDEX idx_history_value ON history (value)",
            "CREATE UNIQUE INDEX idx_history_url ON history (url)",
            "CREATE UNIQUE INDEX idx_tag_name ON tags (name)"
    };

    private static final String SQL_INSERT_SUBSCRIPTION = "REPLACE INTO subscriptions (channel_name, url) VALUES (?, ?)";
    private static final String SQL_DELETE_SUBSCRIPTION = "DELETE FROM subscriptions WHERE url = ?";
    private static final String SQL_GET_SUBSCRIPTIONS = "SELECT channel_name, url FROM subscriptions";

    private static final String SQL_INSERT_HISTORY = "REPLACE INTO history (value, url, type, timestamp) VALUES (?, ?, ?)";
    private static final String SQL_CLEAR_HISTORY = "DELETE FROM history";
    private static final String SQL_CLEAR_HISTORY_BEFORE_TIME = "DELETE FROM history WHERE timestamp < ?";
    private static final String SQL_GET_RECENT_HISTORY = "SELECT value, url, type FROM history ORDER BY timestamp DESC LIMIT 10";

    private static final String SQL_INSERT_TAG = "REPLACE INTO tags (name, is_followed) VALUES (?, ?)";
    private static final String SQL_SET_TAG_FOLLOWED = "UPDATE tags SET is_followed = ? WHERE name = ?";
    private static final String SQL_GET_KNOWN_TAGS = "SELECT name FROM tags";
    private static final String SQL_GET_FOLLOWED_TAGS = "SELECT name FROM tags WHERE is_followed = 1";

    public DatabaseHelper(Context context) {
        super(context, String.format("%s/%s", context.getFilesDir().getAbsolutePath(), DATABASE_NAME), null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        for (String sql : SQL_CREATE_TABLES) {
            db.execSQL(sql);
        }
        for (String sql : SQL_CREATE_INDEXES) {
            db.execSQL(sql);
        }
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public static void createOrUpdateHistoryItem(String text, String url, int type, SQLiteDatabase db) {
        db.execSQL(SQL_INSERT_HISTORY, new Object[] {
                text, url, type, new SimpleDateFormat(Helper.ISO_DATE_FORMAT_PATTERN).format(new Date())
        });
    }
    public static void clearHistory(SQLiteDatabase db) {
        db.execSQL(SQL_CLEAR_HISTORY);
    }
    public static void clearHistoryBefore(Date date, SQLiteDatabase db) {
        db.execSQL(SQL_CLEAR_HISTORY_BEFORE_TIME, new Object[] { new SimpleDateFormat(Helper.ISO_DATE_FORMAT_PATTERN).format(new Date()) });
    }
    // History items are essentially url suggestions
    public static List<UrlSuggestion> getRecentHistory(SQLiteDatabase db) {
        List<UrlSuggestion> suggestions = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(SQL_GET_RECENT_HISTORY, null);
            while (cursor.moveToNext()) {
                UrlSuggestion suggestion = new UrlSuggestion();
                suggestion.setText(cursor.getString(0));
                suggestion.setType(cursor.getInt(2));

                try {
                    suggestion.setUri(cursor.isNull(1) ? null : LbryUri.parse(cursor.getString(1)));
                } catch (LbryUriException ex) {
                    // don't fail if the LbryUri is invalid
                }
                suggestions.add(suggestion);
            }
        } finally {
            Helper.closeCursor(cursor);
        }
        return suggestions;
    }

    public static void createOrUpdateTag(Tag tag, SQLiteDatabase db) {
        db.execSQL(SQL_INSERT_TAG, new Object[] { tag.getLowercaseName(), tag.isFollowed() ? 1 : 0 });
    }
    public static void setTagFollowed(boolean followed, String name, SQLiteDatabase db) {
        db.execSQL(SQL_SET_TAG_FOLLOWED, new Object[] { followed ? 1 : 0, name });
    }
    public static List<Tag> getTags(SQLiteDatabase db) {
        List<Tag> tags = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(SQL_GET_KNOWN_TAGS, null);
            while (cursor.moveToNext()) {
                Tag tag = new Tag();
                tag.setName(cursor.getString(0));
                tag.setFollowed(cursor.getInt(1) == 1);
                tags.add(tag);
            }
        } finally {
            Helper.closeCursor(cursor);
        }
        return tags;
    }

    public static void createOrUpdateSubscription(Subscription subscription, SQLiteDatabase db) {
        db.execSQL(SQL_INSERT_SUBSCRIPTION, new Object[] { subscription.getChannelName(), subscription.getUrl() });
    }
    public static void deleteSubscription(Subscription subscription, SQLiteDatabase db) {
        db.execSQL(SQL_DELETE_SUBSCRIPTION, new Object[] { subscription.getUrl() });
    }
    public static List<Subscription> getSubscriptions(SQLiteDatabase db) {
        List<Subscription> subscriptions = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(SQL_GET_SUBSCRIPTIONS, null);
            while (cursor.moveToNext()) {
                Subscription subscription = new Subscription();
                subscription.setChannelName(cursor.getString(0));
                subscription.setUrl(cursor.getString(1));
                subscriptions.add(subscription);
            }
        } finally {
            Helper.closeCursor(cursor);
        }
        return subscriptions;
    }

}
