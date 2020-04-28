package io.lbry.browser.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import io.lbry.browser.R;
import io.lbry.browser.model.Tag;
import lombok.Setter;

public class TagListAdapter extends RecyclerView.Adapter<TagListAdapter.ViewHolder> {
    private Context context;
    private List<Tag> items;
    @Setter
    private TagClickListener clickListener;

    public TagListAdapter(List<Tag> tags, Context context) {
        this.context = context;
        this.items = new ArrayList<>(tags);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        protected TextView nameView;
        public ViewHolder(View v) {
            super(v);
            nameView = v.findViewById(R.id.tag_name);
        }
    }

    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    public void addTags(List<Tag> tags) {
        for (Tag tag : tags) {
            if (!items.contains(tag)) {
                items.add(tag);
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public TagListAdapter.ViewHolder onCreateViewHolder(ViewGroup root, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.list_item_tag, root, false);
        return new TagListAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(TagListAdapter.ViewHolder vh, int position) {
        Tag tag = items.get(position);
        vh.nameView.setText(tag.getName().toLowerCase());
        vh.itemView.setBackgroundResource(tag.isMature() ? R.drawable.bg_tag_mature : R.drawable.bg_tag);
        vh.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (clickListener != null) {
                    clickListener.onTagClicked(tag);
                }
            }
        });
    }

    public interface TagClickListener {
        void onTagClicked(Tag tag);
    }
}
