package io.lbry.browser.ui.search;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import io.lbry.browser.MainActivity;
import io.lbry.browser.R;
import io.lbry.browser.adapter.ClaimListAdapter;
import io.lbry.browser.model.Claim;
import io.lbry.browser.model.ClaimCacheKey;
import io.lbry.browser.tasks.ClaimSearchTask;
import io.lbry.browser.tasks.LighthouseSearchTask;
import io.lbry.browser.tasks.ResolveTask;
import io.lbry.browser.ui.BaseFragment;
import io.lbry.browser.utils.Helper;
import io.lbry.browser.utils.Lbry;
import io.lbry.browser.utils.LbryUri;
import lombok.Setter;

public class SearchFragment extends BaseFragment implements
        ClaimListAdapter.ClaimListItemListener, SharedPreferences.OnSharedPreferenceChangeListener {
    private ClaimListAdapter resultListAdapter;
    private static final int PAGE_SIZE = 25;

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
        PreferenceManager.getDefaultSharedPreferences(getContext()).registerOnSharedPreferenceChangeListener(this);
        if (!Helper.isNullOrEmpty(currentQuery)) {
            search(currentQuery, currentFrom);
        } else {
            noQueryView.setVisibility(View.VISIBLE);
            noResultsView.setVisibility(View.GONE);
        }
    }

    public void onPause() {
        PreferenceManager.getDefaultSharedPreferences(getContext()).unregisterOnSharedPreferenceChangeListener(this);
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
        return claim;
    }

    private String buildVanityUrl(String query) {
        LbryUri url = new LbryUri();
        url.setClaimName(query);
        return url.toString();
    }

    private void resolveFeaturedItem(String vanityUrl) {
        final ClaimCacheKey key = new ClaimCacheKey();
        key.setVanityUrl(vanityUrl);
        if (Lbry.claimCache.containsKey(key)) {
            Claim cachedClaim = Lbry.claimCache.get(key);
            updateFeaturedItemFromResolvedClaim(cachedClaim);
            return;
        }

        ResolveTask task = new ResolveTask(vanityUrl, Lbry.LBRY_TV_CONNECTION_STRING, null, new ResolveTask.ResolveResultHandler() {
            @Override
            public void onSuccess(List<Claim> claims) {
                if (claims.size() > 0) {
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

            // only set the values we need
            unresolved.setClaimId(resolved.getClaimId());
            unresolved.setName(resolved.getName());
            unresolved.setTimestamp(resolved.getTimestamp());
            unresolved.setValueType(resolved.getValueType());
            unresolved.setPermanentUrl(resolved.getPermanentUrl());
            unresolved.setValue(resolved.getValue());
            unresolved.setSigningChannel(resolved.getSigningChannel());
            unresolved.setUnresolved(false);

            resultListAdapter.notifyDataSetChanged();
        }
    }

    public void search(String query, int from) {
        boolean queryChanged = checkQuery(query);
        if (!queryChanged && from > 0) {
            currentFrom = from;
        }

        searchLoading = true;
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean canShowMatureContent = sp.getBoolean(MainActivity.PREFERENCE_KEY_SHOW_MATURE_CONTENT, false);
        LighthouseSearchTask task = new LighthouseSearchTask(
                currentQuery, PAGE_SIZE, currentFrom, canShowMatureContent, null, loadingView, new ClaimSearchTask.ClaimSearchResultHandler() {
            @Override
            public void onSuccess(List<Claim> claims, boolean hasReachedEnd) {
                contentHasReachedEnd = hasReachedEnd;
                searchLoading = false;

                if (resultListAdapter == null) {
                    resultListAdapter = new ClaimListAdapter(claims, getContext());
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
                noQueryView.setVisibility(View.GONE);
                noResultsView.setVisibility(itemCount == 0 ? View.VISIBLE : View.GONE);
                noResultsView.setText(getString(R.string.search_no_results, currentQuery));
            }

            @Override
            public void onError(Exception error) {
                int itemCount = resultListAdapter == null ? 0 : resultListAdapter.getItemCount();
                noQueryView.setVisibility(View.GONE);
                noResultsView.setVisibility(itemCount == 0 ? View.VISIBLE : View.GONE);
                noResultsView.setText(getString(R.string.search_no_results, currentQuery));
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

        if (claim.isUnresolved()) {
            // open the publish page
        } else if (claim.getName().startsWith("@")) {
            ((MainActivity) getContext()).openChannelUrl(claim.getPermanentUrl());
        } else {
            // not a channel
            MainActivity.openFileClaim(claim, getContext());
        }
    }

    public void onSharedPreferenceChanged(SharedPreferences sp, String key) {
        if (key.equalsIgnoreCase(MainActivity.PREFERENCE_KEY_SHOW_MATURE_CONTENT)) {
            search(currentQuery, currentFrom);
        }
    }
}
