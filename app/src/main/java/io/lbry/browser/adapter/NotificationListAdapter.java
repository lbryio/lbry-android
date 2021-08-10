package io.lbry.browser.adapter;

import android.content.Context;
import android.graphics.Color;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import io.lbry.browser.R;
import io.lbry.browser.listener.SelectionModeListener;
import io.lbry.browser.model.Claim;
import io.lbry.browser.model.lbryinc.LbryNotification;
import io.lbry.browser.ui.controls.SolidIconView;
import io.lbry.browser.utils.Helper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Data
@EqualsAndHashCode(callSuper = false)
public class NotificationListAdapter extends RecyclerView.Adapter<NotificationListAdapter.ViewHolder> {

    private static final String RULE_CREATOR_SUBSCRIBER = "creator_subscriber";
    private static final String RULE_COMMENT = "comment";

    private final Context context;
    private final List<LbryNotification> items;
    private final List<LbryNotification> selectedItems;
    @Setter
    private NotificationClickListener clickListener;
    @Getter
    @Setter
    private boolean inSelectionMode;
    @Setter
    private SelectionModeListener selectionModeListener;

    public NotificationListAdapter(List<LbryNotification> notifications, Context context) {
        this.context = context;
        this.items = new ArrayList<>(notifications);
        this.selectedItems = new ArrayList<>();
        Collections.sort(items, Collections.reverseOrder(new LbryNotification()));
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        protected final View layoutView;
        protected final TextView titleView;
        protected final TextView bodyView;
        protected final TextView timeView;
        protected final SolidIconView iconView;
        protected final ImageView thumbnailView;
        protected final View selectedOverlayView;
        public ViewHolder(View v) {
            super(v);
            layoutView = v.findViewById(R.id.notification_layout);
            titleView = v.findViewById(R.id.notification_title);
            bodyView = v.findViewById(R.id.notification_body);
            timeView = v.findViewById(R.id.notification_time);
            iconView = v.findViewById(R.id.notification_icon);
            thumbnailView = v.findViewById(R.id.notification_author_thumbnail);
            selectedOverlayView = v.findViewById(R.id.notification_selected_overlay);
        }
    }

    public int getItemCount() {
        return items != null ? items.size() : 0;
    }
    public List<LbryNotification> getSelectedItems() {
        return this.selectedItems;
    }
    public int getSelectedCount() {
        return selectedItems != null ? selectedItems.size() : 0;
    }
    public void clearSelectedItems() {
        this.selectedItems.clear();
    }
    public boolean isNotificationSelected(LbryNotification notification) {
        return selectedItems.contains(notification);
    }

    public void insertNotification(LbryNotification notification, int index) {
        if (!items.contains(notification)) {
            items.add(index, notification);
        }
        notifyDataSetChanged();
    }

    public void addNotification(LbryNotification notification) {
        if (!items.contains(notification)) {
            items.add(notification);
        }
        notifyDataSetChanged();
    }
    public void removeNotifications(List<LbryNotification> notifications) {
        for (LbryNotification notification : notifications) {
            items.remove(notification);
        }
        notifyDataSetChanged();
    }

    public List<String> getAuthorUrls() {
        List<String> urls = new ArrayList<>();
        for (LbryNotification item : items) {
            if (!Helper.isNullOrEmpty(item.getAuthorUrl())) {
                urls.add(item.getAuthorUrl());
            }
        }
        return urls;
    }

    public void updateAuthorClaims(List<Claim> claims) {
        for (Claim claim : claims) {
            if (claim != null && claim.getThumbnailUrl() != null) {
                updateClaimForAuthorUrl(claim);
            }
        }
        notifyDataSetChanged();
    }

    private void updateClaimForAuthorUrl(Claim claim) {
        for (LbryNotification item : items) {
            if (claim.getPermanentUrl().equalsIgnoreCase(item.getAuthorUrl())) {
                item.setCommentAuthor(claim);
            }
        }
    }

    public void addNotifications(List<LbryNotification> notifications) {
        for (LbryNotification notification : notifications) {
            if (!items.contains(notification)) {
                items.add(notification);
            }
        }
        Collections.sort(items, Collections.reverseOrder(new LbryNotification()));
        notifyDataSetChanged();
    }

    @Override
    public NotificationListAdapter.ViewHolder onCreateViewHolder(ViewGroup root, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.list_item_notification, root, false);
        return new NotificationListAdapter.ViewHolder(v);
    }

    private int getStringIdForRule(String rule) {
        if (RULE_CREATOR_SUBSCRIBER.equalsIgnoreCase(rule)) {
            return R.string.fa_heart;
        }
        if (RULE_COMMENT.equalsIgnoreCase(rule)) {
            return R.string.fa_comment_alt;
        }
        return R.string.fa_asterisk;
    }

    private int getColorForRule(String rule) {
        if (RULE_CREATOR_SUBSCRIBER.equalsIgnoreCase(rule)) {
            return Color.RED;
        }
        if (RULE_COMMENT.equalsIgnoreCase(rule)) {
            return ContextCompat.getColor(context, R.color.nextLbryGreen);
        }

        return ContextCompat.getColor(context, R.color.lbryGreen);
    }

    @Override
    public void onBindViewHolder(NotificationListAdapter.ViewHolder vh, int position) {
        LbryNotification notification = items.get(position);
        vh.layoutView.setBackgroundColor(ContextCompat.getColor(context, notification.isSeen() ? android.R.color.transparent : R.color.nextLbryGreenSemiTransparent));
        vh.selectedOverlayView.setVisibility(isNotificationSelected(notification) ? View.VISIBLE : View.GONE);

        vh.titleView.setVisibility(!Helper.isNullOrEmpty(notification.getTitle()) ? View.VISIBLE : View.GONE);
        vh.titleView.setText(notification.getTitle());
        vh.bodyView.setText(notification.getDescription());
        vh.timeView.setText(DateUtils.getRelativeTimeSpanString(
                getLocalNotificationTime(notification), System.currentTimeMillis(), 0, DateUtils.FORMAT_ABBREV_RELATIVE));

        vh.thumbnailView.setVisibility(notification.getCommentAuthor() == null ? View.INVISIBLE : View.VISIBLE);
        if (notification.getCommentAuthor() != null) {
            Glide.with(context.getApplicationContext()).load(
                    notification.getCommentAuthor().getThumbnailUrl(vh.thumbnailView.getLayoutParams().width, vh.thumbnailView.getLayoutParams().height, 85)).apply(RequestOptions.circleCropTransform()).into(vh.thumbnailView);
        }

        vh.iconView.setVisibility(notification.getCommentAuthor() != null ? View.INVISIBLE : View.VISIBLE);
        vh.iconView.setText(getStringIdForRule(notification.getRule()));
        vh.iconView.setTextColor(getColorForRule(notification.getRule()));

        vh.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (inSelectionMode) {
                    toggleSelectedNotification(notification);
                } else {
                    if (clickListener != null) {
                        clickListener.onNotificationClicked(notification);
                    }
                }
            }
        });
        vh.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (!inSelectionMode) {
                    inSelectionMode = true;
                    if (selectionModeListener != null) {
                        selectionModeListener.onEnterSelectionMode();
                    }
                }
                toggleSelectedNotification(notification);
                return true;
            }
        });
    }

    private void toggleSelectedNotification(LbryNotification notification) {
        if (selectedItems.contains(notification)) {
            selectedItems.remove(notification);
        } else {
            selectedItems.add(notification);
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

    private long getLocalNotificationTime(LbryNotification notification) {
        TimeZone utcTZ = TimeZone.getTimeZone("UTC");
        TimeZone targetTZ = TimeZone.getDefault();
        Calendar cal = new GregorianCalendar(utcTZ);
        cal.setTimeInMillis(notification.getTimestamp().getTime());

        cal.add(Calendar.MILLISECOND, utcTZ.getRawOffset() * -1);
        cal.add(Calendar.MILLISECOND, targetTZ.getRawOffset());
        return cal.getTimeInMillis();
    }

    public interface NotificationClickListener {
        void onNotificationClicked(LbryNotification notification);
    }
}