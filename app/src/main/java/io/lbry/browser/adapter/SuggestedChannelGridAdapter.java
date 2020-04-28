package io.lbry.browser.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

import io.lbry.browser.R;
import io.lbry.browser.model.Claim;
import io.lbry.browser.listener.ChannelItemSelectionListener;
import io.lbry.browser.utils.Helper;
import lombok.Setter;

public class SuggestedChannelGridAdapter extends RecyclerView.Adapter<SuggestedChannelGridAdapter.ViewHolder> {
    private Context context;
    private List<Claim> items;
    private List<Claim> selectedItems;
    @Setter
    private ChannelItemSelectionListener listener;

    public SuggestedChannelGridAdapter(List<Claim> items, Context context) {
        this.items = new ArrayList<>(items);
        this.selectedItems = new ArrayList<>();
        this.context = context;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        protected ImageView thumbnailView;
        protected TextView alphaView;
        protected TextView titleView;
        protected TextView tagView;
        public ViewHolder(View v) {
            super(v);
            alphaView = v.findViewById(R.id.suggested_channel_alpha_view);
            thumbnailView = v.findViewById(R.id.suggested_channel_thumbnail);
            titleView = v.findViewById(R.id.suggested_channel_title);
            tagView = v.findViewById(R.id.suggested_channel_tag);
        }
    }

    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    public int getSelectedCount() { return selectedItems.size(); }

    public List<Claim> getSelectedItems() {
        return this.selectedItems;
    }
    public void clearSelectedItems() {
        this.selectedItems.clear();
    }
    public boolean isClaimSelected(Claim claim) {
        return selectedItems.contains(claim);
    }

    public void addClaims(List<Claim> claims) {
        for (Claim claim : claims) {
            if (!items.contains(claim)) {
                items.add(claim);
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public SuggestedChannelGridAdapter.ViewHolder onCreateViewHolder(ViewGroup root, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.list_item_suggested_channel, root, false);
        return new SuggestedChannelGridAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(SuggestedChannelGridAdapter.ViewHolder vh, int position) {
        Claim claim = items.get(position);

        String thumbnailUrl = claim.getThumbnailUrl();
        if (!Helper.isNullOrEmpty(thumbnailUrl)) {
            vh.alphaView.setVisibility(View.GONE);
            vh.thumbnailView.setVisibility(View.VISIBLE);
            Glide.with(context.getApplicationContext()).load(thumbnailUrl).apply(RequestOptions.circleCropTransform()).into(vh.thumbnailView);
        } else {
            vh.alphaView.setVisibility(View.VISIBLE);
            vh.thumbnailView.setVisibility(View.GONE);
        }

        vh.alphaView.setText(claim.getFirstCharacter());
        vh.titleView.setText(Helper.isNullOrEmpty(claim.getTitle()) ? claim.getName() : claim.getTitle());

        String firstTag = claim.getFirstTag();
        vh.tagView.setVisibility(Helper.isNullOrEmpty(firstTag) ? View.INVISIBLE : View.VISIBLE);
        vh.tagView.setBackgroundResource(R.drawable.bg_tag);
        vh.tagView.setText(firstTag);
        vh.itemView.setSelected(isClaimSelected(claim));

        vh.itemView.setOnClickListener(view -> {
            if (selectedItems.contains(claim)) {
                selectedItems.remove(claim);
                if (listener != null) {
                    listener.onChannelItemDeselected(claim);
                }
            } else {
                selectedItems.add(claim);
                if (listener != null) {
                    listener.onChannelItemSelected(claim);
                }
            }
            notifyDataSetChanged();
        });
    }
}
