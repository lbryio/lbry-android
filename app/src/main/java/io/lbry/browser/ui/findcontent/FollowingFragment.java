package io.lbry.browser.ui.findcontent;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import io.lbry.browser.MainActivity;
import io.lbry.browser.R;
import io.lbry.browser.adapter.ChannelFilterListAdapter;
import io.lbry.browser.adapter.ClaimListAdapter;
import io.lbry.browser.adapter.SuggestedChannelGridAdapter;
import io.lbry.browser.dialog.ContentFromDialogFragment;
import io.lbry.browser.dialog.ContentSortDialogFragment;
import io.lbry.browser.dialog.DiscoverDialogFragment;
import io.lbry.browser.exceptions.LbryUriException;
import io.lbry.browser.listener.DownloadActionListener;
import io.lbry.browser.model.Claim;
import io.lbry.browser.model.LbryFile;
import io.lbry.browser.model.lbryinc.Subscription;
import io.lbry.browser.tasks.claim.ClaimSearchResultHandler;
import io.lbry.browser.tasks.lbryinc.ChannelSubscribeTask;
import io.lbry.browser.tasks.claim.ClaimListResultHandler;
import io.lbry.browser.tasks.claim.ClaimSearchTask;
import io.lbry.browser.tasks.lbryinc.FetchSubscriptionsTask;
import io.lbry.browser.tasks.claim.ResolveTask;
import io.lbry.browser.listener.ChannelItemSelectionListener;
import io.lbry.browser.ui.BaseFragment;
import io.lbry.browser.utils.Helper;
import io.lbry.browser.utils.Lbry;
import io.lbry.browser.utils.LbryAnalytics;
import io.lbry.browser.utils.LbryUri;
import io.lbry.browser.utils.Lbryio;
import io.lbry.browser.utils.Predefined;

public class FollowingFragment extends BaseFragment implements
        FetchSubscriptionsTask.FetchSubscriptionsHandler,
        ChannelItemSelectionListener,
        DownloadActionListener,
        SharedPreferences.OnSharedPreferenceChangeListener {

    public static boolean resetClaimSearchContent;
    private static final int SUGGESTED_PAGE_SIZE = 45;
    private static final int MIN_SUGGESTED_SUBSCRIBE_COUNT = 5;

    private DiscoverDialogFragment discoverDialog;
    private List<String> excludeChannelIdsForDiscover;
    private MaterialButton suggestedDoneButton;
    private TextView titleView;
    private TextView infoView;
    private RecyclerView horizontalChannelList;
    private RecyclerView suggestedChannelGrid;
    private RecyclerView contentList;
    private ProgressBar bigContentLoading;
    private ProgressBar contentLoading;
    private ProgressBar channelListLoading;
    private View layoutSortContainer;
    private View sortLink;
    private TextView sortLinkText;
    private View contentFromLink;
    private TextView contentFromLinkText;
    private View discoverLink;
    private int currentSortBy;
    private int currentContentFrom;
    private String contentReleaseTime;
    private List<String> contentSortOrder;
    private boolean contentClaimSearchLoading = false;
    private boolean suggestedClaimSearchLoading = false;
    private View noContentView;
    private boolean subscriptionsShown;

    private List<Integer> queuedContentPages = new ArrayList<>();
    private List<Integer> queuedSuggestedPages = new ArrayList<>();

    private int currentSuggestedPage = 0;
    private int currentClaimSearchPage;
    private boolean suggestedHasReachedEnd;
    private boolean contentHasReachedEnd;
    private boolean contentPendingFetch = false;
    private int numSuggestedSelected;

    // adapters
    private SuggestedChannelGridAdapter suggestedChannelAdapter;
    private ChannelFilterListAdapter channelFilterListAdapter;
    private ClaimListAdapter contentListAdapter;

    private List<String> channelIds;
    private List<String> channelUrls;
    private List<Subscription> subscriptionsList;
    private List<Claim> suggestedChannels;
    private ClaimSearchTask suggestedChannelClaimSearchTask;
    private ClaimSearchTask contentClaimSearchTask;
    private boolean loadingSuggested;
    private boolean loadingContent;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_following, container, false);

        // Following page is sorted by new by default, past week if sort is top
        currentSortBy = ContentSortDialogFragment.ITEM_SORT_BY_NEW;
        currentContentFrom = ContentFromDialogFragment.ITEM_FROM_PAST_WEEK;

        titleView = root.findViewById(R.id.following_page_title);
        infoView = root.findViewById(R.id.following_page_info);
        horizontalChannelList = root.findViewById(R.id.following_channel_list);
        layoutSortContainer = root.findViewById(R.id.following_filter_container);
        sortLink = root.findViewById(R.id.following_sort_link);
        sortLinkText = root.findViewById(R.id.following_sort_link_text);
        contentFromLink = root.findViewById(R.id.following_time_link);
        contentFromLinkText = root.findViewById(R.id.following_time_link_text);
        suggestedChannelGrid = root.findViewById(R.id.following_suggested_grid);
        suggestedDoneButton = root.findViewById(R.id.following_suggested_done_button);
        contentList = root.findViewById(R.id.following_content_list);
        bigContentLoading = root.findViewById(R.id.following_main_progress);
        contentLoading = root.findViewById(R.id.following_content_progress);
        channelListLoading = root.findViewById(R.id.following_channel_load_progress);
        discoverLink = root.findViewById(R.id.following_discover_link);
        noContentView = root.findViewById(R.id.following_no_claim_search_content);

        Context context = getContext();
        GridLayoutManager glm = new GridLayoutManager(context, 3);
        suggestedChannelGrid.setLayoutManager(glm);
        suggestedChannelGrid.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (suggestedClaimSearchLoading) {
                    return;
                }

                GridLayoutManager lm = (GridLayoutManager) recyclerView.getLayoutManager();
                if (lm != null) {
                    int visibleItemCount = lm.getChildCount();
                    int totalItemCount = lm.getItemCount();
                    int pastVisibleItems = lm.findFirstVisibleItemPosition();
                    if (pastVisibleItems + visibleItemCount >= totalItemCount) {
                        if (!suggestedHasReachedEnd) {
                            // load more
                            currentSuggestedPage++;
                            fetchSuggestedChannels();
                        }
                    }
                }
            }
        });

        LinearLayoutManager cllm = new LinearLayoutManager(context, RecyclerView.HORIZONTAL, false);
        horizontalChannelList.setLayoutManager(cllm);

        LinearLayoutManager llm = new LinearLayoutManager(context);
        contentList.setLayoutManager(llm);
        contentList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (contentClaimSearchLoading) {
                    return;
                }
                LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (lm != null) {
                    int visibleItemCount = lm.getChildCount();
                    int totalItemCount = lm.getItemCount();
                    int pastVisibleItems = lm.findFirstVisibleItemPosition();
                    if (pastVisibleItems + visibleItemCount >= totalItemCount) {
                        if (!contentHasReachedEnd) {
                            // load more
                            currentClaimSearchPage++;
                            fetchClaimSearchContent();
                        }
                    }
                }
            }
        });

        suggestedDoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int selected = suggestedChannelAdapter == null ? 0 : suggestedChannelAdapter.getSelectedCount();
                int remaining = MIN_SUGGESTED_SUBSCRIBE_COUNT - selected;
                if (remaining == MIN_SUGGESTED_SUBSCRIBE_COUNT) {
                    Snackbar.make(getView(), R.string.select_five_subscriptions, Snackbar.LENGTH_LONG).show();
                } else {
                    fetchSubscriptions();
                    showSubscribedContent();
                    fetchAndResolveChannelList();
                }
            }
        });

        sortLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ContentSortDialogFragment dialog = ContentSortDialogFragment.newInstance();
                dialog.setCurrentSortByItem(currentSortBy);
                dialog.setSortByListener(new ContentSortDialogFragment.SortByListener() {
                    @Override
                    public void onSortByItemSelected(int sortBy) {
                        onSortByChanged(sortBy);
                    }
                });

                Context context = getContext();
                if (context instanceof MainActivity) {
                    MainActivity activity = (MainActivity) context;
                    dialog.show(activity.getSupportFragmentManager(), ContentSortDialogFragment.TAG);
                }
            }
        });
        contentFromLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ContentFromDialogFragment dialog = ContentFromDialogFragment.newInstance();
                dialog.setCurrentFromItem(currentContentFrom);
                dialog.setContentFromListener(new ContentFromDialogFragment.ContentFromListener() {
                    @Override
                    public void onContentFromItemSelected(int contentFromItem) {
                        onContentFromChanged(contentFromItem);
                    }
                });
                Context context = getContext();
                if (context instanceof MainActivity) {
                    MainActivity activity = (MainActivity) context;
                    dialog.show(activity.getSupportFragmentManager(), ContentFromDialogFragment.TAG);
                }
            }
        });
        discoverLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Helper.setViewEnabled(discoverLink, false);
                buildChannelIdsAndUrls();
                currentSuggestedPage = 1;
                discoverDialog = DiscoverDialogFragment.newInstance();
                discoverDialog.setAdapter(suggestedChannelAdapter);
                discoverDialog.setDialogActionsListener(new DiscoverDialogFragment.DiscoverDialogListener() {
                    @Override
                    public void onScrollEndReached() {
                        if (suggestedClaimSearchLoading) {
                            return;
                        }
                        currentSuggestedPage++;
                        fetchSuggestedChannels();
                    }
                    @Override
                    public void onCancel() {
                        discoverDialog = null;
                        excludeChannelIdsForDiscover = null;
                        if (suggestedChannelAdapter != null) {
                            suggestedChannelAdapter.clearItems();
                        }
                        Helper.setViewEnabled(discoverLink, true);
                    }
                    @Override
                    public void onResume() {
                        if (suggestedChannelAdapter == null || suggestedChannelAdapter.getItemCount() == 0) {
                            discoverDialog.setLoading(true);
                            fetchSuggestedChannels();
                        }
                    }
                });

                Context context = getContext();
                if (context instanceof MainActivity) {
                    MainActivity activity = (MainActivity) context;
                    discoverDialog.show(activity.getSupportFragmentManager(), DiscoverDialogFragment.TAG);
                }
            }
        });

        return root;
    }

    private void onContentFromChanged(int contentFrom) {
        currentContentFrom = contentFrom;

        // rebuild options and search
        updateContentFromLinkText();
        contentReleaseTime = Helper.buildReleaseTime(currentContentFrom);
        fetchClaimSearchContent(true);
    }

    private void onSortByChanged(int sortBy) {
        currentSortBy = sortBy;

        // rebuild options and search
        Helper.setViewVisibility(contentFromLink, currentSortBy == ContentSortDialogFragment.ITEM_SORT_BY_TOP ? View.VISIBLE : View.GONE);
        currentContentFrom = currentSortBy == ContentSortDialogFragment.ITEM_SORT_BY_TOP ?
                (currentContentFrom == 0 ? ContentFromDialogFragment.ITEM_FROM_PAST_WEEK : currentContentFrom) : 0;

        updateSortByLinkText();
        contentSortOrder = Helper.buildContentSortOrder(currentSortBy);
        contentReleaseTime = Helper.buildReleaseTime(currentContentFrom);
        fetchClaimSearchContent(true);
    }

    private void updateSortByLinkText() {
        int stringResourceId = -1;
        switch (currentSortBy) {
            case ContentSortDialogFragment.ITEM_SORT_BY_NEW: default: stringResourceId = R.string.new_text; break;
            case ContentSortDialogFragment.ITEM_SORT_BY_TOP: stringResourceId = R.string.top; break;
            case ContentSortDialogFragment.ITEM_SORT_BY_TRENDING: stringResourceId = R.string.trending; break;
        }

        Helper.setViewText(sortLinkText, stringResourceId);
    }

    private void updateContentFromLinkText() {
        int stringResourceId = -1;
        switch (currentContentFrom) {
            case ContentFromDialogFragment.ITEM_FROM_PAST_24_HOURS: stringResourceId = R.string.past_24_hours; break;
            case ContentFromDialogFragment.ITEM_FROM_PAST_WEEK: default: stringResourceId = R.string.past_week; break;
            case ContentFromDialogFragment.ITEM_FROM_PAST_MONTH: stringResourceId = R.string.past_month; break;
            case ContentFromDialogFragment.ITEM_FROM_PAST_YEAR: stringResourceId = R.string.past_year; break;
            case ContentFromDialogFragment.ITEM_FROM_ALL_TIME: stringResourceId = R.string.all_time; break;
        }

        Helper.setViewText(contentFromLinkText, stringResourceId);
    }

    public void onResume() {
        super.onResume();
        Context context = getContext();
        Helper.setWunderbarValue(null, context);
        PreferenceManager.getDefaultSharedPreferences(context).registerOnSharedPreferenceChangeListener(this);
        if (context instanceof MainActivity) {
            MainActivity activity = (MainActivity) context;
            LbryAnalytics.setCurrentScreen(activity, "Subscriptions", "Subscriptions");
            activity.addDownloadActionListener(this);
        }

        // check if subscriptions exist
        if (suggestedChannelAdapter != null) {
            showSuggestedChannels();
            if (suggestedChannelGrid != null) {
                suggestedChannelGrid.setAdapter(suggestedChannelAdapter);
            }
        }

        if (Lbryio.subscriptions != null && Lbryio.subscriptions.size() > 0) {
            fetchLoadedSubscriptions(true);
        } else {
            fetchSubscriptions();
        }
    }
    public void onPause() {
        Context context = getContext();
        if (context instanceof MainActivity) {
            ((MainActivity) context).removeDownloadActionListener(this);
        }
        PreferenceManager.getDefaultSharedPreferences(context).unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }
    public void fetchLoadedSubscriptions(boolean showSubscribed) {
        subscriptionsList = new ArrayList<>(Lbryio.subscriptions);
        buildChannelIdsAndUrls();
        if (Lbryio.cacheResolvedSubscriptions.size() > 0) {
            updateChannelFilterListAdapter(Lbryio.cacheResolvedSubscriptions, resetClaimSearchContent);
        } else {
            fetchAndResolveChannelList();
        }

        fetchClaimSearchContent(resetClaimSearchContent);
        resetClaimSearchContent = false;
        if (showSubscribed && subscriptionsList.size() > 0) {
            showSubscribedContent();
        }
    }

    public void loadFollowing() {
        // wrapper to just re-fetch subscriptions (upon user sign in, for example)
        fetchSubscriptions();
    }

    private void fetchSubscriptions() {
        FetchSubscriptionsTask task = new FetchSubscriptionsTask(getContext(), channelListLoading, this);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private Map<String, Object> buildSuggestedOptions() {
        Context context = getContext();
        boolean canShowMatureContent = false;
        if (context != null) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
            canShowMatureContent = sp.getBoolean(MainActivity.PREFERENCE_KEY_SHOW_MATURE_CONTENT, false);
        }

        return Lbry.buildClaimSearchOptions(
                Claim.TYPE_CHANNEL,
                null,
                canShowMatureContent ? null : new ArrayList<>(Predefined.MATURE_TAGS),
                null,
                excludeChannelIdsForDiscover,
                Arrays.asList(Claim.ORDER_BY_EFFECTIVE_AMOUNT),
                null,
                currentSuggestedPage == 0 ? 1 : currentSuggestedPage,
                SUGGESTED_PAGE_SIZE);
    }

    private Map<String, Object> buildContentOptions() {
        Context context = getContext();
        boolean canShowMatureContent = false;
        if (context != null) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
            canShowMatureContent = sp.getBoolean(MainActivity.PREFERENCE_KEY_SHOW_MATURE_CONTENT, false);
        }

        return Lbry.buildClaimSearchOptions(
                Arrays.asList(Claim.TYPE_STREAM, Claim.TYPE_REPOST),
                null,
                canShowMatureContent ? null : new ArrayList<>(Predefined.MATURE_TAGS),
                getChannelIds(),
                null,
                getContentSortOrder(),
                contentReleaseTime,
                currentClaimSearchPage == 0 ? 1 : currentClaimSearchPage,
                Helper.CONTENT_PAGE_SIZE);
    }

    private List<String> getChannelIds() {
        if (channelFilterListAdapter != null) {
            Claim selected = channelFilterListAdapter.getSelectedItem();
            if (selected != null) {
                return Arrays.asList(selected.getClaimId());
            }
        }

        return channelIds;
    }

    private List<String> getContentSortOrder() {
        if (contentSortOrder == null) {
            return Arrays.asList(Claim.ORDER_BY_RELEASE_TIME);
        }
        return contentSortOrder;
    }

    private void showSuggestedChannels() {
        Helper.setViewText(titleView, R.string.find_channels_to_follow);

        Helper.setViewVisibility(horizontalChannelList, View.GONE);
        Helper.setViewVisibility(contentList, View.GONE);
        Helper.setViewVisibility(infoView, View.VISIBLE);
        Helper.setViewVisibility(layoutSortContainer, View.GONE);
        Helper.setViewVisibility(suggestedChannelGrid, View.VISIBLE);
        Helper.setViewVisibility(suggestedDoneButton, View.VISIBLE);

        updateSuggestedDoneButtonText();
    }

    private void showSubscribedContent() {
        subscriptionsShown = true;
        Helper.setViewText(titleView, R.string.channels_you_follow);

        Helper.setViewVisibility(horizontalChannelList, View.VISIBLE);
        Helper.setViewVisibility(contentList, View.VISIBLE);
        Helper.setViewVisibility(infoView, View.GONE);
        Helper.setViewVisibility(layoutSortContainer, View.VISIBLE);
        Helper.setViewVisibility(suggestedChannelGrid, View.GONE);
        Helper.setViewVisibility(suggestedDoneButton, View.GONE);
    }

    private void buildChannelIdsAndUrls() {
        channelIds = new ArrayList<>();
        channelUrls = new ArrayList<>();
        if (subscriptionsList != null) {
            for (Subscription subscription : subscriptionsList) {
                try {
                    String url = subscription.getUrl();
                    LbryUri uri = LbryUri.parse(url);
                    String claimId = uri.getClaimId();
                    channelIds.add(claimId);
                    channelUrls.add(url);
                } catch (LbryUriException ex) {
                    // pass
                }
            }
        }

        excludeChannelIdsForDiscover = channelIds != null ? new ArrayList<>(channelIds) : null;
    }

    private void fetchAndResolveChannelList() {
        buildChannelIdsAndUrls();
        if (channelIds.size() > 0) {
            ResolveTask resolveSubscribedTask = new ResolveTask(channelUrls, Lbry.LBRY_TV_CONNECTION_STRING, channelListLoading, new ClaimListResultHandler() {
                @Override
                public void onSuccess(List<Claim> claims) {
                    updateChannelFilterListAdapter(claims, true);
                    Lbryio.cacheResolvedSubscriptions = claims;
                }

                @Override
                public void onError(Exception error) {
                    fetchAndResolveChannelList();
                }
            });
            resolveSubscribedTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

            fetchClaimSearchContent();
        }
    }

    private View getLoadingView() {
        return (contentListAdapter == null || contentListAdapter.getItemCount() == 0) ? bigContentLoading : contentLoading;
    }

    private void updateChannelFilterListAdapter(List<Claim> resolvedSubs, boolean reset) {
        Context context = getContext();
        if (channelFilterListAdapter == null && context != null) {
            channelFilterListAdapter = new ChannelFilterListAdapter(context);
            channelFilterListAdapter.setListener(new ChannelItemSelectionListener() {
                @Override
                public void onChannelItemSelected(Claim claim) {
                    if (contentClaimSearchTask != null && contentClaimSearchTask.getStatus() != AsyncTask.Status.FINISHED) {
                        contentClaimSearchTask.cancel(true);
                    }
                    if (contentListAdapter != null) {
                        contentListAdapter.clearItems();
                    }
                    currentClaimSearchPage = 1;
                    contentClaimSearchLoading = false;
                    fetchClaimSearchContent();
                }

                @Override
                public void onChannelItemDeselected(Claim claim) {

                }

                @Override
                public void onChannelSelectionCleared() {
                    if (contentClaimSearchTask != null && contentClaimSearchTask.getStatus() != AsyncTask.Status.FINISHED) {
                        contentClaimSearchTask.cancel(true);
                    }
                    if (contentListAdapter != null) {
                        contentListAdapter.clearItems();
                    }
                    currentClaimSearchPage = 1;
                    contentClaimSearchLoading = false;
                    fetchClaimSearchContent();
                }
            });
        }

        if (channelFilterListAdapter != null) {
            if (horizontalChannelList != null && horizontalChannelList.getAdapter() == null) {
                horizontalChannelList.setAdapter(channelFilterListAdapter);
            }
            if (reset) {
                channelFilterListAdapter.clearClaims();
                channelFilterListAdapter.setSelectedItem(null);
            }
            channelFilterListAdapter.addClaims(resolvedSubs);
        }
    }

    private void fetchClaimSearchContent() {
        fetchClaimSearchContent(false);
    }

    private void fetchClaimSearchContent(boolean reset) {
        if (reset && contentListAdapter != null) {
            contentListAdapter.clearItems();
            currentClaimSearchPage = 1;
        }

        contentClaimSearchLoading = true;
        Helper.setViewVisibility(noContentView, View.GONE);
        Map<String, Object> claimSearchOptions = buildContentOptions();
        contentClaimSearchTask = new ClaimSearchTask(claimSearchOptions, Lbry.LBRY_TV_CONNECTION_STRING, getLoadingView(), new ClaimSearchResultHandler() {
            @Override
            public void onSuccess(List<Claim> claims, boolean hasReachedEnd) {
                if (contentListAdapter == null) {
                    contentListAdapter = new ClaimListAdapter(claims, getContext());
                    contentListAdapter.setListener(new ClaimListAdapter.ClaimListItemListener() {
                        @Override
                        public void onClaimClicked(Claim claim) {
                            Context context = getContext();
                            if (context instanceof MainActivity) {
                                MainActivity activity = (MainActivity) context;
                                if (claim.getName().startsWith("@")) {
                                    // channel claim
                                    activity.openChannelClaim(claim);
                                } else {
                                    activity.openFileClaim(claim);
                                }
                            }
                        }
                    });
                } else {
                    contentListAdapter.addItems(claims);
                }

                if (contentList != null && contentList.getAdapter() == null) {
                    contentList.setAdapter(contentListAdapter);
                }

                contentHasReachedEnd = hasReachedEnd;
                contentClaimSearchLoading = false;
                checkNoContent(false);
            }

            @Override
            public void onError(Exception error) {
                contentClaimSearchLoading = false;
                checkNoContent(false);
            }
        });
        contentClaimSearchTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void updateSuggestedDoneButtonText() {
        Context context = getContext();
        if (context != null) {
            int selected = suggestedChannelAdapter == null ? 0 : suggestedChannelAdapter.getSelectedCount();
            int remaining = MIN_SUGGESTED_SUBSCRIBE_COUNT - selected;
            String buttonText = remaining <= 0 ? getString(R.string.done) : getString(R.string.n_remaining, remaining);
            Helper.setViewText(suggestedDoneButton, buttonText);
        }
    }

    private void fetchSuggestedChannels() {
        if (suggestedClaimSearchLoading) {
            return;
        }

        suggestedClaimSearchLoading = true;
        if (discoverDialog != null) {
            discoverDialog.setLoading(true);
        }

        Helper.setViewVisibility(noContentView, View.GONE);
        suggestedChannelClaimSearchTask = new ClaimSearchTask(
                buildSuggestedOptions(),
                Lbry.LBRY_TV_CONNECTION_STRING,
                suggestedChannelAdapter == null || suggestedChannelAdapter.getItemCount() == 0 ? bigContentLoading : contentLoading,
                new ClaimSearchResultHandler() {
                    @Override
                    public void onSuccess(List<Claim> claims, boolean hasReachedEnd) {
                        suggestedHasReachedEnd = hasReachedEnd;
                        suggestedClaimSearchLoading = false;
                        if (discoverDialog != null) {
                            discoverDialog.setLoading(false);
                        }

                        if (suggestedChannelAdapter == null) {
                            suggestedChannelAdapter = new SuggestedChannelGridAdapter(claims, getContext());
                            suggestedChannelAdapter.setListener(FollowingFragment.this);
                            if (suggestedChannelGrid != null) {
                                suggestedChannelGrid.setAdapter(suggestedChannelAdapter);
                            }
                            if (discoverDialog != null) {
                                discoverDialog.setAdapter(suggestedChannelAdapter);
                            }
                        } else {
                            suggestedChannelAdapter.addClaims(claims);
                        }

                        if (discoverDialog == null || !discoverDialog.isVisible()) {
                            checkNoContent(true);
                        }
                    }

                    @Override
                    public void onError(Exception error) {
                        suggestedClaimSearchLoading = false;
                        if (discoverDialog != null) {
                            discoverDialog.setLoading(false);
                        }
                        if (discoverDialog == null || !discoverDialog.isVisible()) {
                            checkNoContent(true);
                        }
                    }
                });

        suggestedChannelClaimSearchTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    // handler methods
    public void onSuccess(List<Subscription> subscriptions) {
        if (subscriptions.size() == 0) {
            // fresh start
            // TODO: Only do this if there are no local subscriptions stored
            currentSuggestedPage = 1;
            buildSuggestedOptions();
            loadingSuggested = true;
            loadingContent = false;

            fetchSuggestedChannels();
            showSuggestedChannels();
        } else {
            Lbryio.subscriptions = subscriptions;
            subscriptionsList = new ArrayList<>(subscriptions);
            showSubscribedContent();
            fetchAndResolveChannelList();
        }
    }

    public void onError(Exception exception) {

    }

    public void onChannelItemSelected(Claim claim) {
        // subscribe
        Subscription subscription = Subscription.fromClaim(claim);
        String channelClaimId = claim.getClaimId();

        ChannelSubscribeTask task = new ChannelSubscribeTask(getContext(), channelClaimId, subscription, false, new ChannelSubscribeTask.ChannelSubscribeHandler() {
            @Override
            public void onSuccess() {
                Lbryio.addSubscription(subscription);
                Lbryio.addCachedResolvedSubscription(claim);
                resetClaimSearchContent = true;
                fetchLoadedSubscriptions(subscriptionsShown);

                saveSharedUserState();
            }

            @Override
            public void onError(Exception error) { }
        });
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        updateSuggestedDoneButtonText();
    }
    public void onChannelItemDeselected(Claim claim) {
        // unsubscribe
        Subscription subscription = Subscription.fromClaim(claim);
        String channelClaimId = claim.getClaimId();
        ChannelSubscribeTask task = new ChannelSubscribeTask(getContext(), channelClaimId, subscription, true, new ChannelSubscribeTask.ChannelSubscribeHandler() {
            @Override
            public void onSuccess() {
                Lbryio.removeSubscription(subscription);
                Lbryio.removeCachedResolvedSubscription(claim);
                resetClaimSearchContent = true;
                fetchLoadedSubscriptions(subscriptionsShown);

                saveSharedUserState();
            }

            @Override
            public void onError(Exception error) {

            }
        });
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        updateSuggestedDoneButtonText();
    }
    public void onChannelSelectionCleared() {

    }

    private void checkNoContent(boolean suggested) {
        RecyclerView.Adapter adapter = suggested ? suggestedChannelAdapter : contentListAdapter;
        boolean noContent = adapter == null || adapter.getItemCount() == 0;
        Helper.setViewVisibility(noContentView, noContent ? View.VISIBLE : View.GONE);
    }

    private void saveSharedUserState() {
        Context context = getContext();
        if (context instanceof MainActivity) {
            ((MainActivity) context).saveSharedUserState();
        }
    }

    public void onSharedPreferenceChanged(SharedPreferences sp, String key) {
        if (key.equalsIgnoreCase(MainActivity.PREFERENCE_KEY_SHOW_MATURE_CONTENT)) {
            fetchClaimSearchContent(true);
        }
    }

    public void onDownloadAction(String downloadAction, String uri, String outpoint, String fileInfoJson, double progress) {
        if ("abort".equals(downloadAction)) {
            if (contentListAdapter != null) {
                contentListAdapter.clearFileForClaimOrUrl(outpoint, uri);
            }
            return;
        }

        try {
            JSONObject fileInfo = new JSONObject(fileInfoJson);
            LbryFile claimFile = LbryFile.fromJSONObject(fileInfo);
            String claimId = claimFile.getClaimId();
            if (contentListAdapter != null) {
                contentListAdapter.updateFileForClaimByIdOrUrl(claimFile, claimId, uri);
            }
        } catch (JSONException ex) {
            // invalid file info for download
        }
    }
}
