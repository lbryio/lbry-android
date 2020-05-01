package io.lbry.browser.adapter;

import android.content.Context;
import android.text.format.DateUtils;
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
import io.lbry.browser.utils.Helper;
import io.lbry.browser.utils.LbryUri;
import lombok.Setter;

public class ClaimListAdapter extends RecyclerView.Adapter<ClaimListAdapter.ViewHolder> {
    private static final int VIEW_TYPE_STREAM = 1;
    private static final int VIEW_TYPE_CHANNEL = 2;
    private static final int VIEW_TYPE_FEATURED = 3; // featured search result

    private Context context;
    private List<Claim> items;
    private List<Claim> selectedItems;
    @Setter
    private ClaimListItemListener listener;

    public ClaimListAdapter(List<Claim> items, Context context) {
        this.context = context;
        this.items = new ArrayList<>(items);
        this.selectedItems = new ArrayList<>();
    }

    public List<Claim> getSelectedItems() {
        return this.selectedItems;
    }
    public void clearSelectedItems() {
        this.selectedItems.clear();
    }
    public boolean isClaimSelected(Claim claim) {
        return selectedItems.contains(claim);
    }

    public Claim getFeaturedItem() {
        for (Claim claim : items) {
            if (claim.isFeatured()) {
                return claim;
            }
        }
        return null;
    }

    public void clearItems() {
        clearSelectedItems();
        this.items.clear();
        notifyDataSetChanged();
    }

    public void addFeaturedItem(Claim claim) {
        items.add(0, claim);
        notifyDataSetChanged();
    }

    public void addItems(List<Claim> claims) {
        for (Claim claim : claims) {
            if (!items.contains(claim)) {
                items.add(claim);
            }
        }
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        protected ImageView thumbnailView;
        protected View noThumbnailView;
        protected TextView alphaView;
        protected TextView vanityUrlView;
        protected TextView durationView;
        protected TextView titleView;
        protected TextView publisherView;
        protected TextView publishTimeView;
        protected View repostInfoView;
        protected TextView repostChannelView;
        public ViewHolder(View v) {
            super(v);
            alphaView = v.findViewById(R.id.claim_thumbnail_alpha);
            noThumbnailView = v.findViewById(R.id.claim_no_thumbnail);
            thumbnailView = v.findViewById(R.id.claim_thumbnail);
            vanityUrlView = v.findViewById(R.id.claim_vanity_url);
            durationView = v.findViewById(R.id.claim_duration);
            titleView = v.findViewById(R.id.claim_title);
            publisherView = v.findViewById(R.id.claim_publisher);
            publishTimeView = v.findViewById(R.id.claim_publish_time);
            repostInfoView = v.findViewById(R.id.claim_repost_info);
            repostChannelView = v.findViewById(R.id.claim_repost_channel);
        }
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    @Override
    public int getItemViewType(int position) {
        if (items.get(position).isFeatured()) {
            return VIEW_TYPE_FEATURED;
        }

        Claim claim = items.get(position);
        String valueType = items.get(position).getValueType();
        Claim actualClaim = Claim.TYPE_REPOST.equalsIgnoreCase(valueType) ? claim.getRepostedClaim() : claim;

        return Claim.TYPE_CHANNEL.equalsIgnoreCase(actualClaim.getValueType()) ? VIEW_TYPE_CHANNEL : VIEW_TYPE_STREAM;
    }

    @Override
    public ClaimListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int viewResourceId = -1;
        switch (viewType) {
            case VIEW_TYPE_FEATURED: viewResourceId = R.layout.list_item_featured_search_result; break;
            case VIEW_TYPE_CHANNEL: viewResourceId = R.layout.list_item_channel; break;
            case VIEW_TYPE_STREAM: default: viewResourceId = R.layout.list_item_stream; break;
        }

        View v = LayoutInflater.from(context).inflate(viewResourceId, parent, false);
        return new ClaimListAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ClaimListAdapter.ViewHolder vh, int position) {
        int type = getItemViewType(position);

        Claim original = items.get(position);
        boolean isRepost = Claim.TYPE_REPOST.equalsIgnoreCase(original.getValueType());
        final Claim item = Claim.TYPE_REPOST.equalsIgnoreCase(original.getValueType()) ? original.getRepostedClaim() : original;
        Claim.GenericMetadata metadata = item.getValue();
        Claim signingChannel = item.getSigningChannel();
        Claim.StreamMetadata streamMetadata = null;
        if (metadata instanceof Claim.StreamMetadata) {
            streamMetadata = (Claim.StreamMetadata) metadata;
        }
        String thumbnailUrl = item.getThumbnailUrl();
        long publishTime = (streamMetadata != null && streamMetadata.getReleaseTime() > 0) ? streamMetadata.getReleaseTime() * 1000 : item.getTimestamp() * 1000;
        int bgColor = Helper.generateRandomColorForValue(item.getClaimId());
        if (bgColor == 0) {
            bgColor = Helper.generateRandomColorForValue(item.getName());
        }

        vh.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.onClaimClicked(item);
                }
            }
        });

        vh.publisherView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null && signingChannel != null) {
                    listener.onClaimClicked(signingChannel);
                }
            }
        });

        vh.repostInfoView.setVisibility(isRepost ? View.VISIBLE : View.GONE);
        vh.repostChannelView.setText(isRepost ? original.getSigningChannel().getName() : null);
        vh.repostChannelView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.onClaimClicked(original.getSigningChannel());
                }
            }
        });

        vh.titleView.setText(Helper.isNullOrEmpty(item.getTitle()) ? item.getName() : item.getTitle());
        if (type == VIEW_TYPE_FEATURED) {
            LbryUri vanityUrl = new LbryUri();
            vanityUrl.setClaimName(item.getName());
            vh.vanityUrlView.setText(vanityUrl.toString());
        }

        vh.noThumbnailView.setVisibility(Helper.isNullOrEmpty(thumbnailUrl) ? View.VISIBLE : View.GONE);
        Helper.setIconViewBackgroundColor(vh.noThumbnailView, bgColor, false, context);

        if (type == VIEW_TYPE_FEATURED && item.isUnresolved()) {
            vh.durationView.setVisibility(View.GONE);
            vh.titleView.setText("Nothing here. Publish something!");
            vh.alphaView.setText(item.getName().substring(0, Math.min(5, item.getName().length() - 1)));
        } else {
            if (Claim.TYPE_STREAM.equalsIgnoreCase(item.getValueType())) {
                long duration = item.getDuration();
                if (!Helper.isNullOrEmpty(thumbnailUrl)) {
                    Glide.with(context.getApplicationContext()).
                            load(thumbnailUrl).
                            centerCrop().
                            placeholder(R.drawable.bg_thumbnail_placeholder).
                            into(vh.thumbnailView);
                }

                vh.alphaView.setText(item.getName().substring(0, Math.min(5, item.getName().length() - 1)));
                vh.publisherView.setText(signingChannel != null ? signingChannel.getName() : context.getString(R.string.anonymous));
                vh.publishTimeView.setText(DateUtils.getRelativeTimeSpanString(
                        publishTime, System.currentTimeMillis(), 0, DateUtils.FORMAT_ABBREV_RELATIVE));
                vh.durationView.setVisibility(duration > 0 ? View.VISIBLE : View.GONE);
                vh.durationView.setText(Helper.formatDuration(duration));
            } else if (Claim.TYPE_CHANNEL.equalsIgnoreCase(item.getValueType())) {
                if (!Helper.isNullOrEmpty(thumbnailUrl)) {
                    Glide.with(context.getApplicationContext()).
                            load(thumbnailUrl).
                            centerCrop().
                            placeholder(R.drawable.bg_thumbnail_placeholder).
                            apply(RequestOptions.circleCropTransform()).
                            into(vh.thumbnailView);
                }
                vh.alphaView.setText(item.getName().substring(1, 2).toUpperCase());
                vh.publisherView.setText(item.getName());
                vh.publishTimeView.setText(DateUtils.getRelativeTimeSpanString(
                        publishTime, System.currentTimeMillis(), 0, DateUtils.FORMAT_ABBREV_RELATIVE));
            }
        }
    }

    public interface ClaimListItemListener {
        void onClaimClicked(Claim claim);
    }
}
