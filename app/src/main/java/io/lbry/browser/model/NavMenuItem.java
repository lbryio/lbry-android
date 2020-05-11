package io.lbry.browser.model;

import android.content.Context;

import androidx.core.content.res.ResourcesCompat;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class NavMenuItem {
    public static final int ID_GROUP_FIND_CONTENT = 100;
    public static final int ID_GROUP_YOUR_CONTENT = 200;
    public static final int ID_GROUP_WALLET = 300;
    public static final int ID_GROUP_OTHER = 400;

    // Find Content
    public static final int ID_ITEM_FOLLOWING = 101;
    public static final int ID_ITEM_EDITORS_CHOICE = 102;
    public static final int ID_ITEM_ALL_CONTENT = 103;

    // Your Content
    public static final int ID_ITEM_CHANNELS = 201;
    public static final int ID_ITEM_LIBRARY = 202;
    public static final int ID_ITEM_PUBLISHES = 203;
    public static final int ID_ITEM_NEW_PUBLISH = 204;

    // Wallet
    public static final int ID_ITEM_WALLET = 301;
    public static final int ID_ITEM_REWARDS = 302;
    public static final int ID_ITEM_INVITES = 303;

    // Other
    public static final int ID_ITEM_SETTINGS = 401;
    public static final int ID_ITEM_ABOUT = 402;

    private Context context;
    private int id;
    private boolean group;
    private int icon;
    private String title;
    private String extraLabel;
    private String name; // same as title, but only as en lang for events
    private List<NavMenuItem> items;

    public NavMenuItem(int id, int titleResourceId, boolean group, Context context) {
        this.context = context;
        this.id = id;
        this.group = group;

        if (titleResourceId > 0) {
            this.title = context.getString(titleResourceId);
        }
        if (group) {
            this.items = new ArrayList<>();
        }
    }

    public NavMenuItem(int id, int iconStringId, int titleResourceId, String name, Context context) {
        this.context = context;
        this.id = id;
        this.icon = iconStringId;
        this.title = context.getString(titleResourceId);
        this.name = name;
    }
}
