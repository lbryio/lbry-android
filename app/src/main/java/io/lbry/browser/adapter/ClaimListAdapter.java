package io.lbry.browser.adapter;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.snackbar.Snackbar;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.lbry.browser.MainActivity;
import io.lbry.browser.R;
import io.lbry.browser.listener.SelectionModeListener;
import io.lbry.browser.model.Claim;
import io.lbry.browser.model.LbryFile;
import io.lbry.browser.utils.Helper;
import io.lbry.browser.utils.LbryUri;
import io.lbry.browser.utils.Lbryio;
import lombok.Getter;
import lombok.Setter;

public class ClaimListAdapter extends RecyclerView.Adapter<ClaimListAdapter.ViewHolder> {
    private static final int VIEW_TYPE_STREAM = 1;
    private static final int VIEW_TYPE_CHANNEL = 2;
    private static final int VIEW_TYPE_FEATURED = 3; // featured search result

    private Map<String, Claim> quickClaimIdMap;
    private Map<String, Claim> quickClaimUrlMap;
    private Map<String, Boolean> notFoundClaimIdMap;
    private Map<String, Boolean> notFoundClaimUrlMap;

    @Setter
    private boolean hideFee;
    @Setter
    private boolean canEnterSelectionMode;
    private Context context;
    private List<Claim> items;
    private List<Claim> selectedItems;
    @Setter
    private ClaimListItemListener listener;
    @Getter
    @Setter
    private boolean inSelectionMode;
    @Setter
    private SelectionModeListener selectionModeListener;
    private float scale;

    public ClaimListAdapter(List<Claim> items, Context context) {
        this.context = context;
        this.items = new ArrayList<>(items);
        this.selectedItems = new ArrayList<>();
        quickClaimIdMap = new HashMap<>();
        quickClaimUrlMap = new HashMap<>();
        notFoundClaimIdMap = new HashMap<>();
        notFoundClaimUrlMap = new HashMap<>();
        if (context != null) {
            scale = context.getResources().getDisplayMetrics().density;
        }
    }

    public List<Claim> getSelectedItems() {
        return this.selectedItems;
    }
    public int getSelectedCount() {
        return selectedItems != null ? selectedItems.size() : 0;
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

    public void removeFeaturedItem() {
        int featuredIndex = -1;
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).isFeatured()) {
                featuredIndex = i;
                break;
            }
        }
        if (featuredIndex > -1) {
            items.remove(featuredIndex);
        }
    }

    public List<Claim> getItems() {
        return new ArrayList<>(this.items);
    }

    public void updateSigningChannelForClaim(Claim resolvedClaim) {
        for (Claim claim : items) {
            if (claim.getClaimId().equalsIgnoreCase(resolvedClaim.getClaimId())) {
                claim.setSigningChannel(resolvedClaim.getSigningChannel());
            }
        }
    }

    public void clearItems() {
        clearSelectedItems();
        this.items.clear();
        quickClaimIdMap.clear();
        quickClaimUrlMap.clear();
        notFoundClaimIdMap.clear();
        notFoundClaimUrlMap.clear();
        notifyDataSetChanged();
    }

    public Claim getLastItem() {
        return items.size() > 0 ? items.get(items.size() - 1) : null;
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

        notFoundClaimUrlMap.clear();
        notFoundClaimIdMap.clear();
        notifyDataSetChanged();
    }
    public void setItems(List<Claim> claims) {
        items = new ArrayList<>(claims);
        notifyDataSetChanged();
    }

    public void removeItems(List<Claim> claims) {
        items.removeAll(claims);
        notifyDataSetChanged();
    }

    public void removeItem(Claim claim) {
        items.remove(claim);
        selectedItems.remove(claim);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        protected View feeContainer;
        protected TextView feeView;
        protected ImageView thumbnailView;
        protected View noThumbnailView;
        protected TextView alphaView;
        protected TextView vanityUrlView;
        protected TextView durationView;
        protected TextView titleView;
        protected TextView publisherView;
        protected TextView publishTimeView;
        protected TextView pendingTextView;
        protected View repostInfoView;
        protected TextView repostChannelView;
        protected View selectedOverlayView;
        protected TextView fileSizeView;
        protected ProgressBar downloadProgressView;
        protected TextView deviceView;
        public ViewHolder(View v) {
            super(v);
            feeContainer = v.findViewById(R.id.claim_fee_container);
            feeView = v.findViewById(R.id.claim_fee);
            alphaView = v.findViewById(R.id.claim_thumbnail_alpha);
            noThumbnailView = v.findViewById(R.id.claim_no_thumbnail);
            thumbnailView = v.findViewById(R.id.claim_thumbnail);
            vanityUrlView = v.findViewById(R.id.claim_vanity_url);
            durationView = v.findViewById(R.id.claim_duration);
            titleView = v.findViewById(R.id.claim_title);
            publisherView = v.findViewById(R.id.claim_publisher);
            publishTimeView = v.findViewById(R.id.claim_publish_time);
            pendingTextView = v.findViewById(R.id.claim_pending_text);
            repostInfoView = v.findViewById(R.id.claim_repost_info);
            repostChannelView = v.findViewById(R.id.claim_repost_channel);
            selectedOverlayView = v.findViewById(R.id.claim_selected_overlay);
            fileSizeView = v.findViewById(R.id.claim_file_size);
            downloadProgressView = v.findViewById(R.id.claim_download_progress);
            deviceView = v.findViewById(R.id.claim_view_device);
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

    public void updateFileForClaimByIdOrUrl(LbryFile file, String claimId, String url) {
        updateFileForClaimByIdOrUrl(file, claimId, url,  false);
    }
    public void updateFileForClaimByIdOrUrl(LbryFile file, String claimId, String url, boolean skipNotFound) {
        if (!skipNotFound) {
            if (notFoundClaimIdMap.containsKey(claimId) && notFoundClaimUrlMap.containsKey(url)) {
                return;
            }
        }
        if (quickClaimIdMap.containsKey(claimId)) {
            quickClaimIdMap.get(claimId).setFile(file);
            notifyDataSetChanged();
            return;
        }
        if (quickClaimUrlMap.containsKey(claimId)) {
            quickClaimUrlMap.get(claimId).setFile(file);
            notifyDataSetChanged();
            return;
        }

        boolean claimFound = false;
        for (int i = 0; i < items.size(); i++) {
            Claim claim = items.get(i);
            if (claimId.equalsIgnoreCase(claim.getClaimId()) || url.equalsIgnoreCase(claim.getPermanentUrl())) {
                quickClaimIdMap.put(claimId, claim);
                quickClaimUrlMap.put(url, claim);
                claim.setFile(file);
                notifyDataSetChanged();
                claimFound = true;
                break;
            }
        }

        if (!claimFound) {
            notFoundClaimIdMap.put(claimId, true);
            notFoundClaimUrlMap.put(url, true);
        }
    }
    public void clearFileForClaimOrUrl(String outpoint, String url) {
        clearFileForClaimOrUrl(outpoint, url, false);
        notifyDataSetChanged();
    }


    public void clearFileForClaimOrUrl(String outpoint, String url, boolean remove) {
        int claimIndex = -1;
        for (int i = 0; i < items.size(); i++) {
            Claim claim = items.get(i);
            if (outpoint.equalsIgnoreCase(claim.getOutpoint()) || url.equalsIgnoreCase(claim.getPermanentUrl())) {
                claimIndex = i;
                claim.setFile(null);
                break;
            }
        }
        if (remove && claimIndex > -1) {
            Claim removed = items.remove(claimIndex);
            selectedItems.remove(removed);
        }

        notifyDataSetChanged();
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

    public int getScaledValue(int value) {
        return (int) (value * scale + 0.5f);
    }

    @Override
    public void onBindViewHolder(ClaimListAdapter.ViewHolder vh, int position) {
        int type = getItemViewType(position);
        int paddingTop = position == 0 ? 16 : 8;
        int paddingBottom = position == getItemCount() - 1 ? 16 : 8;
        int paddingTopScaled = getScaledValue(paddingTop);
        int paddingBottomScaled = getScaledValue(paddingBottom);
        vh.itemView.setPadding(vh.itemView.getPaddingLeft(), paddingTopScaled, vh.itemView.getPaddingRight(), paddingBottomScaled);

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

        boolean isPending = item.getConfirmations() == 0;
        boolean isSelected = isClaimSelected(item);
        vh.itemView.setSelected(isSelected);
        vh.selectedOverlayView.setVisibility(isSelected ? View.VISIBLE : View.GONE);
        vh.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isPending) {
                    Snackbar snackbar = Snackbar.make(vh.itemView, R.string.item_pending_blockchain, Snackbar.LENGTH_LONG);
                    TextView snackbarText = snackbar.getView().findViewById(com.google.android.material.R.id.snackbar_text);
                    snackbarText.setMaxLines(5);
                    snackbar.show();
                    return;
                }

                if (inSelectionMode) {
                    toggleSelectedClaim(original);
                } else {
                    if (listener != null) {
                        listener.onClaimClicked(item);
                    }
                }
            }
        });
        vh.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (!canEnterSelectionMode) {
                    return false;
                }

                if (isPending) {
                    Snackbar snackbar = Snackbar.make(vh.itemView, R.string.item_pending_blockchain, Snackbar.LENGTH_LONG);
                    TextView snackbarText = snackbar.getView().findViewById(com.google.android.material.R.id.snackbar_text);
                    snackbarText.setMaxLines(5);
                    snackbar.show();
                    return false;
                }

                if (!inSelectionMode) {
                    inSelectionMode = true;
                    if (selectionModeListener != null) {
                        selectionModeListener.onEnterSelectionMode();
                    }
                }
                toggleSelectedClaim(original);
                return true;
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

        vh.publishTimeView.setVisibility(!isPending ? View.VISIBLE : View.GONE);
        vh.pendingTextView.setVisibility(isPending ? View.VISIBLE : View.GONE);
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

        vh.feeContainer.setVisibility(item.isUnresolved() || !Claim.TYPE_STREAM.equalsIgnoreCase(item.getValueType()) ? View.GONE : View.VISIBLE);
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
                            asBitmap().
                            load(thumbnailUrl).
                            centerCrop().
                            placeholder(R.drawable.bg_thumbnail_placeholder).
                            into(vh.thumbnailView);
                    vh.thumbnailView.setVisibility(View.VISIBLE);
                } else {
                    vh.thumbnailView.setVisibility(View.GONE);
                }

                BigDecimal cost = item.getActualCost(Lbryio.LBCUSDRate);
                vh.feeContainer.setVisibility(cost.doubleValue() > 0 && !hideFee ? View.VISIBLE : View.GONE);
                vh.feeView.setText(cost.doubleValue() > 0 ? Helper.shortCurrencyFormat(cost.doubleValue()) : "Paid");
                vh.alphaView.setText(item.getName().substring(0, Math.min(5, item.getName().length() - 1)));
                vh.publisherView.setText(signingChannel != null ? signingChannel.getName() : context.getString(R.string.anonymous));
                vh.publishTimeView.setText(DateUtils.getRelativeTimeSpanString(
                        publishTime, System.currentTimeMillis(), 0, DateUtils.FORMAT_ABBREV_RELATIVE));
                vh.durationView.setVisibility(duration > 0 ? View.VISIBLE : View.GONE);
                vh.durationView.setText(Helper.formatDuration(duration));

                LbryFile claimFile = item.getFile();
                boolean isDownloading = false;
                int progress = 0;
                String fileSizeString = claimFile == null ? null : Helper.formatBytes(claimFile.getTotalBytes(), false);
                if (claimFile != null &&
                        !Helper.isNullOrEmpty(claimFile.getDownloadPath()) &&
                        !claimFile.isCompleted() &&
                        claimFile.getWrittenBytes() < claimFile.getTotalBytes()) {
                    isDownloading = true;
                    progress = claimFile.getTotalBytes() > 0 ?
                            Double.valueOf(((double) claimFile.getWrittenBytes() / (double) claimFile.getTotalBytes()) * 100.0).intValue() : 0;
                    fileSizeString = String.format("%s / %s",
                            Helper.formatBytes(claimFile.getWrittenBytes(), false),
                            Helper.formatBytes(claimFile.getTotalBytes(), false));
                }

                Helper.setViewText(vh.fileSizeView, claimFile != null && !Helper.isNullOrEmpty(claimFile.getDownloadPath()) ? fileSizeString : null);
                Helper.setViewVisibility(vh.downloadProgressView, isDownloading ? View.VISIBLE : View.INVISIBLE);
                Helper.setViewProgress(vh.downloadProgressView, progress);
                Helper.setViewText(vh.deviceView, item.getDevice());
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

    private void toggleSelectedClaim(Claim claim) {
        if (selectedItems.contains(claim)) {
            selectedItems.remove(claim);
        } else {
            selectedItems.add(claim);
        }

        if (selectionModeListener != null) {
            selectionModeListener.onItemSelectionToggled();
        }

        if (selectedItems.size() == 0) {
            inSelectionMode = false;
            if (selectionModeListener != null) {
                selectionModeListener.onExitSelectionMode();
            }
        }

        notifyDataSetChanged();
    }

    public interface ClaimListItemListener {
        void onClaimClicked(Claim claim);
    }
}
