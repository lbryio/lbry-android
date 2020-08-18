package io.lbry.browser.adapter;

import android.content.Context;
import android.graphics.Color;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import io.lbry.browser.R;
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

    public static class ViewHolder extends RecyclerView.ViewHolder {
        protected TextView titleView;
        protected TextView bodyView;
        protected TextView timeView;
        protected SolidIconView iconView;
        public ViewHolder(View v) {
            super(v);
            titleView = v.findViewById(R.id.notification_title);
            bodyView = v.findViewById(R.id.notification_body);
            timeView = v.findViewById(R.id.notification_time);
            iconView = v.findViewById(R.id.notification_icon);
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

    public void addTags(List<LbryNotification> notifications) {
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
        vh.titleView.setText(notification.getTitle());
        vh.titleView.setVisibility(!Helper.isNullOrEmpty(notification.getTitle()) ? View.VISIBLE : View.GONE);
        vh.bodyView.setText(notification.getDescription());
        vh.timeView.setText(DateUtils.getRelativeTimeSpanString(
                notification.getTimestamp().getTime(),
                System.currentTimeMillis(), 0, DateUtils.FORMAT_ABBREV_RELATIVE));


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