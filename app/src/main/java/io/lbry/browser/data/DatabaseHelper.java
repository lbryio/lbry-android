package io.lbry.browser.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.opengl.Visibility;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.lbry.browser.exceptions.LbryUriException;
import io.lbry.browser.model.Tag;
import io.lbry.browser.model.UrlSuggestion;
import io.lbry.browser.model.ViewHistory;
import io.lbry.browser.model.lbryinc.Subscription;
import io.lbry.browser.utils.Helper;
import io.lbry.browser.utils.LbryUri;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "LbryApp.db";
    private static DatabaseHelper instance;

    private static final String[] SQL_CREATE_TABLES = {
            // local subscription store
            "CREATE TABLE subscriptions (url TEXT PRIMARY KEY NOT NULL, channel_name TEXT NOT NULL)",
            // url entry / suggestion history
            "CREATE TABLE url_history (id INTEGER PRIMARY KEY NOT NULL, value TEXT NOT NULL, url TEXT, type INTEGER NOT NULL, timestamp TEXT NOT NULL)",
            // tags (known and followed)
            "CREATE TABLE tags (id INTEGER PRIMARY KEY NOT NULL, name TEXT NOT NULL, is_followed INTEGER NOT NULL)",
            // view history (stores only stream claims that have resolved)
            "CREATE TABLE view_history (" +
                    "  id INTEGER PRIMARY KEY NOT NULL" +
                    ", url TEXT NOT NULL" +
                    ", claim_id TEXT" +
                    ", claim_name TEXT" +
                    ", cost REAL " +
                    ", currency TEXT " +
                    ", title TEXT " +
                    ", publisher_claim_id TEXT" +
                    ", publisher_name TEXT" +
                    ", publisher_title TEXT" +
                    ", thumbnail_url TEXT" +
                    ", release_time INTEGER " +
                    ", device TEXT" +
                    ", timestamp TEXT NOT NULL)"
    };
    private static final String[] SQL_CREATE_INDEXES = {
            "CREATE UNIQUE INDEX idx_subscription_url ON subscriptions (url)",
            "CREATE UNIQUE INDEX idx_url_history_value ON url_history (value)",
            "CREATE UNIQUE INDEX idx_url_history_url ON url_history (url)",
            "CREATE UNIQUE INDEX idx_tag_name ON tags (name)",
            "CREATE UNIQUE INDEX idx_view_history_url_device ON view_history (url, device)",
            "CREATE INDEX idx_view_history_device ON view_history (device)"
    };

    private static final String[] SQL_V1_V2_UPGRADE = {
            "ALTER TABLE view_history ADD COLUMN currency TEXT"
    };

    private static final String SQL_INSERT_SUBSCRIPTION = "REPLACE INTO subscriptions (channel_name, url) VALUES (?, ?)";
    private static final String SQL_DELETE_SUBSCRIPTION = "DELETE FROM subscriptions WHERE url = ?";
    private static final String SQL_GET_SUBSCRIPTIONS = "SELECT channel_name, url FROM subscriptions";

    private static final String SQL_INSERT_URL_HISTORY = "REPLACE INTO url_history (value, url, type, timestamp) VALUES (?, ?, ?, ?)";
    private static final String SQL_CLEAR_URL_HISTORY = "DELETE FROM url_history";
    private static final String SQL_CLEAR_URL_HISTORY_BEFORE_TIME = "DELETE FROM url_history WHERE timestamp < ?";
    private static final String SQL_GET_RECENT_URL_HISTORY = "SELECT value, url, type FROM url_history ORDER BY timestamp DESC LIMIT 10";

    private static final String SQL_INSERT_VIEW_HISTORY =
            "REPLACE INTO view_history (url, claim_id, claim_name, cost, currency, title, publisher_claim_id, publisher_name, publisher_title, thumbnail_url, device, release_time, timestamp) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String SQL_GET_VIEW_HISTORY =
            "SELECT url, claim_id, claim_name, cost, currency, title, publisher_claim_id, publisher_name, publisher_title, thumbnail_url, device, release_time, timestamp " +
            "FROM view_history WHERE '' = ? OR timestamp < ? ORDER BY timestamp DESC LIMIT %d";
    private static final String SQL_CLEAR_VIEW_HISTORY = "DELETE FROM view_history";
    private static final String SQL_CLEAR_VIEW_HISTORY_BY_DEVICE = "DELETE FROM view_history WHERE device = ?";
    private static final String SQL_CLEAR_VIEW_HISTORY_BEFORE_TIME = "DELETE FROM view_history WHERE timestamp < ?";
    private static final String SQL_CLEAR_VIEW_HISTORY_BY_DEVICE_BEFORE_TIME = "DELETE FROM view_history WHERE device = ? AND timestamp < ?";

    private static final String SQL_INSERT_TAG = "REPLACE INTO tags (name, is_followed) VALUES (?, ?)";
    private static final String SQL_GET_KNOWN_TAGS = "SELECT name, is_followed FROM tags";
    private static final String SQL_UNFOLLOW_TAGS = "UPDATE tags SET is_followed = 0";
    private static final String SQL_GET_FOLLOWED_TAGS = "SELECT name FROM tags WHERE is_followed = 1";



    public DatabaseHelper(Context context) {
        super(context, String.format("%s/%s", context.getFilesDir().getAbsolutePath(), DATABASE_NAME), null, DATABASE_VERSION);
        instance = this;
    }
    public static DatabaseHelper getInstance() {
        return instance;
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
        if (oldVersion < 2) {
            for (String sql : SQL_V1_V2_UPGRADE) {
                db.execSQL(sql);
            }
        }
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public static void createOrUpdateUrlHistoryItem(String text, String url, int type, SQLiteDatabase db) {
        db.execSQL(SQL_INSERT_URL_HISTORY, new Object[] {
                text, url, type, new SimpleDateFormat(Helper.ISO_DATE_FORMAT_PATTERN).format(new Date())
        });
    }
    public static void clearUrlHistory(SQLiteDatabase db) {
        db.execSQL(SQL_CLEAR_URL_HISTORY);
    }
    public static void clearUrlHistoryBefore(Date date, SQLiteDatabase db) {
        db.execSQL(SQL_CLEAR_URL_HISTORY_BEFORE_TIME, new Object[] { new SimpleDateFormat(Helper.ISO_DATE_FORMAT_PATTERN).format(new Date()) });
    }
    // History items are essentially url suggestions
    public static List<UrlSuggestion> getRecentHistory(SQLiteDatabase db) {
        List<UrlSuggestion> suggestions = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(SQL_GET_RECENT_URL_HISTORY, null);
            while (cursor.moveToNext()) {
                UrlSuggestion suggestion = new UrlSuggestion();
                suggestion.setText(cursor.getString(0));
                suggestion.setUri(cursor.isNull(1) ? null : LbryUri.tryParse(cursor.getString(1)));
                suggestion.setType(cursor.getInt(2));
                suggestion.setTitleUrlOnly(true);
                suggestions.add(suggestion);
            }
        } finally {
            Helper.closeCursor(cursor);
        }
        return suggestions;
    }

    // View history items are stream claims
    public static void createOrUpdateViewHistoryItem(ViewHistory viewHistory, SQLiteDatabase db) {
        db.execSQL(SQL_INSERT_VIEW_HISTORY, new Object[] {
                viewHistory.getUri().toString(),
                viewHistory.getClaimId(),
                viewHistory.getClaimName(),
                viewHistory.getCost() != null ? viewHistory.getCost().doubleValue() : 0,
                viewHistory.getCurrency(),
                viewHistory.getTitle(),
                viewHistory.getPublisherClaimId(),
                viewHistory.getPublisherName(),
                viewHistory.getPublisherTitle(),
                viewHistory.getThumbnailUrl(),
                viewHistory.getDevice(),
                viewHistory.getReleaseTime(),
                new SimpleDateFormat(Helper.ISO_DATE_FORMAT_PATTERN).format(new Date())
        });
    }

    public static List<ViewHistory> getViewHistory(String lastTimestamp, int pageLimit, SQLiteDatabase db) {
        List<ViewHistory> history = new ArrayList<>();
        Cursor cursor = null;
        try {
            String arg = lastTimestamp == null ? "" : lastTimestamp;
            cursor = db.rawQuery(String.format(SQL_GET_VIEW_HISTORY, pageLimit), new String[] { arg, arg });
            while (cursor.moveToNext()) {
                ViewHistory item = new ViewHistory();
                int cursorIndex = 0;
                item.setUri(LbryUri.tryParse(cursor.getString(cursorIndex++)));
                item.setClaimId(cursor.getString(cursorIndex++));
                item.setClaimName(cursor.getString(cursorIndex++));
                item.setCost(new BigDecimal(cursor.getDouble(cursorIndex++)));
                item.setCurrency(cursor.getString(cursorIndex++));
                item.setTitle(cursor.getString(cursorIndex++));
                item.setPublisherClaimId(cursor.getString(cursorIndex++));
                item.setPublisherName(cursor.getString(cursorIndex++));
                item.setPublisherTitle(cursor.getString(cursorIndex++));
                item.setThumbnailUrl(cursor.getString(cursorIndex++));
                item.setDevice(cursor.getString(cursorIndex++));
                item.setReleaseTime(cursor.getLong(cursorIndex++));
                try {
                    item.setTimestamp(new SimpleDateFormat(Helper.ISO_DATE_FORMAT_PATTERN).parse(cursor.getString(cursorIndex)));
                } catch (ParseException ex) {
                    // invalid timestamp (which shouldn't happen). Skip this item
                    continue;
                }

                history.add(item);
            }
        } finally {
            Helper.closeCursor(cursor);
        }
        return history;
    }

    public static void createOrUpdateTag(Tag tag, SQLiteDatabase db) {
        db.execSQL(SQL_INSERT_TAG, new Object[] { tag.getLowercaseName(), tag.isFollowed() ? 1 : 0 });
    }
    public static void setAllTagsUnfollowed(SQLiteDatabase db) {
        db.execSQL(SQL_UNFOLLOW_TAGS);
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
