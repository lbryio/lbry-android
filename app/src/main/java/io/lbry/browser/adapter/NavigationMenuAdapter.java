package io.lbry.browser.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import io.lbry.browser.R;
import io.lbry.browser.model.NavMenuItem;
import io.lbry.browser.ui.controls.SolidIconView;
import io.lbry.browser.utils.Helper;
import lombok.Getter;
import lombok.Setter;

public class NavigationMenuAdapter extends RecyclerView.Adapter<NavigationMenuAdapter.ViewHolder> {
    private static final int TYPE_GROUP = 1;
    private static final int TYPE_ITEM = 2;

    private Context context;
    private List<NavMenuItem> menuItems;
    private NavMenuItem currentItem;
    @Setter
    private NavigationMenuItemClickListener listener;

    public NavigationMenuAdapter(List<NavMenuItem> menuItems, Context context) {
        this.menuItems = new ArrayList<>(menuItems);
        this.context = context;
    }

    public void setCurrentItem(int id) {
        for (NavMenuItem item : menuItems) {
            if (item.getId() == id) {
                this.currentItem = item;
                break;
            }
        }
        notifyDataSetChanged();
    }

    public void setExtraLabelForItem(int id, String extraLabel) {
        for (NavMenuItem item : menuItems) {
            if (item.getId() == id) {
                item.setExtraLabel(extraLabel);
                break;
            }
        }
        notifyDataSetChanged();
    }

    public void setCurrentItem(NavMenuItem currentItem) {
        this.currentItem = currentItem;
        notifyDataSetChanged();
    }

    public int getCurrentItemId() {
        return currentItem != null ? currentItem.getId() : -1;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        protected SolidIconView iconView;
        protected TextView titleView;
        public ViewHolder(View v) {
            super(v);
            titleView = v.findViewById(R.id.nav_menu_title);
            iconView = v.findViewById(R.id.nav_menu_item_icon);
        }
    }

    @Override
    public int getItemCount() {
        return menuItems != null ? menuItems.size() : 0;
    }

    @Override
    public int getItemViewType(int position) {
        return menuItems.get(position).isGroup() ? TYPE_GROUP : TYPE_ITEM;
    }

    @Override
    public NavigationMenuAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(viewType == TYPE_GROUP ?
                R.layout.list_item_nav_menu_group : R.layout.list_item_nav_menu_item, parent, false);
        return new NavigationMenuAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder vh, int position) {
        int type = getItemViewType(position);
        NavMenuItem item = menuItems.get(position);
        String displayTitle = !Helper.isNullOrEmpty(item.getExtraLabel()) ? String.format("%s (%s)", item.getTitle(), item.getExtraLabel()) : item.getTitle();
        vh.titleView.setText(displayTitle);
        if (type == TYPE_ITEM && vh.iconView != null) {
            vh.iconView.setText(item.getIcon());
        }
        vh.itemView.setSelected(item.equals(currentItem));
        vh.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.onNavigationMenuItemClicked(item);
                }
            }
        });
    }

    public interface NavigationMenuItemClickListener {
        void onNavigationMenuItemClicked(NavMenuItem menuItem);
    }
}
