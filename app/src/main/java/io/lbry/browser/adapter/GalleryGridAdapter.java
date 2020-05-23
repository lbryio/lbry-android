package io.lbry.browser.adapter;

import android.content.Context;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import io.lbry.browser.R;
import io.lbry.browser.model.GalleryItem;
import io.lbry.browser.utils.Helper;
import lombok.Setter;

public class GalleryGridAdapter extends RecyclerView.Adapter<GalleryGridAdapter.ViewHolder> {
    private Context context;
    private List<GalleryItem> items;
    @Setter
    private GalleryItemClickListener listener;

    public GalleryGridAdapter(List<GalleryItem> items, Context context) {
        this.items = new ArrayList<>(items);
        this.context = context;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        protected ImageView thumbnailView;
        protected TextView durationView;
        public ViewHolder(View v) {
            super(v);
            thumbnailView = v.findViewById(R.id.gallery_item_thumbnail);
            durationView = v.findViewById(R.id.gallery_item_duration);
        }
    }

    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    public void addItem(GalleryItem item) {
        if (!items.contains(item)) {
            items.add(item);
            notifyDataSetChanged();
        }
    }

    public void addItems(List<GalleryItem> items) {
        for (GalleryItem item : items) {
            if (!this.items.contains(item)) {
                this.items.add(item);
                notifyDataSetChanged();
            }
        }
    }

    public void clearItems() {
        items.clear();
        notifyDataSetChanged();
    }

    @Override
    public GalleryGridAdapter.ViewHolder onCreateViewHolder(ViewGroup root, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.list_item_gallery, root, false);
        return new GalleryGridAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(GalleryGridAdapter.ViewHolder vh, int position) {
        GalleryItem item = items.get(position);
        String thumbnailUrl = item.getThumbnailPath();
        Glide.with(context.getApplicationContext()).load(thumbnailUrl).centerCrop().into(vh.thumbnailView);
        vh.durationView.setVisibility(item.getDuration() > 0 ? View.VISIBLE : View.INVISIBLE);
        vh.durationView.setText(item.getDuration() > 0 ? Helper.formatDuration(Double.valueOf(item.getDuration() / 1000.0).longValue()) : null);

        vh.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.onGalleryItemClicked(item);
                }
            }
        });
    }

    public interface GalleryItemClickListener {
        void onGalleryItemClicked(GalleryItem item);
    }

    public static  class GalleryGridItemDecoration extends RecyclerView.ItemDecoration {

        private int spanCount;
        private int spacing;

        public GalleryGridItemDecoration(int spanCount, int spacing) {
            this.spanCount = spanCount;
            this.spacing = spacing;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column

            outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
            outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
            if (position >= spanCount) {
                outRect.top = spacing; // item top
            }
        }
    }
}
