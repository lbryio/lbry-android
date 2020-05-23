package io.lbry.browser.ui.findcontent;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.lbry.browser.MainActivity;
import io.lbry.browser.R;
import io.lbry.browser.adapter.EditorsChoiceItemAdapter;
import io.lbry.browser.model.Claim;
import io.lbry.browser.model.EditorsChoiceItem;
import io.lbry.browser.tasks.claim.ClaimSearchResultHandler;
import io.lbry.browser.tasks.claim.ClaimSearchTask;
import io.lbry.browser.ui.BaseFragment;
import io.lbry.browser.utils.Helper;
import io.lbry.browser.utils.Lbry;
import io.lbry.browser.utils.LbryAnalytics;

public class EditorsChoiceFragment extends BaseFragment {

    private static final HashMap<String, String> titleChannelIdsMap = new LinkedHashMap<>();
    static {
        titleChannelIdsMap.put("Short Films", "7056f8267188fc49cd3f7162b4115d9e3c8216f6");
        titleChannelIdsMap.put("Feature-Length Films", "7aad6f36f61da95cb02471fae55f736b28e3bca7");
        titleChannelIdsMap.put("Documentaries", "d57c606e11462e821d5596430c336b58716193bb");
        titleChannelIdsMap.put("Episodic Content", "ea5fc1bd3e1335776fe2641a539a47850606d7db");
    }

    private boolean contentLoading;
    private ProgressBar loading;
    private RecyclerView contentList;
    private EditorsChoiceItemAdapter contentListAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_editors_choice, container, false);

        loading = root.findViewById(R.id.editors_choice_loading);
        contentList = root.findViewById(R.id.editors_choice_content_list);

        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        contentList.setLayoutManager(llm);

        return root;
    }

    private Map<String, Object> buildContentOptions() {
        Context context = getContext();
        boolean canShowMatureContent = false;
        if (context != null) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
            canShowMatureContent = sp.getBoolean(MainActivity.PREFERENCE_KEY_SHOW_MATURE_CONTENT, false);
        }

        return Lbry.buildClaimSearchOptions(
                Claim.TYPE_REPOST,
                null,
                null, /*canShowMatureContent ? null : new ArrayList<>(Predefined.MATURE_TAGS),*/
                new ArrayList<>(titleChannelIdsMap.values()),
                null,
                Arrays.asList(Claim.ORDER_BY_RELEASE_TIME),
                null,
                1,
                99);
    }

    public void onResume() {
        super.onResume();
        Context context = getContext();
        Helper.setWunderbarValue(null, context);
        if (context instanceof MainActivity) {
            MainActivity activity = (MainActivity) context;
            LbryAnalytics.setCurrentScreen(activity, "Editor's Choice", "EditorsChoice");
        }

        if (contentListAdapter == null || contentListAdapter.getItemCount() == 0) {
            fetchClaimSearchContent();
        } else {
            if (contentList != null) {
                contentList.setAdapter(contentListAdapter);
            }
        }
    }

    public void fetchClaimSearchContent() {
        if (contentLoading) {
            return;
        }

        contentLoading = true;
        ClaimSearchTask task = new ClaimSearchTask(buildContentOptions(), Lbry.LBRY_TV_CONNECTION_STRING, loading, new ClaimSearchResultHandler() {
            @Override
            public void onSuccess(List<Claim> items, boolean hasReachedEnd) {
                List<EditorsChoiceItem> data = buildDataFromClaims(items);
                if (contentListAdapter == null) {
                    contentListAdapter = new EditorsChoiceItemAdapter(data, getContext());
                    contentListAdapter.setListener(new EditorsChoiceItemAdapter.EditorsChoiceItemListener() {
                        @Override
                        public void onEditorsChoiceItemClicked(EditorsChoiceItem item) {
                            String url = item.getPermanentUrl();
                            Context context = getContext();
                            if (context instanceof MainActivity) {
                                ((MainActivity) context).openFileUrl(url);
                            }
                        }
                    });
                } else {
                    contentListAdapter.addItems(data);
                }

                if (contentList != null && contentList.getAdapter() == null) {
                    contentList.setAdapter(contentListAdapter);
                }
                contentLoading = false;
            }

            @Override
            public void onError(Exception error) {
                contentLoading = false;
            }
        });
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private List<EditorsChoiceItem> buildDataFromClaims(List<Claim> claims) {
        List<EditorsChoiceItem> data = new ArrayList<>();
        for (String title : titleChannelIdsMap.keySet()) {
            EditorsChoiceItem titleItem = new EditorsChoiceItem();
            titleItem.setTitle(title);
            titleItem.setHeader(true);
            data.add(titleItem);

            String channelClaimId = titleChannelIdsMap.get(title);
            for (Claim c : claims) {
                if (c.getSigningChannel() != null && channelClaimId.equalsIgnoreCase(c.getSigningChannel().getClaimId())) {
                    EditorsChoiceItem item = EditorsChoiceItem.fromClaim(
                            Claim.TYPE_REPOST.equalsIgnoreCase(c.getValueType()) ? c.getRepostedClaim() : c);
                    data.add(item);
                }
            }
        }

        return data;
    }
}
