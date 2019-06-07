package io.lbry.browser.reactmodules;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.os.Bundle;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;

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

    private List<GalleryItem> loadVideos() {
        String[] projection = {
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DATA,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.Video.Media.DURATION
        };

        List<GalleryItem> items = new ArrayList<GalleryItem>();
        Cursor cursor = context.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection, null, null, null);
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
            items.add(item);
        }

        return items;
    }


    private static class GalleryItem {
        private String id;

        private int duration;

        private String filePath;

        private String name;

        private String thumbnailUri;

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

        public String getThumbnailUri() {
            return thumbnailUri;
        }

        public void setThumnbailUri(String thumbnailUri) {
            this.thumbnailUri = thumbnailUri;
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
            map.putString("thumbnailUri", thumbnailUri);
            map.putInt("duration", duration);

            return map;
        }
    }
}
