package io.lbry.browser.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.view.View;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Closeable;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import io.lbry.browser.MainActivity;
import io.lbry.browser.dialog.ContentFromDialogFragment;
import io.lbry.browser.dialog.ContentSortDialogFragment;
import io.lbry.browser.model.Claim;
import io.lbry.browser.model.Tag;
import okhttp3.MediaType;

public final class Helper {
    public static final String METHOD_GET = "GET";
    public static final String METHOD_POST = "POST";
    public static final String ISO_DATE_FORMAT_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS";
    public static final String SDK_AMOUNT_FORMAT = "0.0#######";
    public static final MediaType FORM_MEDIA_TYPE = MediaType.parse("application/x-www-form-urlencoded");
    public static final MediaType JSON_MEDIA_TYPE = MediaType.get("application/json; charset=utf-8");
    public static final int CONTENT_PAGE_SIZE = 25;

    public static boolean isNull(String value) {
        return value == null;
    }
    public static boolean isNullOrEmpty(String value) {
        return value == null || value.trim().length() == 0;
    }

    public static String capitalize(String value) {
        StringBuilder sb = new StringBuilder();
        boolean capitalizeNext = true;
        for (char c : value.toCharArray()) {
            if (c == ' ') {
                capitalizeNext = true;
                sb.append(c);
            } else {
                if (capitalizeNext) {
                    sb.append(String.valueOf(c).toUpperCase());
                } else {
                    sb.append(c);
                }
                capitalizeNext = false;
            }
        }
        return sb.toString();
    }

    public static String join(List<String> list, String delimiter) {
        StringBuilder sb = new StringBuilder();
        String delim = "";
        for (String s : list) {
            sb.append(delim).append(s);
            delim = delimiter;
        }
        return sb.toString();
    }

    public static <T> JSONArray jsonArrayFromList(List<T> values) {
        JSONArray array = new JSONArray();
        for (T value : values) {
            array.put(value);
        }
        return array;
    }

    public static void unregisterReceiver(BroadcastReceiver receiver, Context context) {
        if (receiver != null && context != null) {
            context.unregisterReceiver(receiver);
        }
    }

    public static void setViewVisibility(View view, int visibility) {
        if (view != null) {
            view.setVisibility(visibility);
        }
    }

    public static void setViewText(TextView view, int stringResourceId) {
        if (view != null) {
            view.setText(stringResourceId);
        }
    }

    public static void setViewText(TextView view, String text) {
        if (view != null) {
            view.setText(text);
        }
    }

    public static void closeCursor(Cursor cursor) {
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
    }

    public static void closeDatabase(SQLiteDatabase db) {
        if (db != null) {
            db.close();
        }
    }

    public static void closeCloseable(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException ex) {
            // pass
        }
    }

    public static int parseInt(Object value, int defaultValue) {
        try {
            return Integer.parseInt(String.valueOf(value), 10);
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    public static String formatDuration(long duration) {
        long seconds = duration;
        long hours = Double.valueOf(Math.floor(seconds / 3600.0)).longValue();
        seconds = duration - hours * 3600;
        long minutes = Double.valueOf(Math.floor(seconds / 60.0)).longValue();
        seconds = seconds % 60;

        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        }
        return String.format("%d:%02d", minutes, seconds);
    }

    public static JSONObject getJSONObject(String name, JSONObject object) {
       try {
           return object.has(name) && !object.isNull(name) ? object.getJSONObject(name) : null;
       } catch (JSONException ex) {
           return null;
        }
    }
    public static boolean getJSONBoolean(String name, boolean defaultValue, JSONObject object) {
        try {
            return object.has(name) && !object.isNull(name) ? object.getBoolean(name) : defaultValue;
        } catch (JSONException ex) {
            return defaultValue;
        }
    }

    public static String getJSONString(String name, String defaultValue, JSONObject object) {
       try {
           return object.has(name) && !object.isNull(name) ? object.getString(name) : defaultValue;
       } catch (JSONException ex) {
           return defaultValue;
       }
    }

    public static double getJSONDouble(String name, double defaultValue, JSONObject object) {
        try {
            return object.has(name) && !object.isNull(name) ? object.getDouble(name) : defaultValue;
        } catch (JSONException ex) {
            return defaultValue;
        }
    }


    public static long getJSONLong(String name, long defaultValue, JSONObject object) {
        try {
            return object.has(name) && !object.isNull(name) ? object.getLong(name) : defaultValue;
        } catch (JSONException ex) {
            return defaultValue;
        }
    }
    public static int getJSONInt(String name, int defaultValue, JSONObject object) {
        try {
            return object.has(name) && !object.isNull(name) ? object.getInt(name) : defaultValue;
        } catch (JSONException ex) {
            return defaultValue;
        }
    }
    public static void setViewEnabled(View view, boolean enabled) {
        if (view != null) {
            view.setEnabled(enabled);
        }
    }

    public static String shortCurrencyFormat(double value) {
        if (value > 1000000000.00) {
            return String.format("%.1fB", value / 1000000000.0);
        }
        if (value > 1000000.0) {
            return String.format("%.1fM", value / 1000000.0);
        }
        if (value > 1000.0) {
            return String.format("%.1fK", value / 1000.0);
        }

        if (value == 0) {
            return "0";
        }

        return new DecimalFormat("###.##").format(value);
    }

    public static String getValue(CharSequence cs) {
        return cs != null ? cs.toString() : "";
    }

    public static List<String> buildContentSortOrder(int sortBy) {
        List<String> sortOrder = new ArrayList<>();
        switch (sortBy) {
            case ContentSortDialogFragment.ITEM_SORT_BY_NEW:
                sortOrder = Arrays.asList(Claim.ORDER_BY_RELEASE_TIME); break;
            case ContentSortDialogFragment.ITEM_SORT_BY_TOP:
                sortOrder = Arrays.asList(Claim.ORDER_BY_EFFECTIVE_AMOUNT); break;
            case ContentSortDialogFragment.ITEM_SORT_BY_TRENDING:
                sortOrder = Arrays.asList(Claim.ORDER_BY_TRENDING_GROUP, Claim.ORDER_BY_TRENDING_MIXED); break;
        }

        return sortOrder;
    }

    public static String buildReleaseTime(int contentFrom) {
        if (contentFrom == 0 || contentFrom == ContentFromDialogFragment.ITEM_FROM_ALL_TIME) {
            return null;
        }

        Calendar cal = Calendar.getInstance();
        switch (contentFrom) {
            case ContentFromDialogFragment.ITEM_FROM_PAST_24_HOURS: cal.add(Calendar.HOUR_OF_DAY, -24) ; break;
            case ContentFromDialogFragment.ITEM_FROM_PAST_WEEK: default: cal.add(Calendar.DAY_OF_YEAR, -7); break;
            case ContentFromDialogFragment.ITEM_FROM_PAST_MONTH: cal.add(Calendar.MONTH, -1); break;
            case ContentFromDialogFragment.ITEM_FROM_PAST_YEAR: cal.add(Calendar.YEAR, -1); break;
        }

        return String.format(">%d", Double.valueOf(cal.getTimeInMillis() / 1000.0).longValue());
    }

    public static final Map<String, Integer> randomColorMap = new HashMap<>();
    public static int generateRandomColorForValue(String value) {
        if (Helper.isNullOrEmpty(value)) {
            return 0;
        }

        if (randomColorMap.containsKey(value)) {
            return randomColorMap.get(value);
        }

        Random random = new Random(value.hashCode());
        int color = Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256));
        randomColorMap.put(value, color);
        return color;
    }

    public static void setIconViewBackgroundColor(View view, int color, boolean isPlaceholder, Context context) {
        Drawable bg = view.getBackground();
        if (bg instanceof ShapeDrawable) {
            ((ShapeDrawable) bg).getPaint().setColor(isPlaceholder ? ContextCompat.getColor(context, android.R.color.transparent) : color);
        } else if (bg instanceof GradientDrawable) {
            ((GradientDrawable) bg).setColor(isPlaceholder ? ContextCompat.getColor(context, android.R.color.transparent) : color);
        } else if (bg instanceof ColorDrawable) {
            ((ColorDrawable) bg).setColor(isPlaceholder ? ContextCompat.getColor(context, android.R.color.transparent) : color);
        }
    }

    public static List<Tag> getTagObjectsForTags(List<String> tags) {
        List<Tag> tagObjects = new ArrayList<>(tags.size());
        for (String tag : tags) {
            tagObjects.add(new Tag(tag));
        }
        return tagObjects;
    }
    public static List<String> getTagsForTagObjects(List<Tag> tagObjects) {
        List<String> tags = new ArrayList<>(tagObjects.size());
        for (Tag tagObject : tagObjects) {
            tags.add(tagObject.getLowercaseName());
        }
        return tags;
    }
    public static List<Tag> mergeKnownTags(List<Tag> fetchedTags) {
        List<Tag> allKnownTags = getTagObjectsForTags(Predefined.DEFAULT_KNOWN_TAGS);
        List<Integer> followIndexes = new ArrayList<>();
        for (Tag tag : fetchedTags) {
            if (!allKnownTags.contains(tag)) {
                allKnownTags.add(tag);
            } else if (tag.isFollowed()) {
                followIndexes.add(allKnownTags.indexOf(tag));
            }
        }
        for (int index : followIndexes) {
            allKnownTags.get(index).setFollowed(true);
        }
        return allKnownTags;
    }
    public static List<Tag> filterFollowedTags(List<Tag> tags) {
        List<Tag> followedTags = new ArrayList<>();
        for (Tag tag : tags) {
            if (tag.isFollowed()) {
                followedTags.add(tag);
            }
        }
        return followedTags;
    }

    public static void setWunderbarValue(String value, Context context) {
        if (context instanceof MainActivity) {
            ((MainActivity) context).setWunderbarValue(value);
        }
    }
}
