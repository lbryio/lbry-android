package io.lbry.browser.adapter;

import android.content.Context;
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
import io.lbry.browser.model.EditorsChoiceItem;
import io.lbry.browser.utils.Helper;
import lombok.Setter;

public class EditorsChoiceItemAdapter extends RecyclerView.Adapter<EditorsChoiceItemAdapter.ViewHolder> {
    private static final int VIEW_TYPE_HEADER = 1;
    private static final int VIEW_TYPE_CONTENT = 2;

    private Context context;
    private List<EditorsChoiceItem> items;
    @Setter
    private EditorsChoiceItemListener listener;

    public EditorsChoiceItemAdapter(List<EditorsChoiceItem> items, Context context) {
        this.context = context;
        this.items = new ArrayList<>(items);
    }

    public void addFeaturedItem(EditorsChoiceItem item) {
        items.add(0, item);
        notifyDataSetChanged();
    }

    public void addItems(List<EditorsChoiceItem> items) {
        for (EditorsChoiceItem item : items) {
            if (!this.items.contains(item)) {
                this.items.add(item);
            }
        }
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        protected ImageView thumbnailView;
        protected TextView descriptionView;
        protected TextView headerView;
        protected TextView titleView;
        protected View cardView;

        public ViewHolder(View v) {
            super(v);

            cardView = v.findViewById(R.id.editors_choice_content_card);
            descriptionView = v.findViewById(R.id.editors_choice_content_description);
            titleView = v.findViewById(R.id.editors_choice_content_title);

            thumbnailView = v.findViewById(R.id.editors_choice_content_thumbnail);
            headerView = v.findViewById(R.id.editors_choice_header_title);
        }
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).isHeader() ? VIEW_TYPE_HEADER : VIEW_TYPE_CONTENT;
    }

    @Override
    public EditorsChoiceItemAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.list_item_editors_choice, parent, false);
        return new EditorsChoiceItemAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(EditorsChoiceItemAdapter.ViewHolder vh, int position) {
        int type = getItemViewType(position);
        EditorsChoiceItem item = items.get(position);

        vh.headerView.setVisibility(type == VIEW_TYPE_HEADER ? View.VISIBLE : View.GONE);
        vh.cardView.setVisibility(type == VIEW_TYPE_CONTENT ? View.VISIBLE : View.GONE);

        vh.headerView.setText(item.getTitle());
        vh.titleView.setText(item.getTitle());
        vh.descriptionView.setText(item.getDescription());
        if (!Helper.isNullOrEmpty(item.getThumbnailUrl())) {
            Glide.with(context.getApplicationContext()).
                    asBitmap().
                    load(item.getThumbnailUrl()).
                    centerCrop().
                    placeholder(R.drawable.bg_thumbnail_placeholder).
                    into(vh.thumbnailView);
        }

        vh.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.onEditorsChoiceItemClicked(item);
                }
            }
        });
    }

    public interface EditorsChoiceItemListener {
        void onEditorsChoiceItemClicked(EditorsChoiceItem item);
    }
}
