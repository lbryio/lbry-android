package io.lbry.browser.utils;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.method.LinkMovementMethod;
import android.view.ContextMenu;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import io.lbry.browser.MainActivity;
import io.lbry.browser.data.DatabaseHelper;
import io.lbry.browser.dialog.ContentFromDialogFragment;
import io.lbry.browser.dialog.ContentSortDialogFragment;
import io.lbry.browser.model.Claim;
import io.lbry.browser.model.LbryFile;
import io.lbry.browser.model.Tag;
import io.lbry.browser.model.UrlSuggestion;
import io.lbry.browser.model.ViewHistory;
import io.lbry.browser.tasks.localdata.SaveUrlHistoryTask;
import io.lbry.browser.tasks.localdata.SaveViewHistoryTask;
import okhttp3.MediaType;

public final class Helper {
    public static final String UNKNOWN = "Unknown";
    public static final String METHOD_GET = "GET";
    public static final String METHOD_POST = "POST";
    public static final String ISO_DATE_FORMAT_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS";
    public static final String SDK_AMOUNT_FORMAT = "0.0#######";
    public static final MediaType FORM_MEDIA_TYPE = MediaType.parse("application/x-www-form-urlencoded");
    public static final MediaType JSON_MEDIA_TYPE = MediaType.get("application/json; charset=utf-8");
    public static final int CONTENT_PAGE_SIZE = 25;
    public static final double MIN_DEPOSIT = 0.001;
    public static final String LBC_CURRENCY_FORMAT_PATTERN = "#,###.##";
    public static final String FILE_SIZE_FORMAT_PATTERN = "#,###.#";
    public static final DecimalFormat LBC_CURRENCY_FORMAT = new DecimalFormat(LBC_CURRENCY_FORMAT_PATTERN);
    public static final DecimalFormat FULL_LBC_CURRENCY_FORMAT = new DecimalFormat("#,###.########");
    public static final DecimalFormat SIMPLE_CURRENCY_FORMAT = new DecimalFormat("#,##0.00");
    public static final SimpleDateFormat FILESTAMP_FORMAT =  new SimpleDateFormat("yyyyMMdd_HHmmss");
    public static final String EXPLORER_TX_PREFIX = "https://explorer.lbry.com/tx";

    public static final List<Double> PLAYBACK_SPEEDS = Arrays.asList(0.25, 0.5, 0.75, 1.0, 1.25, 1.5, 1.75, 2.0);

    public static boolean isNull(String value) {
        return value == null;
    }
    public static boolean isNullOrEmpty(String value) {
        return value == null || value.trim().length() == 0;
    }

    public static void buildPlaybackSpeedMenu(ContextMenu menu) {
        int order = 0;
        DecimalFormat formatter = new DecimalFormat("0.##");
        for (Double speed : PLAYBACK_SPEEDS) {
            menu.add(0, Double.valueOf(speed * 100).intValue(), ++order, String.format("%sx", formatter.format(speed)));
        }
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

    public static void setViewProgress(ProgressBar progressBar, int progress) {
        if (progressBar != null) {
            progressBar.setProgress(progress);
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

    public static Double parseDouble(Object value, double defaultValue) {
        try {
            return Double.parseDouble(String.valueOf(value));
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
    public static String[] formatBytesParts(long bytes, boolean showTB) {
        DecimalFormat formatter = new DecimalFormat(FILE_SIZE_FORMAT_PATTERN);
        if (bytes < 1048576) {
            // less than 1MB
            return new String[] { formatter.format(bytes / 1024.0), "KB" };
        }
        if (bytes < 1073741824) {
            // less than 1GB
            return new String[] { formatter.format(bytes / (1024.0 * 1024.0)), "MB" };
        }
        if (showTB) {
            if (bytes < (1073741824L * 1024L))  {
                return new String[] { formatter.format(bytes / (1024.0 * 1024.0 * 1024.0)), "GB" };
            }
            return new String[] { formatter.format(bytes / (1024.0 * 1024.0 * 1024.0 * 1024.0)), "TB" };
        }
        return new String[] { formatter.format(bytes / (1024.0 * 1024.0 * 1024.0)), "GB" };
    }

    public static String formatBytes(long bytes, boolean showTB) {
        DecimalFormat formatter = new DecimalFormat(FILE_SIZE_FORMAT_PATTERN);
        if (bytes < 1048576) {
            // less than 1MB
            return String.format("%sKB", formatter.format(bytes / 1024.0));
        }
        if (bytes < 1073741824) {
            // less than 1GB
            return String.format("%sMB", formatter.format(bytes / (1024.0 * 1024.0)));
        }
        if (showTB) {
            if (bytes < (1073741824L * 1024L))  {
                return String.format("%sGB", formatter.format(bytes / (1024.0 * 1024.0 * 1024.0)));
            }
            return String.format("%sTB", formatter.format(bytes / (1024.0 * 1024.0 * 1024.0 * 1024.0)));
        }
        return String.format("%sGB", formatter.format(bytes / (1024.0 * 1024.0 * 1024.0)));
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
        DecimalFormat format = new DecimalFormat("#,###.#");
        if (value > 1000000000.00) {
            return String.format("%sB", format.format(value / 1000000000.0));
        }
        if (value > 1000000.0) {
            return String.format("%sM",format.format( value / 1000000.0));
        }
        if (value > 1000.0) {
            return String.format("%sK", format.format(value / 1000.0));
        }

        if (value == 0) {
            return "0";
        }

        return format.format(value).equals("0") ? FULL_LBC_CURRENCY_FORMAT.format(value) : format.format(value);
    }

    public static String getValue(CharSequence cs) {
        return cs != null ? cs.toString().trim() : "";
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
    public static List<Claim> filterDeletedClaims(List<Claim> claims) {
        List<Claim> filtered = new ArrayList<>();
        for (Claim claim : claims) {
            if (!Lbry.abandonedClaimIds.contains(claim.getClaimId())) {
                filtered.add(claim);
            }
        }
        return filtered;
    }

    public static void setWunderbarValue(String value, Context context) {
        if (context instanceof MainActivity) {
            ((MainActivity) context).setWunderbarValue(value);
        }
    }

    public static String getDeviceName() {
        if (Helper.isNullOrEmpty(Build.MANUFACTURER) || UNKNOWN.equalsIgnoreCase(Build.MANUFACTURER)) {
            return Build.MODEL;
        }
        return String.format("%s %s", Build.MANUFACTURER, Build.MODEL);
    }

    public static boolean channelExists(String channelName) {
        for (Claim claim : Lbry.ownChannels) {
            if (channelName.equalsIgnoreCase(claim.getName())) {
                return true;
            }
        }
        return false;
    }
    public static boolean claimNameExists(String claimName) {
        for (Claim claim : Lbry.ownClaims) {
            if (claimName.equalsIgnoreCase(claim.getName())) {
                return true;
            }
        }
        return false;
    }

    public static String getRealPathFromURI_API19(final Context context, final Uri uri) {
        return getRealPathFromURI_API19(context, uri, false);
    }
    /**
     * https://gist.github.com/HBiSoft/15899990b8cd0723c3a894c1636550a8
     */
    @SuppressLint("NewApi")
    public static String getRealPathFromURI_API19(final Context context, final Uri uri, boolean folderPath) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                // This is for checking Main Memory
                if ("primary".equalsIgnoreCase(type)) {
                    if (split.length > 1) {
                        return Environment.getExternalStorageDirectory() + "/" + split[1];
                    } else {
                        return Environment.getExternalStorageDirectory() + "/";
                    }
                    // This is for checking SD Card
                } else {
                    return "storage" + "/" + docId.replace(":", "/");
                }

            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {
                String fileName = getFilePath(context, uri);
                if (fileName != null) {
                    String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
                    return folderPath ?
                            String.format("%s/Download", extStorageDirectory) :
                            String.format("%s/Download/%s", extStorageDirectory, fileName);
                }

                String id = DocumentsContract.getDocumentId(uri);
                if (id.startsWith("raw:")) {
                    id = id.replaceFirst("raw:", "");
                    File file = new File(id);
                    if (file.exists())
                        return id;
                }

                String[] contentUriPrefixesToTry = new String[]{
                        "content://downloads/public_downloads",
                        "content://downloads/my_downloads",
                        "content://downloads/all_downloads"
                };

                for (String contentUriPrefix : contentUriPrefixesToTry) {
                    Uri contentUri = Helper.parseInt(id, -1) > 0 ?
                            ContentUris.withAppendedId(Uri.parse(contentUriPrefix), Long.valueOf(id)) :
                            Uri.parse(contentUriPrefix);
                    try {
                        String path = getDataColumn(context, contentUri, null, null);
                        if (path != null) {
                            return path;
                        }
                    } catch (Exception ex) {
                        // pass
                    }
                }
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    public static String getFilePath(Context context, Uri uri) {
        Cursor cursor = null;
        final String[] projection = { MediaStore.MediaColumns.DISPLAY_NAME };

        try {
            cursor = context.getContentResolver().query(uri, projection, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    public static void applyHtmlForTextView(TextView textView) {
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setText(HtmlCompat.fromHtml(textView.getText().toString(), HtmlCompat.FROM_HTML_MODE_LEGACY));
    }

    public static List<Claim> filterInvalidReposts(List<Claim> claims) {
        List<Claim> filtered = new ArrayList<>();
        for (Claim claim : claims) {
            if (Claim.TYPE_REPOST.equalsIgnoreCase(claim.getValueType()) && claim.getRepostedClaim() == null) {
                continue;
            }
            filtered.add(claim);
        }
        return filtered;
    }

    public static List<LbryFile> filterDownloads(List<LbryFile> files) {
        List<LbryFile> filtered = new ArrayList<>();
        for (int i = 0; i < files.size(); i++) {
            LbryFile file = files.get(i);
            // remove own claims as well
            if (Lbry.ownClaims != null && Lbry.ownClaims.size() > 0) {
                for (Claim own : Lbry.ownClaims) {
                    if (own.getClaimId().equalsIgnoreCase(file.getClaimId())) {
                        continue;
                    }
                }
            }
            if (!Helper.isNullOrEmpty(file.getDownloadPath())) {
                filtered.add(file);
            }
        }
        return filtered;
    }

    public static List<Claim> claimsFromFiles(List<LbryFile> files) {
        List<Claim> claims = new ArrayList<>();
        for (LbryFile file : files) {
            claims.add(file.getClaim());
        }
        return claims;
    }

    public static List<Claim> claimsFromViewHistory(List<ViewHistory> history) {
        List<Claim> claims = new ArrayList<>();
        for (ViewHistory item : history) {
            claims.add(Claim.fromViewHistory(item));
        }
        return claims;
    }

    public static void saveUrlHistory(String url, String title, int type) {
        DatabaseHelper dbHelper = DatabaseHelper.getInstance();
        if (dbHelper != null) {
            UrlSuggestion suggestion = new UrlSuggestion();
            suggestion.setUri(LbryUri.tryParse(url));
            suggestion.setType(type);
            suggestion.setText(Helper.isNull(title) ? "" : title);
            new SaveUrlHistoryTask(suggestion, dbHelper, null).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    public static void saveViewHistory(String url, Claim claim) {
        DatabaseHelper dbHelper = DatabaseHelper.getInstance();
        if (dbHelper != null) {
            ViewHistory viewHistory = ViewHistory.fromClaimWithUrlAndDeviceName(claim, url, getDeviceName());
            new SaveViewHistoryTask(viewHistory, dbHelper, null).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }
    public static String normalizeChannelName(String channelName) {
        if (Helper.isNullOrEmpty(channelName)) {
            return "";
        }
        if (!channelName.startsWith("@")) {
            return String.format("@%s", channelName);
        }
        return channelName;
    }

    public static int getScaledValue(int value, float scale) {
        return (int) (value * scale + 0.5f);
    }

    public static String generateUrl() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        sb.append(Predefined.ADJECTIVES.get(random.nextInt(Predefined.ADJECTIVES.size()))).append("-").
            append(Predefined.ADJECTIVES.get(random.nextInt(Predefined.ADJECTIVES.size()))).append("-").
                append(Predefined.ANIMALS.get(random.nextInt(Predefined.ANIMALS.size())));
        return sb.toString().toLowerCase();
    }

    public static void refreshRecyclerView(RecyclerView rv) {
        if (rv == null) {
            return;
        }

        RecyclerView.Adapter adapter = rv.getAdapter();
        int prevScrollPosition = 0;

        RecyclerView.LayoutManager lm = rv.getLayoutManager();
        if (lm instanceof LinearLayoutManager) {
            prevScrollPosition = ((LinearLayoutManager) lm).findLastCompletelyVisibleItemPosition();
        } else if (lm instanceof GridLayoutManager) {
            prevScrollPosition = ((GridLayoutManager) lm).findLastCompletelyVisibleItemPosition();
        }

        rv.setAdapter(null);
        rv.setAdapter(adapter);
        rv.scrollToPosition(prevScrollPosition > 0 ? prevScrollPosition : 0);
    }

    public static String makeid(int length) {
        Random random = new Random();
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder id = new StringBuilder();
        for (int i = 0; i < length; i++) {
            id.append(chars.charAt(random.nextInt(chars.length())));
        }
        return id.toString();
    }
}
