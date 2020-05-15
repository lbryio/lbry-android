package io.lbry.browser.ui.library;

import android.content.Context;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.lbry.browser.MainActivity;
import io.lbry.browser.R;
import io.lbry.browser.adapter.ClaimListAdapter;
import io.lbry.browser.data.DatabaseHelper;
import io.lbry.browser.listener.DownloadActionListener;
import io.lbry.browser.listener.SdkStatusListener;
import io.lbry.browser.model.Claim;
import io.lbry.browser.model.LbryFile;
import io.lbry.browser.model.ViewHistory;
import io.lbry.browser.tasks.file.FileListTask;
import io.lbry.browser.tasks.localdata.FetchViewHistoryTask;
import io.lbry.browser.ui.BaseFragment;
import io.lbry.browser.utils.Helper;
import io.lbry.browser.utils.Lbry;
import io.lbry.browser.utils.LbryAnalytics;

public class LibraryFragment extends BaseFragment implements DownloadActionListener, SdkStatusListener  {

    private static final int FILTER_DOWNLOADS = 1;
    private static final int FILTER_HISTORY = 2;
    private static final int PAGE_SIZE = 50;

    private int currentFilter;
    private List<LbryFile> currentFiles;
    private View layoutSdkInitializing;
    private RecyclerView contentList;
    private ClaimListAdapter contentListAdapter;
    private ProgressBar listLoading;
    private TextView linkFilterDownloads;
    private TextView linkFilterHistory;
    private View layoutListEmpty;
    private TextView textListEmpty;
    private int currentPage;
    private Date lastDate;
    private boolean listReachedEnd;

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
        linkFilterHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showHistory();
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
        }
    }

    private void showDownloads() {
        currentFilter = FILTER_DOWNLOADS;
        linkFilterDownloads.setTypeface(null, Typeface.BOLD);
        linkFilterHistory.setTypeface(null, Typeface.NORMAL);
        if (contentListAdapter != null) {
            contentListAdapter.clearItems();
            contentListAdapter.setCanEnterSelectionMode(true);
        }

        checkStatsLink();
        layoutSdkInitializing.setVisibility(Lbry.SDK_READY ? View.GONE : View.VISIBLE);
        currentPage = 1;
        if (Lbry.SDK_READY) {
            fetchDownloads();
        }
    }

    private void showHistory() {
        currentFilter = FILTER_HISTORY;
        linkFilterDownloads.setTypeface(null, Typeface.NORMAL);
        linkFilterHistory.setTypeface(null, Typeface.BOLD);
        if (contentListAdapter != null) {
            contentListAdapter.clearItems();
            contentListAdapter.setCanEnterSelectionMode(false);
        }

        cardStats.setVisibility(View.GONE);
        checkStatsLink();

        layoutSdkInitializing.setVisibility(View.GONE);
        lastDate = null;
        fetchHistory();
    }

    private void initContentListAdapter(List<Claim> claims) {
        contentListAdapter = new ClaimListAdapter(claims, getContext());
        contentListAdapter.setCanEnterSelectionMode(true);
        contentListAdapter.setListener(new ClaimListAdapter.ClaimListItemListener() {
            @Override
            public void onClaimClicked(Claim claim) {
                Context context = getContext();
                if (context instanceof MainActivity) {
                    MainActivity activity = (MainActivity) getContext();
                    if (claim.getName().startsWith("@")) {
                        activity.openChannelUrl(claim.getPermanentUrl());
                    } else {
                        MainActivity.openFileUrl(claim.getPermanentUrl(), context);
                    }
                }
            }
        });
    }

    private void fetchDownloads() {
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
                contentList.setAdapter(contentListAdapter);
                checkListEmpty();
            }

            @Override
            public void onError(Exception error) {
                // pass
                checkStatsLink();
                checkListEmpty();
            }
        });
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void fetchHistory() {
        Helper.setViewVisibility(layoutListEmpty, View.GONE);
        DatabaseHelper dbHelper = DatabaseHelper.getInstance();
        if (dbHelper != null) {
            FetchViewHistoryTask task = new FetchViewHistoryTask(lastDate, PAGE_SIZE, dbHelper, new FetchViewHistoryTask.FetchViewHistoryHandler() {
                @Override
                public void onSuccess(List<ViewHistory> history, boolean hasReachedEnd) {
                    listReachedEnd = hasReachedEnd;
                    List<Claim> claims = Helper.claimsFromViewHistory(history);
                    if (contentListAdapter == null) {
                        initContentListAdapter(claims);
                    } else {
                        contentListAdapter.addItems(claims);
                    }
                    contentList.setAdapter(contentListAdapter);
                    checkListEmpty();
                }
            });
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            checkListEmpty();
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
        textListEmpty.setText(currentFilter == FILTER_DOWNLOADS ? R.string.library_no_downloads : R.string.library_no_history);
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
                        currentFilter == FILTER_HISTORY ?
                View.GONE : View.VISIBLE);
    }
}
