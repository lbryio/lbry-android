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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.lbry.browser.MainActivity;
import io.lbry.browser.R;
import io.lbry.browser.adapter.ClaimListAdapter;
import io.lbry.browser.listener.DownloadActionListener;
import io.lbry.browser.model.Claim;
import io.lbry.browser.model.ClaimCacheKey;
import io.lbry.browser.model.LbryFile;
import io.lbry.browser.model.NavMenuItem;
import io.lbry.browser.tasks.claim.ClaimListResultHandler;
import io.lbry.browser.tasks.claim.ClaimSearchResultHandler;
import io.lbry.browser.tasks.LighthouseSearchTask;
import io.lbry.browser.tasks.claim.ResolveTask;
import io.lbry.browser.ui.BaseFragment;
import io.lbry.browser.ui.publish.PublishFragment;
import io.lbry.browser.utils.Helper;
import io.lbry.browser.utils.Lbry;
import io.lbry.browser.utils.LbryAnalytics;
import io.lbry.browser.utils.LbryUri;
import lombok.Setter;

public class SearchFragment extends BaseFragment implements
        ClaimListAdapter.ClaimListItemListener, DownloadActionListener, SharedPreferences.OnSharedPreferenceChangeListener {
    private static final int PAGE_SIZE = 25;

    private ClaimListAdapter resultListAdapter;
    private ProgressBar loadingView;
    private RecyclerView resultList;
    private TextView noQueryView;
    private TextView noResultsView;

    @Setter
    private String currentQuery;
    private boolean searchLoading;
    private boolean contentHasReachedEnd;
    private int currentFrom;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_search, container, false);

        loadingView = root.findViewById(R.id.search_loading);
        noQueryView = root.findViewById(R.id.search_no_query);
        noResultsView = root.findViewById(R.id.search_no_results);

        resultList = root.findViewById(R.id.search_result_list);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        resultList.setLayoutManager(llm);
        resultList.setAdapter(resultListAdapter);
        resultList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (searchLoading) {
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
                            int newFrom = currentFrom + PAGE_SIZE;
                            search(currentQuery, newFrom);
                        }
                    }
                }
            }
        });

        return root;
    }

    public void onResume() {
        super.onResume();
        Context context = getContext();
        Helper.setWunderbarValue(currentQuery, context);
        PreferenceManager.getDefaultSharedPreferences(context).registerOnSharedPreferenceChangeListener(this);
        if (context instanceof MainActivity) {
            MainActivity activity = (MainActivity) context;
            LbryAnalytics.setCurrentScreen(activity, "Search", "Search");
            activity.addDownloadActionListener(this);
        }
        if (!Helper.isNullOrEmpty(currentQuery)) {
            logSearch(currentQuery);
            search(currentQuery, currentFrom);
        } else {
            noQueryView.setVisibility(View.VISIBLE);
            noResultsView.setVisibility(View.GONE);
        }
    }

    public void onPause() {
        Context context = getContext();
        if (context != null) {
            ((MainActivity) context).removeDownloadActionListener(this);
        }
        PreferenceManager.getDefaultSharedPreferences(context).unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    private boolean checkQuery(String query) {
        if (!Helper.isNullOrEmpty(query) && !query.equalsIgnoreCase(currentQuery)) {
            // new query, reset values
            currentFrom = 0;
            currentQuery = query;
            if (resultListAdapter != null) {
                resultListAdapter.clearItems();
            }
            return true;
        }

        return false;
    }

    private Claim buildFeaturedItem(String query) {
        Claim claim = new Claim();
        claim.setName(query);
        claim.setFeatured(true);
        claim.setUnresolved(true);
        claim.setConfirmations(1);
        return claim;
    }

    private String buildVanityUrl(String query) {
        LbryUri url = new LbryUri();
        url.setClaimName(query);
        return url.toString();
    }

    private void resolveFeaturedItem(String vanityUrl) {
        final ClaimCacheKey key = new ClaimCacheKey();
        key.setUrl(vanityUrl);
        if (Lbry.claimCache.containsKey(key)) {
            Claim cachedClaim = Lbry.claimCache.get(key);
            updateFeaturedItemFromResolvedClaim(cachedClaim);
            return;
        }

        ResolveTask task = new ResolveTask(vanityUrl, Lbry.LBRY_TV_CONNECTION_STRING, null, new ClaimListResultHandler() {
            @Override
            public void onSuccess(List<Claim> claims) {
                if (claims.size() > 0 && !Helper.isNullOrEmpty(claims.get(0).getClaimId())) {
                    Claim resolved = claims.get(0);
                    Lbry.claimCache.put(key, resolved);
                    updateFeaturedItemFromResolvedClaim(resolved);
                }
            }

            @Override
            public void onError(Exception error) {

            }
        });
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void updateFeaturedItemFromResolvedClaim(Claim resolved) {
        if (resultListAdapter != null) {
            Claim unresolved = resultListAdapter.getFeaturedItem();

            Context context = getContext();
            boolean canShowMatureContent = false;
            if (context != null) {
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
                canShowMatureContent = sp.getBoolean(MainActivity.PREFERENCE_KEY_SHOW_MATURE_CONTENT, false);
            }
            if (resolved.isMature() && !canShowMatureContent) {
                resultListAdapter.removeFeaturedItem();
            } else {
                // only set the values we need
                unresolved.setClaimId(resolved.getClaimId());
                unresolved.setName(resolved.getName());
                unresolved.setTimestamp(resolved.getTimestamp());
                unresolved.setValueType(resolved.getValueType());
                unresolved.setPermanentUrl(resolved.getPermanentUrl());
                unresolved.setValue(resolved.getValue());
                unresolved.setSigningChannel(resolved.getSigningChannel());
                unresolved.setUnresolved(false);
                unresolved.setConfirmations(resolved.getConfirmations());
            }

            resultListAdapter.notifyDataSetChanged();
        }
    }

    private void logSearch(String query) {
        Bundle bundle = new Bundle();
        bundle.putString("query", query);
        LbryAnalytics.logEvent(LbryAnalytics.EVENT_SEARCH, bundle);
    }

    public void search(String query, int from) {
        boolean queryChanged = checkQuery(query);
        if (!queryChanged && from > 0) {
            currentFrom = from;
        }

        if (queryChanged) {
            logSearch(query);
        }

        searchLoading = true;
        Context context = getContext();
        boolean canShowMatureContent = false;
        if (context != null) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
            canShowMatureContent = sp.getBoolean(MainActivity.PREFERENCE_KEY_SHOW_MATURE_CONTENT, false);
        }

        LighthouseSearchTask task = new LighthouseSearchTask(
                currentQuery, PAGE_SIZE, currentFrom, canShowMatureContent, null, loadingView, new ClaimSearchResultHandler() {
            @Override
            public void onSuccess(List<Claim> claims, boolean hasReachedEnd) {
                contentHasReachedEnd = hasReachedEnd;
                searchLoading = false;
                Context context = getContext();
                if (context != null) {
                    if (resultListAdapter == null) {
                        resultListAdapter = new ClaimListAdapter(claims, context);
                        resultListAdapter.addFeaturedItem(buildFeaturedItem(query));
                        resolveFeaturedItem(buildVanityUrl(query));
                        resultListAdapter.setListener(SearchFragment.this);
                        if (resultList != null) {
                            resultList.setAdapter(resultListAdapter);
                        }
                    } else {
                        resultListAdapter.addItems(claims);
                    }

                    int itemCount = resultListAdapter.getItemCount();
                    Helper.setViewVisibility(noQueryView, View.GONE);
                    Helper.setViewVisibility(noResultsView, itemCount == 0 ? View.VISIBLE : View.GONE);
                    Helper.setViewText(noResultsView, getString(R.string.search_no_results, currentQuery));
                }
            }

            @Override
            public void onError(Exception error) {
                Context context = getContext();
                int itemCount = resultListAdapter == null ? 0 : resultListAdapter.getItemCount();
                Helper.setViewVisibility(noQueryView, View.GONE);
                Helper.setViewVisibility(noResultsView, itemCount == 0 ? View.VISIBLE : View.GONE);
                if (context != null) {
                    Helper.setViewText(noResultsView, getString(R.string.search_no_results, currentQuery));
                }
                searchLoading = false;
            }
        });
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void onClaimClicked(Claim claim) {
        if (Helper.isNullOrEmpty(claim.getName())) {
            // never should happen, but if it does, do nothing
            return;
        }

        Context context = getContext();
        if (context instanceof MainActivity) {
            MainActivity activity = (MainActivity) context;
            if (claim.isUnresolved()) {
                // open the publish page
                Map<String, Object> params = new HashMap<>();
                params.put("suggestedUrl", claim.getName());
                activity.openFragment(PublishFragment.class, true, NavMenuItem.ID_ITEM_NEW_PUBLISH, params);
            } else if (claim.getName().startsWith("@")) {
                activity.openChannelUrl(claim.getPermanentUrl());
            } else {
                // not a channel
                activity.openFileClaim(claim);
            }
        }
    }

    public void onSharedPreferenceChanged(SharedPreferences sp, String key) {
        if (key.equalsIgnoreCase(MainActivity.PREFERENCE_KEY_SHOW_MATURE_CONTENT)) {
            search(currentQuery, currentFrom);
        }
    }

    public void onDownloadAction(String downloadAction, String uri, String outpoint, String fileInfoJson, double progress) {
        if ("abort".equals(downloadAction)) {
            if (resultListAdapter != null) {
                resultListAdapter.clearFileForClaimOrUrl(outpoint, uri);
            }
            return;
        }

        try {
            JSONObject fileInfo = new JSONObject(fileInfoJson);
            LbryFile claimFile = LbryFile.fromJSONObject(fileInfo);
            String claimId = claimFile.getClaimId();
            if (resultListAdapter != null) {
                resultListAdapter.updateFileForClaimByIdOrUrl(claimFile, claimId, uri);
            }
        } catch (JSONException ex) {
            // invalid file info for download
        }
    }
}
