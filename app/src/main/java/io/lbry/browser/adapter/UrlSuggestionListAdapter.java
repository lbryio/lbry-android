package io.lbry.browser.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import io.lbry.browser.R;
import io.lbry.browser.exceptions.LbryUriException;
import io.lbry.browser.model.Claim;
import io.lbry.browser.model.UrlSuggestion;
import io.lbry.browser.ui.controls.SolidIconView;
import io.lbry.browser.utils.Helper;
import io.lbry.browser.utils.LbryUri;
import lombok.Setter;

public class UrlSuggestionListAdapter extends RecyclerView.Adapter<UrlSuggestionListAdapter.ViewHolder> {
    private Context context;
    private List<UrlSuggestion> items;
    @Setter
    private UrlSuggestionClickListener listener;

    public UrlSuggestionListAdapter(Context context) {
        this.context = context;
        this.items = new ArrayList<>();
    }

    public void clear() {
        items.clear();
        notifyDataSetChanged();
    }

    public List<UrlSuggestion> getItems() {
        return new ArrayList<>(items);
    }

    public List<String> getItemUrls() {
        List<String> uris = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            LbryUri uri = items.get(i).getUri();
            if (uri != null) {
                uris.add(uri.toString());
            }
        }
        return uris;
    }

    public void setClaimForUrl(LbryUri url, Claim claim) {
        for (int i = 0; i < items.size(); i++) {
            LbryUri thisUrl = items.get(i).getUri();
            try {
                if (thisUrl != null) {
                    LbryUri vanity = LbryUri.parse(thisUrl.toVanityString());
                    if (thisUrl.equals(url) || vanity.equals(url)) {
                        items.get(i).setClaim(claim);
                    }
                }
            } catch (LbryUriException ex) {
                // pass
            }
        }
    }

    public void addUrlSuggestions(List<UrlSuggestion> urlSuggestions) {
        for (UrlSuggestion urlSuggestion : urlSuggestions) {
            if (!items.contains(urlSuggestion)) {
                items.add(urlSuggestion);
            }
        }
        notifyDataSetChanged();
    }

    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    @Override
    public UrlSuggestionListAdapter.ViewHolder onCreateViewHolder(ViewGroup root, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.list_item_url_suggestion, root, false);
        return new UrlSuggestionListAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(UrlSuggestionListAdapter.ViewHolder vh, int position) {
        UrlSuggestion item = items.get(position);

        String fullTitle, desc;
        int iconStringId;
        switch (item.getType()) {
            case UrlSuggestion.TYPE_CHANNEL:
                iconStringId = R.string.fa_at;
                fullTitle = item.getTitle();
                desc = item.getClaim() != null ? item.getClaim().getTitle() :
                        ((item.isUseTextAsDescription() && !Helper.isNullOrEmpty(item.getText())) ? item.getText() : String.format(context.getString(R.string.view_channel_url_desc), item.getText()));
                break;
            case UrlSuggestion.TYPE_TAG:
                iconStringId = R.string.fa_hashtag;
                fullTitle = String.format(context.getString(R.string.tag_url_title), item.getText());
                desc = String.format(context.getString(R.string.explore_tag_url_desc), item.getText());
                break;
            case UrlSuggestion.TYPE_SEARCH:
                iconStringId = R.string.fa_search;
                fullTitle = String.format(context.getString(R.string.search_url_title), item.getText());
                desc = String.format(context.getString(R.string.search_url_desc), item.getText());
                break;
            case UrlSuggestion.TYPE_FILE:
            default:
                iconStringId = R.string.fa_file;
                fullTitle = item.getTitle();
                desc = item.getClaim() != null ? item.getClaim().getTitle() :
                        ((item.isUseTextAsDescription() && !Helper.isNullOrEmpty(item.getText())) ? item.getText() : String.format(context.getString(R.string.view_file_url_desc), item.getText()));
                break;
        }

        vh.iconView.setText(iconStringId);
        vh.titleView.setText(fullTitle);
        vh.descView.setText(desc);

        vh.itemView.setOnClickListener(view -> {
            if (listener != null) {
                listener.onUrlSuggestionClicked(item);
            }
        });
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        protected SolidIconView iconView;
        protected TextView titleView;
        protected TextView descView;
        public ViewHolder(View v) {
            super(v);
            iconView = v.findViewById(R.id.url_suggestion_icon);
            titleView = v.findViewById(R.id.url_suggestion_title);
            descView = v.findViewById(R.id.url_suggestion_description);
        }
    }

    public interface UrlSuggestionClickListener {
        void onUrlSuggestionClicked(UrlSuggestion urlSuggestion);
    }
}
