package io.lbry.browser.utils;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;

public class LbryAnalytics {

    public static final String EVENT_APP_EXCEPTION = "app_exception";
    public static final String EVENT_APP_LAUNCH = "app_launch";
    public static final String EVENT_EMAIL_ADDED = "email_added";
    public static final String EVENT_EMAIL_VERIFIED = "email_verified";
    public static final String EVENT_FIRST_RUN_COMPLETED = "first_run_completed";
    public static final String EVENT_FIRST_USER_AUTH = "first_user_auth";
    public static final String EVENT_LBRY_NOTIFICATION_OPEN = "lbry_notification_open";
    public static final String EVENT_LBRY_NOTIFICATION_RECEIVE = "lbry_notification_receive";
    public static final String EVENT_OPEN_FILE_PAGE = "open_file_page";
    public static final String EVENT_PLAY = "play";
    public static final String EVENT_PURCHASE_URI = "purchase_uri";
    public static final String EVENT_REWARD_ELIGIBILITY_COMPLETED = "reward_eligibility_completed";
    public static final String EVENT_TAG_FOLLOW = "tag_follow";
    public static final String EVENT_TAG_UNFOLLOW = "tag_unfollow";
    public static final String EVENT_PUBLISH = "publish";
    public static final String EVENT_CHANNEL_CREATE = "channel_create";
    public static final String EVENT_SEARCH = "search";

    private static FirebaseAnalytics analytics;

    public static void init(Context context) {
        analytics = FirebaseAnalytics.getInstance(context);
    }

    public static void setCurrentScreen(Activity activity, String name, String className) {
        analytics.setCurrentScreen(activity, name, className);
    }

    public static void logEvent(String name) {
        logEvent(name, null);
    }

    public static void logEvent(String name, Bundle bundle) {
        analytics.logEvent(name, bundle);
    }

    public static void logException(String message, String exceptionName) {
        Bundle bundle = new Bundle();
        bundle.putString("message", message);
        bundle.putString("name", exceptionName);
        logEvent("app_exception", bundle);
    }
}
