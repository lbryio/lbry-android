package io.lbry.browser.ui.library;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ActionMode;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import io.lbry.browser.MainActivity;
import io.lbry.browser.R;
import io.lbry.browser.adapter.ClaimListAdapter;
import io.lbry.browser.data.DatabaseHelper;
import io.lbry.browser.listener.DownloadActionListener;
import io.lbry.browser.listener.SdkStatusListener;
import io.lbry.browser.listener.SelectionModeListener;
import io.lbry.browser.model.Claim;
import io.lbry.browser.model.LbryFile;
import io.lbry.browser.model.ViewHistory;
import io.lbry.browser.tasks.claim.ClaimListResultHandler;
import io.lbry.browser.tasks.claim.ClaimListTask;
import io.lbry.browser.tasks.claim.ClaimSearchResultHandler;
import io.lbry.browser.tasks.claim.PurchaseListTask;
import io.lbry.browser.tasks.claim.ResolveTask;
import io.lbry.browser.tasks.file.BulkDeleteFilesTask;
import io.lbry.browser.tasks.file.FileListTask;
import io.lbry.browser.tasks.localdata.FetchViewHistoryTask;
import io.lbry.browser.ui.BaseFragment;
import io.lbry.browser.utils.Helper;
import io.lbry.browser.utils.Lbry;
import io.lbry.browser.utils.LbryAnalytics;
import io.lbry.browser.utils.LbryUri;

public class LibraryFragment extends BaseFragment implements
        ActionMode.Callback, DownloadActionListener, SelectionModeListener, SdkStatusListener  {

    private static final int FILTER_DOWNLOADS = 1;
    private static final int FILTER_PURCHASES = 2;
    private static final int FILTER_HISTORY = 3;
    private static final int PAGE_SIZE = 50;

    private ActionMode actionMode;
    private int currentFilter;
    private List<LbryFile> currentFiles;
    private View layoutSdkInitializing;
    private RecyclerView contentList;
    private ClaimListAdapter contentListAdapter;
    private ProgressBar listLoading;
    private TextView linkFilterDownloads;
    private TextView linkFilterPurchases;
    private TextView linkFilterHistory;
    private View layoutListEmpty;
    private TextView textListEmpty;
    private int currentPage;
    private Date lastDate;
    private boolean listReachedEnd;
    private boolean contentListLoading;
    private boolean initialOwnClaimsFetched;

    private CardView cardStats;
    private TextView linkStats;
    private TextView linkHide;
    private View viewStatsDistribution;
    private View viewVideoStatsBar;
    private View viewAudioStatsBar;
    private View viewImageStatsBar;
    private View viewOtherStatsBar;
    private TextView textStatsTotalSize;
    private TextView textStatsTotalSizeUnits;
    private TextView textStatsVideoSize;
    private TextView textStatsAudioSize;
    private TextView textStatsImageSize;
    private TextView textStatsOtherSize;
    private View legendVideo;
    private View legendAudio;
    private View legendImage;
    private View legendOther;
    
    private long totalBytes;
    private long totalVideoBytes;
    private long totalAudioBytes;
    private long totalImageBytes;
    private long totalOtherBytes;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_library, container, false);

        layoutSdkInitializing = root.findViewById(R.id.container_library_sdk_initializing);

        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        contentList = root.findViewById(R.id.library_list);
        contentList.setLayoutManager(llm);

        listLoading = root.findViewById(R.id.library_list_loading);
        linkFilterDownloads = root.findViewById(R.id.library_filter_link_downloads);
        linkFilterPurchases = root.findViewById(R.id.library_filter_link_purchases);
        linkFilterHistory = root.findViewById(R.id.library_filter_link_history);

        layoutListEmpty = root.findViewById(R.id.library_empty_container);
        textListEmpty = root.findViewById(R.id.library_list_empty_text);

        currentFilter = FILTER_DOWNLOADS;
        linkFilterDownloads.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDownloads();
            }
        });
        linkFilterPurchases.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPurchases();
            }
        });
        linkFilterHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showHistory();
            }
        });
        contentList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (contentListLoading) {
                    return;
                }

                LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (lm != null) {
                    int visibleItemCount = lm.getChildCount();
                    int totalItemCount = lm.getItemCount();
                    int pastVisibleItems = lm.findFirstVisibleItemPosition();
                    if (pastVisibleItems + visibleItemCount >= totalItemCount) {
                        if (!listReachedEnd) {
                            // load more
                            if (currentFilter == FILTER_DOWNLOADS) {
                                currentPage++;
                                fetchDownloads();
                            } else if (currentFilter == FILTER_HISTORY) {
                                fetchHistory();
                            }
                        }
                    }
                }
            }
        });
        
        // stats
        linkStats = root.findViewById(R.id.library_show_stats);
        linkHide = root.findViewById(R.id.library_hide_stats);
        cardStats = root.findViewById(R.id.library_storage_stats_card);
        viewStatsDistribution = root.findViewById(R.id.library_storage_stat_distribution);
        viewVideoStatsBar = root.findViewById(R.id.library_storage_stat_video_bar);
        viewAudioStatsBar = root.findViewById(R.id.library_storage_stat_audio_bar);
        viewImageStatsBar = root.findViewById(R.id.library_storage_stat_image_bar);
        viewOtherStatsBar = root.findViewById(R.id.library_storage_stat_other_bar);
        textStatsTotalSize = root.findViewById(R.id.library_storage_stat_used);
        textStatsTotalSizeUnits = root.findViewById(R.id.library_storage_stat_unit);
        textStatsVideoSize = root.findViewById(R.id.library_storage_stat_video_size);
        textStatsAudioSize = root.findViewById(R.id.library_storage_stat_audio_size);
        textStatsImageSize = root.findViewById(R.id.library_storage_stat_image_size);
        textStatsOtherSize = root.findViewById(R.id.library_storage_stat_other_size);
        legendVideo = root.findViewById(R.id.library_storage_legend_video);
        legendAudio = root.findViewById(R.id.library_storage_legend_audio);
        legendImage = root.findViewById(R.id.library_storage_legend_image);
        legendOther = root.findViewById(R.id.library_storage_legend_other);

        linkStats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateStats();
                cardStats.setVisibility(View.VISIBLE);
                checkStatsLink();
            }
        });
        linkHide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cardStats.setVisibility(View.GONE);
                checkStatsLink();
            }
        });

        return root;
    }

    public void onResume() {
        super.onResume();

        Context context = getContext();
        Helper.setWunderbarValue(null, context);
        if (context instanceof MainActivity) {
            MainActivity activity = (MainActivity) context;
            LbryAnalytics.setCurrentScreen(activity, "Library", "Library");
            activity.addDownloadActionListener(this);
        }

        layoutSdkInitializing.setVisibility(
                !Lbry.SDK_READY && currentFilter == FILTER_DOWNLOADS ? View.VISIBLE : View.GONE);
        if (!Lbry.SDK_READY) {
            if (context instanceof MainActivity) {
                MainActivity activity = (MainActivity) context;
                activity.addSdkStatusListener(this);
            }
        } else {
            onSdkReady();
        }
    }

    public void onPause() {
        Context context = getContext();
        if (context instanceof MainActivity) {
            MainActivity activity = (MainActivity) context;
            activity.removeSdkStatusListener(this);
            activity.removeDownloadActionListener(this);
        }
        super.onPause();
    }

    public void onSdkReady() {
        layoutSdkInitializing.setVisibility(View.GONE);
        if (currentFilter == FILTER_DOWNLOADS) {
            showDownloads();
        } else if (currentFilter == FILTER_HISTORY) {
            showHistory();
        } else if (currentFilter == FILTER_PURCHASES) {
            showPurchases();
        }
    }

    private void showDownloads() {
        currentFilter = FILTER_DOWNLOADS;
        linkFilterDownloads.setTypeface(null, Typeface.BOLD);
        linkFilterPurchases.setTypeface(null, Typeface.NORMAL);
        linkFilterHistory.setTypeface(null, Typeface.NORMAL);
        if (contentListAdapter != null) {
            contentListAdapter.setHideFee(false);
            contentListAdapter.clearItems();
            contentListAdapter.setCanEnterSelectionMode(true);
        }
        listReachedEnd = false;

        checkStatsLink();
        layoutSdkInitializing.setVisibility(Lbry.SDK_READY ? View.GONE : View.VISIBLE);
        currentPage = 1;
        currentFiles = new ArrayList<>();
        if (Lbry.SDK_READY) {
            if (!initialOwnClaimsFetched) {
                fetchOwnClaimsAndShowDownloads();
            } else {
                fetchDownloads();
            }
        }
    }

    private void fetchOwnClaimsAndShowDownloads() {
        if (Lbry.ownClaims != null && Lbry.ownClaims.size() > 0) {
            initialOwnClaimsFetched = true;
            fetchDownloads();
            return;
        }

        linkStats.setVisibility(View.INVISIBLE);
        ClaimListTask task = new ClaimListTask(Arrays.asList(Claim.TYPE_STREAM, Claim.TYPE_REPOST), listLoading, new ClaimListResultHandler() {
            @Override
            public void onSuccess(List<Claim> claims) {
                Lbry.ownClaims = Helper.filterDeletedClaims(new ArrayList<>(claims));
                initialOwnClaimsFetched = true;
                if (currentFilter == FILTER_DOWNLOADS) {
                    fetchDownloads();
                }
                checkStatsLink();
            }

            @Override
            public void onError(Exception error) {
                initialOwnClaimsFetched = true;
                checkStatsLink();
            }
        });
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void showPurchases() {
        currentFilter = FILTER_PURCHASES;
        linkFilterDownloads.setTypeface(null, Typeface.NORMAL);
        linkFilterPurchases.setTypeface(null, Typeface.BOLD);
        linkFilterHistory.setTypeface(null, Typeface.NORMAL);
        if (contentListAdapter != null) {
            contentListAdapter.setHideFee(true);
            contentListAdapter.clearItems();
            contentListAdapter.setCanEnterSelectionMode(false);
        }
        listReachedEnd = false;

        cardStats.setVisibility(View.GONE);
        checkStatsLink();

        layoutSdkInitializing.setVisibility(Lbry.SDK_READY ? View.GONE : View.VISIBLE);
        currentPage = 1;
        if (Lbry.SDK_READY) {
            fetchPurchases();
        }
    }

    private void showHistory() {
        currentFilter = FILTER_HISTORY;
        linkFilterDownloads.setTypeface(null, Typeface.NORMAL);
        linkFilterPurchases.setTypeface(null, Typeface.NORMAL);
        linkFilterHistory.setTypeface(null, Typeface.BOLD);
        if (actionMode != null) {
            actionMode.finish();
        }
        if (contentListAdapter != null) {
            contentListAdapter.setHideFee(false);
            contentListAdapter.clearItems();
            contentListAdapter.setCanEnterSelectionMode(false);
        }
        listReachedEnd = false;

        cardStats.setVisibility(View.GONE);
        checkStatsLink();

        layoutSdkInitializing.setVisibility(View.GONE);
        lastDate = null;
        fetchHistory();
    }

    private void initContentListAdapter(List<Claim> claims) {
        contentListAdapter = new ClaimListAdapter(claims, getContext());
        contentListAdapter.setCanEnterSelectionMode(currentFilter == FILTER_DOWNLOADS);
        contentListAdapter.setSelectionModeListener(this);
        contentListAdapter.setHideFee(currentFilter != FILTER_PURCHASES);
        contentListAdapter.setListener(new ClaimListAdapter.ClaimListItemListener() {
            @Override
            public void onClaimClicked(Claim claim) {
                Context context = getContext();
                if (context instanceof MainActivity) {
                    MainActivity activity = (MainActivity) getContext();
                    if (claim.getName().startsWith("@")) {
                        activity.openChannelUrl(claim.getPermanentUrl());
                    } else {
                        activity.openFileUrl(claim.getPermanentUrl());
                    }
                }
            }
        });
    }

    private void fetchDownloads() {
        contentListLoading  = true;
        Helper.setViewVisibility(linkStats, View.GONE);
        Helper.setViewVisibility(layoutListEmpty, View.GONE);
        FileListTask task = new FileListTask(currentPage, PAGE_SIZE, true, listLoading, new FileListTask.FileListResultHandler() {
            @Override
            public void onSuccess(List<LbryFile> files, boolean hasReachedEnd) {
                listReachedEnd = hasReachedEnd;
                List<LbryFile> filteredFiles = Helper.filterDownloads(files);
                List<Claim> claims = Helper.claimsFromFiles(filteredFiles);
                addFiles(filteredFiles);
                updateStats();
                checkStatsLink();

                if (contentListAdapter == null) {
                    initContentListAdapter(claims);
                } else {
                    contentListAdapter.addItems(claims);
                }
                if (contentList.getAdapter() == null) {
                    contentList.setAdapter(contentListAdapter);
                }
                resolveMissingChannelNames(buildUrlsToResolve(claims));
                checkListEmpty();
                contentListLoading = false;
            }

            @Override
            public void onError(Exception error) {
                // pass
                checkStatsLink();
                checkListEmpty();
                contentListLoading = false;
            }
        });
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void fetchPurchases() {
        contentListLoading  = true;
        Helper.setViewVisibility(linkStats, View.GONE);
        Helper.setViewVisibility(layoutListEmpty, View.GONE);
        PurchaseListTask task = new PurchaseListTask(currentPage, PAGE_SIZE, listLoading, new ClaimSearchResultHandler() {
            @Override
            public void onSuccess(List<Claim> claims, boolean hasReachedEnd) {
                listReachedEnd = hasReachedEnd;
                if (contentListAdapter == null) {
                    initContentListAdapter(claims);
                } else {
                    contentListAdapter.addItems(claims);
                }
                if (contentList.getAdapter() == null) {
                    contentList.setAdapter(contentListAdapter);
                }
                checkListEmpty();
                contentListLoading = false;
            }

            @Override
            public void onError(Exception error) {
                checkStatsLink();
                checkListEmpty();
                contentListLoading = false;
            }
        });
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void fetchHistory() {
        contentListLoading = true;
        Helper.setViewVisibility(layoutListEmpty, View.GONE);
        DatabaseHelper dbHelper = DatabaseHelper.getInstance();
        if (dbHelper != null) {
            FetchViewHistoryTask task = new FetchViewHistoryTask(lastDate, PAGE_SIZE, dbHelper, new FetchViewHistoryTask.FetchViewHistoryHandler() {
                @Override
                public void onSuccess(List<ViewHistory> history, boolean hasReachedEnd) {
                    listReachedEnd = hasReachedEnd;
                    if (history.size() > 0) {
                        lastDate = history.get(history.size() - 1).getTimestamp();
                    }

                    List<Claim> claims = Helper.claimsFromViewHistory(history);
                    if (contentListAdapter == null) {
                        initContentListAdapter(claims);
                    } else {
                        contentListAdapter.addItems(claims);
                    }
                    if (contentList.getAdapter() == null) {
                        contentList.setAdapter(contentListAdapter);
                    }
                    checkListEmpty();
                    contentListLoading = false;
                }
            });
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            checkListEmpty();
            contentListLoading = false;
        }
    }

    public void onDownloadAction(String downloadAction, String uri, String outpoint, String fileInfoJson, double progress) {
        if ("abort".equals(downloadAction)) {
            if (contentListAdapter != null) {
                contentListAdapter.clearFileForClaimOrUrl(outpoint, uri, currentFilter == FILTER_DOWNLOADS);
            }
            return;
        }

        try {
            JSONObject fileInfo = new JSONObject(fileInfoJson);
            LbryFile claimFile = LbryFile.fromJSONObject(fileInfo);
            String claimId = claimFile.getClaimId();
            if (contentListAdapter != null) {
                contentListAdapter.updateFileForClaimByIdOrUrl(claimFile, claimId, uri, true);
            }
        } catch (JSONException ex) {
            // invalid file info for download
        }
    }

    private void checkListEmpty() {
        layoutListEmpty.setVisibility(contentListAdapter == null || contentListAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
        int stringResourceId;
        switch (currentFilter) {
            case FILTER_DOWNLOADS: default: stringResourceId = R.string.library_no_downloads; break;
            case FILTER_HISTORY: stringResourceId = R.string.library_no_history; break;
            case FILTER_PURCHASES: stringResourceId = R.string.library_no_purchases; break;
        }
        textListEmpty.setText(stringResourceId);
    }

    private void addFiles(List<LbryFile> files) {
        if (currentFiles == null) {
            currentFiles = new ArrayList<>();
        }
        for  (LbryFile file : files) {
            if (!currentFiles.contains(file)) {
                currentFiles.add(file);
            }
        }
    }

    private void updateStats() {
        totalBytes = 0;
        totalVideoBytes = 0;
        totalAudioBytes = 0;
        totalImageBytes = 0;
        totalOtherBytes = 0;
        if (currentFiles != null) {
            for (LbryFile file : currentFiles) {
                long writtenBytes = file.getWrittenBytes();
                String mime = file.getMimeType();
                if (mime != null) {
                    if (mime.startsWith("video/")) {
                        totalVideoBytes += writtenBytes;
                    } else if (mime.startsWith("audio/")) {
                        totalAudioBytes += writtenBytes;
                    } else if (mime.startsWith("image/")) {
                        totalImageBytes += writtenBytes;
                    } else {
                        totalOtherBytes += writtenBytes;
                    }
                }

                totalBytes += writtenBytes;
            }
        }

        renderStats();
    }

    private void renderStats() {
        String[] totalSizeParts = Helper.formatBytesParts(totalBytes, false);
        textStatsTotalSize.setText(totalSizeParts[0]);
        textStatsTotalSizeUnits.setText(totalSizeParts[1]);

        viewStatsDistribution.setVisibility(totalBytes > 0 ? View.VISIBLE : View.GONE);

        int percentVideo = normalizePercent((double) totalVideoBytes / (double) totalBytes * 100.0);
        legendVideo.setVisibility(totalVideoBytes > 0 ? View.VISIBLE : View.GONE);
        textStatsVideoSize.setText(Helper.formatBytes(totalVideoBytes, false));
        applyLayoutWeight(viewVideoStatsBar, percentVideo);

        int percentAudio = normalizePercent((double) totalAudioBytes / (double) totalBytes * 100.0);
        legendAudio.setVisibility(totalAudioBytes > 0 ? View.VISIBLE : View.GONE);
        textStatsAudioSize.setText(Helper.formatBytes(totalAudioBytes, false));
        applyLayoutWeight(viewAudioStatsBar, percentAudio);

        int percentImage = normalizePercent((double) totalImageBytes / (double) totalBytes * 100.0);
        legendImage.setVisibility(totalImageBytes > 0 ? View.VISIBLE : View.GONE);
        textStatsImageSize.setText(Helper.formatBytes(totalImageBytes, false));
        applyLayoutWeight(viewImageStatsBar, percentImage);

        int percentOther = normalizePercent((double) totalOtherBytes / (double) totalBytes * 100.0);
        legendOther.setVisibility(totalOtherBytes > 0 ? View.VISIBLE : View.GONE);
        textStatsOtherSize.setText(Helper.formatBytes(totalOtherBytes, false));
        applyLayoutWeight(viewOtherStatsBar, percentOther);

        // We have to get to 100 (or adjust the container accordingly)
        int totalPercent = percentVideo + percentAudio + percentImage + percentOther;
        ((LinearLayout) viewStatsDistribution).setWeightSum(totalPercent);
    }

    private void applyLayoutWeight(View view, int weight) {
        LinearLayout.LayoutParams params =  (LinearLayout.LayoutParams) view.getLayoutParams();
        params.weight = weight;
    }

    private static int normalizePercent(double value) {
        if (value > 0 && value < 1) {
            return 1;
        }
        return Double.valueOf(Math.floor(value)).intValue();
    }
    
    private void checkStatsLink() {
        linkStats.setVisibility(cardStats.getVisibility() == View.VISIBLE ||
                        listLoading.getVisibility() == View.VISIBLE ||
                        currentFilter != FILTER_DOWNLOADS ||
                        !Lbry.SDK_READY ?
                View.GONE : View.VISIBLE);
    }

    @Override
    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
        this.actionMode = actionMode;
        Context context = getContext();
        if (context instanceof MainActivity) {
            MainActivity activity = (MainActivity) context;
            if (!activity.isDarkMode()) {
                activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            }
        }

        actionMode.getMenuInflater().inflate(R.menu.menu_claim_list, menu);
        return true;
    }
    @Override
    public void onDestroyActionMode(ActionMode actionMode) {
        if (contentListAdapter != null) {
            contentListAdapter.clearSelectedItems();
            contentListAdapter.setInSelectionMode(false);
            contentListAdapter.notifyDataSetChanged();
        }
        Context context = getContext();
        if (context != null) {
            MainActivity activity = (MainActivity) context;
            if (!activity.isDarkMode()) {
                activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        }
        this.actionMode = null;
    }

    @Override
    public boolean onPrepareActionMode(androidx.appcompat.view.ActionMode actionMode, Menu menu) {
        menu.findItem(R.id.action_edit).setVisible(false);
        return true;
    }

    @Override
    public boolean onActionItemClicked(androidx.appcompat.view.ActionMode actionMode, MenuItem menuItem) {
        if (R.id.action_delete == menuItem.getItemId()) {
            if (contentListAdapter != null && contentListAdapter.getSelectedCount() > 0) {
                final List<Claim> selectedClaims = new ArrayList<>(contentListAdapter.getSelectedItems());
                String message = getResources().getQuantityString(R.plurals.confirm_delete_files, selectedClaims.size());
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext()).
                        setTitle(R.string.delete_selection).
                        setMessage(message)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                handleDeleteSelectedClaims(selectedClaims);
                            }
                        }).setNegativeButton(R.string.no, null);
                builder.show();
                return true;
            }
        }

        return false;
    }

    private void handleDeleteSelectedClaims(List<Claim> selectedClaims) {
        List<String> claimIds = new ArrayList<>();
        for (Claim claim : selectedClaims) {
            claimIds.add(claim.getClaimId());
        }

        new BulkDeleteFilesTask(claimIds).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        Lbry.unsetFilesForCachedClaims(claimIds);
        if (currentFilter == FILTER_DOWNLOADS) {
            contentListAdapter.removeItems(selectedClaims);
        }
        if (actionMode != null) {
            actionMode.finish();
        }
        View root = getView();
        if (root != null) {
            String message = getResources().getQuantityString(R.plurals.files_deleted, claimIds.size());
            Snackbar.make(root, message, Snackbar.LENGTH_LONG).show();
        }
    }

    private List<String> buildUrlsToResolve(List<Claim> claims) {
        List<String> urls = new ArrayList<>();
        for (Claim claim : claims) {
            Claim channel = claim.getSigningChannel();
            if (channel != null && Helper.isNullOrEmpty(channel.getName()) && !Helper.isNullOrEmpty(channel.getClaimId())) {
                LbryUri uri = LbryUri.tryParse(String.format("%s#%s", claim.getName(), claim.getClaimId()));
                if (uri != null) {
                    urls.add(uri.toString());
                }
            }
        }
        return urls;
    }

    private void resolveMissingChannelNames(List<String> urls) {
        if (urls.size() > 0) {
            ResolveTask task = new ResolveTask(urls, Lbry.SDK_CONNECTION_STRING, null, new ClaimListResultHandler() {
                @Override
                public void onSuccess(List<Claim> claims) {
                    boolean updated = false;
                    for (Claim claim : claims) {
                        if (claim.getClaimId() == null) {
                            continue;
                        }

                        if (contentListAdapter != null) {
                            contentListAdapter.updateSigningChannelForClaim(claim);
                            updated = true;
                        }
                    }
                    if (updated) {
                        contentListAdapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onError(Exception error) {

                }
            });
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    public void onEnterSelectionMode() {
        Context context = getContext();
        if (context instanceof MainActivity) {
            MainActivity activity = (MainActivity) context;
            activity.startSupportActionMode(this);
        }
    }
    public void onItemSelectionToggled() {
        if (actionMode != null) {
            actionMode.setTitle(String.valueOf(contentListAdapter.getSelectedCount()));
            actionMode.invalidate();
        }
    }
    public void onExitSelectionMode() {
        if (actionMode != null) {
            actionMode.finish();
        }
    }
}
