package io.lbry.browser.tasks.localdata;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.lbry.browser.model.GalleryItem;
import io.lbry.browser.utils.Helper;

public class LoadGalleryItemsTask extends AsyncTask<Void, GalleryItem, List<GalleryItem>> {
    private static final String TAG = "LoadGalleryItemsTask";
    private LoadGalleryHandler handler;
    private View progressView;
    private Context context;

    public LoadGalleryItemsTask(View progressView, Context context, LoadGalleryHandler handler) {
        this.progressView = progressView;
        this.context = context;
        this.handler = handler;
    }

    protected void onPreExecute(Void... params) {
        Helper.setViewVisibility(progressView, View.VISIBLE);
    }

    protected List<GalleryItem> doInBackground(Void... params) {
        List<GalleryItem> items = new ArrayList<>();
        List<GalleryItem> itemsWithThumbnails = new ArrayList<>();
        Cursor cursor = null;
        if (context != null) {
            ContentResolver resolver = context.getContentResolver();
            try {
                String[] projection = {
                        MediaStore.MediaColumns._ID,
                        MediaStore.MediaColumns.DATA,
                        MediaStore.MediaColumns.DISPLAY_NAME,
                        MediaStore.MediaColumns.MIME_TYPE,
                        MediaStore.Video.Media.DURATION
                };
                cursor = resolver.query(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        projection, null, null,
                        String.format("%s DESC LIMIT 150", MediaStore.MediaColumns.DATE_MODIFIED));
                while (cursor.moveToNext()) {
                    int idColumn = cursor.getColumnIndex(MediaStore.MediaColumns._ID);
                    int nameColumn = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME);
                    int typeColumn = cursor.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE);
                    int pathColumn = cursor.getColumnIndex(MediaStore.MediaColumns.DATA);
                    int durationColumn = cursor.getColumnIndex(MediaStore.Video.Media.DURATION);

                    GalleryItem item = new GalleryItem();
                    item.setId(cursor.getString(idColumn));
                    item.setName(cursor.getString(nameColumn));
                    item.setType(cursor.getString(typeColumn));
                    item.setFilePath(cursor.getString(pathColumn));
                    item.setDuration(cursor.getLong(durationColumn));
                    items.add(item);
                }
            } catch (SQLiteException ex) {
                // failed to load videos. log and pass
                Log.e(TAG, ex.getMessage(), ex);
            } finally {
                Helper.closeCursor(cursor);
            }

            // load (or generate) thumbnail for each item
            for (GalleryItem item : items) {
                String id = item.getId();
                File cacheDir = context.getExternalCacheDir();
                File thumbnailsDir = new File(String.format("%s/thumbnails", cacheDir.getAbsolutePath()));
                if (!thumbnailsDir.isDirectory()) {
                    thumbnailsDir.mkdirs();
                }

                String thumbnailPath = String.format("%s/%s.png", thumbnailsDir.getAbsolutePath(), id);
                File file = new File(thumbnailPath);
                if (!file.exists()) {
                    // save the thumbnail to the path
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 1;
                    Bitmap thumbnail = MediaStore.Video.Thumbnails.getThumbnail(
                            resolver, Long.parseLong(id), MediaStore.Video.Thumbnails.MINI_KIND, options);
                    if (thumbnail != null) {
                        try (FileOutputStream os = new FileOutputStream(thumbnailPath)) {
                            thumbnail.compress(Bitmap.CompressFormat.PNG, 80, os);
                        } catch (IOException ex) {
                            // skip
                        }
                    }
                }

                if (file.exists() && file.length() > 0) {
                    item.setThumbnailPath(file.getAbsolutePath());
                    itemsWithThumbnails.add(item);
                    publishProgress(item);
                }
            }
        }

        return itemsWithThumbnails;
    }

    protected void onProgressUpdate(GalleryItem... items) {
        if (handler != null) {
            for (GalleryItem item : items) {
                handler.onItemLoaded(item);
            }
        }
    }

    protected void onPostExecute(List<GalleryItem> items) {
        if (handler != null) {
            handler.onAllItemsLoaded(items);
        }
    }

    public interface LoadGalleryHandler {
        void onItemLoaded(GalleryItem item);
        void onAllItemsLoaded(List<GalleryItem> items);
    }
}
