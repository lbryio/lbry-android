package io.lbry.browser.ui.channel;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.List;
import java.util.Map;

import io.lbry.browser.MainActivity;
import io.lbry.browser.R;
import io.lbry.browser.model.Claim;
import io.lbry.browser.tasks.ResolveTask;
import io.lbry.browser.ui.BaseFragment;
import io.lbry.browser.utils.Helper;
import io.lbry.browser.utils.Lbry;
import lombok.SneakyThrows;

public class ChannelFragment extends BaseFragment {
    private Claim claim;
    private boolean resolving;
    private String url;

    private View layoutResolving;
    private View layoutDisplayArea;
    private ImageView imageCover;
    private ImageView imageThumbnail;
    private View noThumbnailView;
    private TextView textAlpha;
    private TextView textTitle;
    private TextView textFollowerCount;
    private TabLayout tabLayout;
    private ViewPager2 tabPager;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_channel, container, false);

        layoutDisplayArea = root.findViewById(R.id.channel_view_claim_display_area);
        layoutResolving = root.findViewById(R.id.channel_view_loading_container);

        imageCover = root.findViewById(R.id.channel_view_cover_image);
        imageThumbnail = root.findViewById(R.id.channel_view_thumbnail);
        noThumbnailView = root.findViewById(R.id.channel_view_no_thumbnail);
        textAlpha = root.findViewById(R.id.channel_view_icon_alpha);
        textTitle = root.findViewById(R.id.channel_view_title);
        textFollowerCount = root.findViewById(R.id.channel_view_follower_count);

        tabPager = root.findViewById(R.id.channel_view_pager);
        tabLayout = root.findViewById(R.id.channel_view_tabs);
        tabPager.setSaveEnabled(false);

        return root;
    }

    public void onResume() {
        super.onResume();
        checkParams();
    }

    private void checkParams() {
        boolean updateRequired = false;
        Map<String, Object> params = getParams();

        if (params.containsKey("claim")) {
            Claim claim = (Claim) params.get("claim");
            if (claim != null && !claim.equals(this.claim)) {
                this.claim = claim;
                updateRequired = true;
            }
        }
        if (!updateRequired && params.containsKey("url")) {
            String newUrl = params.get("url").toString();
            if (!newUrl.equalsIgnoreCase(url) || claim == null) {
                this.claim = null;
                this.url = newUrl;
                updateRequired = true;
            }
        }
        if (updateRequired) {
            if (!Helper.isNullOrEmpty(url)) {
                resolveUrl();
            } else if (claim == null) {
                // nothing at this location
                renderNothingAtLocation();
            }
        }

        if (claim != null) {
            renderClaim();
        }
    }

    private void resolveUrl() {
        layoutDisplayArea.setVisibility(View.INVISIBLE);
        ResolveTask task = new ResolveTask(url, Lbry.LBRY_TV_CONNECTION_STRING, layoutResolving, new ResolveTask.ResolveResultHandler() {
            @Override
            public void onSuccess(List<Claim> claims) {
                if (claims.size() > 0) {
                    claim = claims.get(0);
                    renderClaim();
                    // TODO: Load follower count
                } else {
                    renderNothingAtLocation();
                }
            }

            @Override
            public void onError(Exception error) {
                renderNothingAtLocation();
            }
        });
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void renderNothingAtLocation() {

    }

    public void setParams(Map<String, Object> params) {
        super.setParams(params);
        if (getView() != null) {
            checkParams();
        }
    }

    private void renderClaim() {
        if (claim == null) {
            renderNothingAtLocation();
            return;
        }

        layoutDisplayArea.setVisibility(View.VISIBLE);

        String thumbnailUrl = claim.getThumbnailUrl();
        String coverUrl = claim.getCoverUrl();
        textTitle.setText(Helper.isNullOrEmpty(claim.getTitle()) ? claim.getName() : claim.getTitle());

        if (!Helper.isNullOrEmpty(coverUrl)) {
            Glide.with(getContext().getApplicationContext()).load(coverUrl).centerCrop().into(imageCover);
        }
        if (!Helper.isNullOrEmpty(thumbnailUrl)) {
            Glide.with(getContext().getApplicationContext()).load(thumbnailUrl).apply(RequestOptions.circleCropTransform()).into(imageThumbnail);
            noThumbnailView.setVisibility(View.GONE);
        } else {
            imageThumbnail.setVisibility(View.GONE);

            int bgColor = Helper.generateRandomColorForValue(claim.getClaimId());
            Helper.setIconViewBackgroundColor(noThumbnailView, bgColor, false, getContext());
            noThumbnailView.setVisibility(View.VISIBLE);
            textAlpha.setText(claim.getName().substring(1, 2));
        }

        tabPager.setAdapter(new ChannelPagerAdapter(claim, (MainActivity) getContext()));
        new TabLayoutMediator(tabLayout, tabPager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                tab.setText(position == 0 ? R.string.content : R.string.about);
            }
        }).attach();
    }

    private static class ChannelPagerAdapter extends FragmentStateAdapter {
        private Claim channelClaim;
        public ChannelPagerAdapter(Claim channelClaim, FragmentActivity activity) {
            super(activity);
            this.channelClaim = channelClaim;
        }

        @SneakyThrows
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    ChannelContentFragment contentFragment = ChannelContentFragment.class.newInstance();
                    contentFragment.setChannelId(channelClaim.getClaimId());
                    return contentFragment;

                case 1:
                    ChannelAboutFragment aboutFragment = ChannelAboutFragment.class.newInstance();
                    try {
                        Claim.ChannelMetadata metadata = (Claim.ChannelMetadata) channelClaim.getValue();
                        aboutFragment.setDescription(metadata.getDescription());
                        aboutFragment.setEmail(metadata.getEmail());
                        aboutFragment.setWebsite(metadata.getWebsiteUrl());
                    } catch (ClassCastException ex) {
                        // pass
                    }
                    return aboutFragment;
            }

            return null;
        }

        public long getItemId(int position) {
            return String.format("%s-%d", channelClaim.getClaimId(), position).hashCode();
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }
}
