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
import lombok.Getter;
import lombok.Setter;

public class ChannelFilterListAdapter extends RecyclerView.Adapter<ChannelFilterListAdapter.ViewHolder> {
    private Context context;
    private List<Claim> items;
    @Getter
    @Setter
    private Claim selectedItem;
    @Setter
    private ChannelItemSelectionListener listener;

    public ChannelFilterListAdapter(Context context) {
        this.context = context;
        this.items = new ArrayList<>();

        // Always list the placeholder  as the first item
        Claim claim = new Claim();
        claim.setPlaceholder(true);
        items.add(claim);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        protected View mediaContainer;
        protected View alphaContainer;
        protected View allView;
        protected ImageView thumbnailView;
        protected TextView alphaView;
        protected TextView titleView;
        public ViewHolder(View v) {
            super(v);
            mediaContainer = v.findViewById(R.id.channel_filter_media_container);
            alphaContainer = v.findViewById(R.id.channel_filter_no_thumbnail);
            alphaView = v.findViewById(R.id.channel_filter_alpha_view);
            thumbnailView = v.findViewById(R.id.channel_filter_thumbnail);
            titleView = v.findViewById(R.id.channel_filter_title);
            allView = v.findViewById(R.id.channel_filter_all_container);
        }
    }

    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    public boolean isClaimSelected(Claim claim) {
        return claim.equals(selectedItem);
    }

    public void clearClaims() {
        items = new ArrayList<>(items.subList(0, 1));
        notifyDataSetChanged();
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
    public ChannelFilterListAdapter.ViewHolder onCreateViewHolder(ViewGroup root, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.list_item_channel_filter, root, false);
        return new ChannelFilterListAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ChannelFilterListAdapter.ViewHolder vh, int position) {
        Claim claim = items.get(position);
        vh.alphaView.setVisibility(claim.isPlaceholder() ? View.GONE : View.VISIBLE);
        vh.titleView.setVisibility(claim.isPlaceholder() ? View.INVISIBLE : View.VISIBLE);
        vh.allView.setVisibility(claim.isPlaceholder() ? View.VISIBLE : View.GONE);

        vh.titleView.setText(Helper.isNullOrEmpty(claim.getTitle()) ? claim.getName() : claim.getTitle());
        String thumbnailUrl = claim.getThumbnailUrl();
        if (!Helper.isNullOrEmpty(thumbnailUrl) && context != null) {
            Glide.with(context.getApplicationContext()).asBitmap().load(thumbnailUrl).apply(RequestOptions.circleCropTransform()).into(vh.thumbnailView);
        }
        vh.alphaContainer.setVisibility(claim.isPlaceholder() || Helper.isNullOrEmpty(thumbnailUrl) ? View.VISIBLE : View.GONE);
        vh.thumbnailView.setVisibility(claim.isPlaceholder() || Helper.isNullOrEmpty(thumbnailUrl) ? View.GONE : View.VISIBLE);
        vh.alphaView.setText(claim.isPlaceholder() ? null : claim.getName() != null ? claim.getName().substring(1, 2) : "");

        int bgColor = Helper.generateRandomColorForValue(claim.getClaimId());
        Helper.setIconViewBackgroundColor(vh.alphaContainer, bgColor, claim.isPlaceholder(), context);

        vh.itemView.setSelected(isClaimSelected(claim));
        vh.itemView.setOnClickListener(view -> {
            if (claim.isPlaceholder()) {
                selectedItem = null;
                if (listener != null) {
                    listener.onChannelSelectionCleared();
                }
            } else if (!claim.equals(selectedItem)) {
                selectedItem = claim;
                if (listener != null) {
                    listener.onChannelItemSelected(claim);
                }
            }
            notifyDataSetChanged();
        });
    }
}
