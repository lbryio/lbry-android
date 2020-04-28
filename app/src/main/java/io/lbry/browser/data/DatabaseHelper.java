package io.lbry.browser.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import io.lbry.browser.model.lbryinc.Subscription;
import io.lbry.browser.utils.Helper;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "LbryApp.db";

    private static final String[] SQL_CREATE_TABLES = {
            // local subscription store
            "CREATE TABLE subscriptions (url TEXT PRIMARY KEY NOT NULL, channel_name TEXT NOT NULL)",

            // local claim cache store for quick load / refresh

    };
    private static final String[] SQL_CREATE_INDEXES = {
            "CREATE UNIQUE INDEX idx_subscription_url ON subscriptions (url)"
    };

    private static final String SQL_INSERT_SUBSCRIPTION = "REPLACE INTO subscriptions (channel_name, url) VALUES (?, ?)";
    private static final String SQL_DELETE_SUBSCRIPTION = "DELETE FROM subscriptions WHERE url = ?";
    private static final String SQL_GET_SUBSCRIPTIONS = "SELECT channel_name, url FROM subscriptions";

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
