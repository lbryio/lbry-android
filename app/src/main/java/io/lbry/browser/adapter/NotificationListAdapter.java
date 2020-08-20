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
import java.util.List;

import io.lbry.browser.R;
import io.lbry.browser.model.Claim;
import io.lbry.browser.model.lbryinc.LbryNotification;
import io.lbry.browser.ui.controls.SolidIconView;
import io.lbry.browser.utils.Helper;
import lombok.Getter;
import lombok.Setter;

public class NotificationListAdapter extends RecyclerView.Adapter<NotificationListAdapter.ViewHolder> {

    private static final String RULE_CREATOR_SUBSCRIBER = "creator_subscriber";
    private static final String RULE_COMMENT = "comment";

    private Context context;
    private List<LbryNotification> items;
    @Setter
    private NotificationClickListener clickListener;
    @Getter
    @Setter
    private int customizeMode;

    public NotificationListAdapter(List<LbryNotification> notifications, Context context) {
        this.context = context;
        this.items = new ArrayList<>(notifications);
    }

    public List<LbryNotification> getItems() {
        return new ArrayList<>(items);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        protected View layoutView;
        protected TextView titleView;
        protected TextView bodyView;
        protected TextView timeView;
        protected SolidIconView iconView;
        protected ImageView thumbnailView;
        public ViewHolder(View v) {
            super(v);
            layoutView = v.findViewById(R.id.notification_layout);
            titleView = v.findViewById(R.id.notification_title);
            bodyView = v.findViewById(R.id.notification_body);
            timeView = v.findViewById(R.id.notification_time);
            iconView = v.findViewById(R.id.notification_icon);
            thumbnailView = v.findViewById(R.id.notification_author_thumbnail);
        }
    }

    public int getItemCount() {
        return items != null ? items.size() : 0;
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

        vh.layoutView.setBackgroundColor(ContextCompat.getColor(context, notification.isSeen() ? R.color.white : R.color.nextLbryGreenSemiTransparent));

        vh.titleView.setVisibility(!Helper.isNullOrEmpty(notification.getTitle()) ? View.VISIBLE : View.GONE);
        vh.titleView.setText(notification.getTitle());
        vh.bodyView.setText(notification.getDescription());
        vh.timeView.setText(DateUtils.getRelativeTimeSpanString(
                notification.getTimestamp().getTime(),
                System.currentTimeMillis(), 0, DateUtils.FORMAT_ABBREV_RELATIVE));

        vh.thumbnailView.setVisibility(notification.getCommentAuthor() == null ? View.INVISIBLE : View.VISIBLE);
        if (notification.getCommentAuthor() != null) {
            Glide.with(context.getApplicationContext()).load(
                    notification.getCommentAuthor().getThumbnailUrl()).apply(RequestOptions.circleCropTransform()).into(vh.thumbnailView);
        }

        vh.iconView.setVisibility(notification.getCommentAuthor() != null ? View.INVISIBLE : View.VISIBLE);
        vh.iconView.setText(getStringIdForRule(notification.getRule()));
        vh.iconView.setTextColor(getColorForRule(notification.getRule()));

        vh.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (clickListener != null) {
                    clickListener.onNotificationClicked(notification);
                }
            }
        });
    }

    public interface NotificationClickListener {
        void onNotificationClicked(LbryNotification notification);
    }
}