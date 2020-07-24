package io.lbry.browser.adapter;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import io.lbry.browser.R;
import io.lbry.browser.model.lbryinc.LbryNotification;
import io.lbry.browser.utils.Helper;
import lombok.Getter;
import lombok.Setter;

public class NotificationListAdapter extends RecyclerView.Adapter<NotificationListAdapter.ViewHolder> {

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
        public ViewHolder(View v) {
            super(v);
            titleView = v.findViewById(R.id.notification_title);
            bodyView = v.findViewById(R.id.notification_body);
            timeView = v.findViewById(R.id.notification_time);
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

    @Override
    public void onBindViewHolder(NotificationListAdapter.ViewHolder vh, int position) {
        LbryNotification notification = items.get(position);
        vh.titleView.setText(notification.getTitle());
        vh.titleView.setVisibility(!Helper.isNullOrEmpty(notification.getTitle()) ? View.VISIBLE : View.GONE);
        vh.bodyView.setText(notification.getDescription());
        vh.timeView.setText(DateUtils.getRelativeTimeSpanString(
                notification.getTimestamp().getTime(),
                System.currentTimeMillis(), 0, DateUtils.FORMAT_ABBREV_RELATIVE));

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