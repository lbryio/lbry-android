package io.lbry.browser.reactmodules;

import android.content.Context;
import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

public class GalleryModule extends ReactContextBaseJavaModule {
    private Context context;

    public GalleryModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.context = reactContext;
    }

    @Override
    public String getName() {
        return "Gallery";
    }

    @ReactMethod
    public void getVideos(Promise promise) {
        WritableArray items = Arguments.createArray();
        List<GalleryItem> videos = loadVideos();
        for (int i = 0; i < videos.size(); i++) {
            items.pushMap(videos.get(i).toMap());
        }

        promise.resolve(items);
    }

    @ReactMethod
    public void getThumbnailPath(Promise promise) {
        if (context != null) {
            File cacheDir = context.getExternalCacheDir();
            String thumbnailPath = String.format("%s/thumbnails", cacheDir.getAbsolutePath());
            promise.resolve(thumbnailPath);
            return;
        }

        promise.resolve(null);
    }

    private List<GalleryItem> loadVideos() {
        String[] projection = {
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DATA,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.Video.Media.DURATION
        };

        List<String> ids = new ArrayList<String>();
        List<GalleryItem> items = new ArrayList<GalleryItem>();
        Cursor cursor = context.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection, null, null,
                                                           String.format("%s DESC", MediaStore.MediaColumns.DATE_MODIFIED));
        while (cursor.moveToNext()) {
            int idColumn = cursor.getColumnIndex(MediaStore.MediaColumns._ID);
            int nameColumn = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME);
            int typeColumn = cursor.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE);
            int pathColumn = cursor.getColumnIndex(MediaStore.MediaColumns.DATA);
            int durationColumn = cursor.getColumnIndex(MediaStore.Video.Media.DURATION);

            String id = cursor.getString(idColumn);
            GalleryItem item = new GalleryItem();
            item.setId(id);
            item.setName(cursor.getString(nameColumn));
            item.setType(cursor.getString(typeColumn));
            item.setFilePath(cursor.getString(pathColumn));
            items.add(item);
            ids.add(id);
        }

        checkThumbnails(ids);

        return items;
    }

    private void checkThumbnails(final List<String> ids) {
        (new AsyncTask<Void, Void, Void>() {
            protected Void doInBackground(Void... param) {
                if (context != null) {
                    ContentResolver resolver = context.getContentResolver();
                    for (int i = 0; i < ids.size(); i++) {
                        String id = ids.get(i);
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
                    }
                }

                return null;
            }
        }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private static class GalleryItem {
        private String id;

        private int duration;

        private String filePath;

        private String name;

        private String type;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public int getDuration() {
            return duration;
        }

        public void setDuration(int duration) {
            this.duration = duration;
        }

        public String getFilePath() {
            return filePath;
        }

        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public WritableMap toMap() {
            WritableMap map = Arguments.createMap();
            map.putString("id", id);
            map.putString("name", name);
            map.putString("filePath", filePath);
            map.putString("type", type);
            map.putInt("duration", duration);

            return map;
        }
    }
}
