package io.lbry.browser.ui.findcontent;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Outline;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.webkit.WebSettingsCompat;
import androidx.webkit.WebViewFeature;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultControlDispatcher;
import com.google.android.exoplayer2.ParserException;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.database.ExoDatabaseProvider;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultLoadErrorHandlingPolicy;
import com.google.android.exoplayer2.upstream.Loader;
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;
import com.google.android.exoplayer2.util.Util;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import io.lbry.browser.MainActivity;
import io.lbry.browser.R;
import io.lbry.browser.adapter.ClaimListAdapter;
import io.lbry.browser.adapter.CommentListAdapter;
import io.lbry.browser.adapter.InlineChannelSpinnerAdapter;
import io.lbry.browser.adapter.TagListAdapter;
import io.lbry.browser.dialog.RepostClaimDialogFragment;
import io.lbry.browser.dialog.CreateSupportDialogFragment;
import io.lbry.browser.exceptions.LbryUriException;
import io.lbry.browser.listener.DownloadActionListener;
import io.lbry.browser.listener.FetchClaimsListener;
import io.lbry.browser.listener.PIPModeListener;
import io.lbry.browser.listener.ScreenOrientationListener;
import io.lbry.browser.listener.SdkStatusListener;
import io.lbry.browser.listener.StoragePermissionListener;
import io.lbry.browser.listener.WalletBalanceListener;
import io.lbry.browser.model.Claim;
import io.lbry.browser.model.ClaimCacheKey;
import io.lbry.browser.model.Comment;
import io.lbry.browser.model.Fee;
import io.lbry.browser.model.LbryFile;
import io.lbry.browser.model.NavMenuItem;
import io.lbry.browser.model.Tag;
import io.lbry.browser.model.UrlSuggestion;
import io.lbry.browser.model.WalletBalance;
import io.lbry.browser.model.lbryinc.Reward;
import io.lbry.browser.model.lbryinc.Subscription;
import io.lbry.browser.tasks.BufferEventTask;
import io.lbry.browser.tasks.CommentCreateWithTipTask;
import io.lbry.browser.tasks.CommentListHandler;
import io.lbry.browser.tasks.CommentListTask;
import io.lbry.browser.tasks.GenericTaskHandler;
import io.lbry.browser.tasks.LighthouseSearchTask;
import io.lbry.browser.tasks.ReadTextFileTask;
import io.lbry.browser.tasks.SetSdkSettingTask;
import io.lbry.browser.tasks.claim.AbandonHandler;
import io.lbry.browser.tasks.claim.AbandonStreamTask;
import io.lbry.browser.tasks.claim.ClaimListResultHandler;
import io.lbry.browser.tasks.claim.ClaimListTask;
import io.lbry.browser.tasks.claim.ClaimSearchResultHandler;
import io.lbry.browser.tasks.claim.PurchaseListTask;
import io.lbry.browser.tasks.claim.ResolveTask;
import io.lbry.browser.tasks.file.DeleteFileTask;
import io.lbry.browser.tasks.file.FileListTask;
import io.lbry.browser.tasks.file.GetFileTask;
import io.lbry.browser.tasks.lbryinc.ChannelSubscribeTask;
import io.lbry.browser.tasks.lbryinc.ClaimRewardTask;
import io.lbry.browser.tasks.lbryinc.FetchStatCountTask;
import io.lbry.browser.tasks.lbryinc.LogFileViewTask;
import io.lbry.browser.ui.BaseFragment;
import io.lbry.browser.ui.controls.OutlineIconView;
import io.lbry.browser.ui.controls.SolidIconView;
import io.lbry.browser.ui.publish.PublishFragment;
import io.lbry.browser.utils.Helper;
import io.lbry.browser.utils.Lbry;
import io.lbry.browser.utils.LbryAnalytics;
import io.lbry.browser.utils.LbryUri;
import io.lbry.browser.utils.Lbryio;
import io.lbry.lbrysdk.DownloadManager;
import io.lbry.lbrysdk.LbrynetService;
import io.lbry.lbrysdk.Utils;

public class FileViewFragment extends BaseFragment implements
        MainActivity.BackPressInterceptor,
        DownloadActionListener,
        FetchClaimsListener,
        PIPModeListener,
        ScreenOrientationListener,
        SdkStatusListener,
        StoragePermissionListener,
        WalletBalanceListener {
    private static final int RELATED_CONTENT_SIZE = 16;
    private static final String DEFAULT_PLAYBACK_SPEED = "1x";
    private static final String CDN_PREFIX = "https://cdn.lbryplayer.xyz";

    private PlayerControlView castControlView;
    private Player currentPlayer;
    private boolean loadingNewClaim;
    private boolean startDownloadPending;
    private boolean fileGetPending;
    private boolean downloadInProgress;
    private boolean downloadRequested;
    private boolean loadFilePending;
    private boolean isPlaying;
    private boolean resolving;
    private boolean initialFileLoadDone;
    private Claim claim;
    private String currentUrl;
    private ClaimListAdapter relatedContentAdapter;
    private CommentListAdapter commentListAdapter;
    private BroadcastReceiver sdkReceiver;
    private Player.EventListener fileViewPlayerListener;

    private long elapsedDuration = 0;
    private long totalDuration = 0;
    private boolean elapsedPlaybackScheduled;
    private ScheduledExecutorService elapsedPlaybackScheduler;
    private boolean playbackStarted;
    private long startTimeMillis;
    private GetFileTask getFileTask;

    private boolean storagePermissionRefusedOnce;
    private View buttonPublishSomething;
    private View layoutLoadingState;
    private View layoutNothingAtLocation;
    private View layoutDisplayArea;
    private View layoutResolving;
    private int lastPositionSaved;

    private WebView webView;
    private boolean webViewAdded;

    private Comment replyToComment;
    private View containerReplyToComment;
    private TextView textReplyingTo;
    private TextView textReplyToBody;
    private View buttonClearReplyToComment;

    private boolean postingComment;
    private boolean fetchingChannels;
    private View progressLoadingChannels;
    private View progressPostComment;
    private InlineChannelSpinnerAdapter commentChannelSpinnerAdapter;
    private AppCompatSpinner commentChannelSpinner;
    private TextInputEditText inputComment;
    private TextView textCommentLimit;
    private MaterialButton buttonPostComment;
    private ImageView commentPostAsThumbnail;
    private View commentPostAsNoThumbnail;
    private TextView commentPostAsAlpha;

    private View inlineChannelCreator;
    private TextInputEditText inlineChannelCreatorInputName;
    private TextInputEditText inlineChannelCreatorInputDeposit;
    private View inlineChannelCreatorInlineBalance;
    private TextView inlineChannelCreatorInlineBalanceValue;
    private View inlineChannelCreatorCancelLink;
    private View inlineChannelCreatorProgress;
    private MaterialButton inlineChannelCreatorCreateButton;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_file_view, container, false);

        layoutLoadingState = root.findViewById(R.id.file_view_loading_state);
        layoutNothingAtLocation = root.findViewById(R.id.container_nothing_at_location);
        layoutResolving = root.findViewById(R.id.file_view_loading_container);
        layoutDisplayArea = root.findViewById(R.id.file_view_claim_display_area);
        buttonPublishSomething = root.findViewById(R.id.nothing_at_location_publish_button);

        containerReplyToComment = root.findViewById(R.id.comment_form_reply_to_container);
        textReplyingTo = root.findViewById(R.id.comment_form_replying_to_text);
        textReplyToBody = root.findViewById(R.id.comment_form_reply_to_body);
        buttonClearReplyToComment = root.findViewById(R.id.comment_form_clear_reply_to);

        commentChannelSpinner = root.findViewById(R.id.comment_form_channel_spinner);
        progressLoadingChannels = root.findViewById(R.id.comment_form_channels_loading);
        progressPostComment = root.findViewById(R.id.comment_form_post_progress);
        inputComment = root.findViewById(R.id.comment_form_body);
        textCommentLimit = root.findViewById(R.id.comment_form_text_limit);
        buttonPostComment = root.findViewById(R.id.comment_form_post);
        commentPostAsThumbnail = root.findViewById(R.id.comment_form_thumbnail);
        commentPostAsNoThumbnail = root.findViewById(R.id.comment_form_no_thumbnail);
        commentPostAsAlpha = root.findViewById(R.id.comment_form_thumbnail_alpha);

        inlineChannelCreator = root.findViewById(R.id.container_inline_channel_form_create);
        inlineChannelCreatorInputName = root.findViewById(R.id.inline_channel_form_input_name);
        inlineChannelCreatorInputDeposit = root.findViewById(R.id.inline_channel_form_input_deposit);
        inlineChannelCreatorInlineBalance = root.findViewById(R.id.inline_channel_form_inline_balance_container);
        inlineChannelCreatorInlineBalanceValue = root.findViewById(R.id.inline_channel_form_inline_balance_value);
        inlineChannelCreatorProgress = root.findViewById(R.id.inline_channel_form_create_progress);
        inlineChannelCreatorCancelLink = root.findViewById(R.id.inline_channel_form_cancel_link);
        inlineChannelCreatorCreateButton = root.findViewById(R.id.inline_channel_form_create_button);

        initUi(root);

        fileViewPlayerListener = new Player.EventListener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if (playbackState == Player.STATE_READY) {
                    elapsedDuration = MainActivity.appPlayer.getCurrentPosition();
                    totalDuration = MainActivity.appPlayer.getDuration() < 0 ? 0 : MainActivity.appPlayer.getDuration();
                    if (!playbackStarted) {
                        logPlay(currentUrl, startTimeMillis);
                        playbackStarted = true;
                        isPlaying = true;

                        long lastPosition = loadLastPlaybackPosition();
                        if (lastPosition > -1) {
                            MainActivity.appPlayer.seekTo(lastPosition);
                        }
                    }
                    renderTotalDuration();
                    scheduleElapsedPlayback();
                    hideBuffering();

                    if (loadingNewClaim) {
                        MainActivity.appPlayer.setPlayWhenReady(true);
                        loadingNewClaim = false;
                    }
                } else if (playbackState == Player.STATE_BUFFERING) {
                    if (MainActivity.appPlayer != null && MainActivity.appPlayer.getCurrentPosition() > 0) {
                        // we only want to log a buffer event after the media has already started playing
                        String mediaSourceUrl = getStreamingUrl();
                        long duration = MainActivity.appPlayer.getDuration();
                        long position = MainActivity.appPlayer.getCurrentPosition();
                        // TODO: Determine a hash for the userId
                        String userIdHash = Helper.SHA256(Lbryio.currentUser != null ? String.valueOf(Lbryio.currentUser.getId()) : "0");
                        if (mediaSourceUrl.startsWith(CDN_PREFIX)) {
                            BufferEventTask bufferEvent = new BufferEventTask(claim.getPermanentUrl(), duration, position, 1, userIdHash);
                            bufferEvent.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
                        } else {
                            // sdk stream buffer events should be handled differently
                            Bundle bundle = new Bundle();
                            bundle.putString("url", claim.getPermanentUrl());
                            bundle.putLong("stream_duration", duration);
                            bundle.putLong("stream_position", position);
                            bundle.putString("user_id_hash", userIdHash);
                            LbryAnalytics.logEvent(LbryAnalytics.EVENT_BUFFER, bundle);
                        }
                    }

                    showBuffering();
                } else {
                    hideBuffering();
                }
            }
        };

        return root;
    }

    public void onStart() {
        super.onStart();
        Context context = getContext();

        if (context instanceof MainActivity) {
            MainActivity activity = (MainActivity) context;
            activity.setBackPressInterceptor(this);
            activity.addDownloadActionListener(this);
            activity.addFetchClaimsListener(this);
            activity.addPIPModeListener(this);
            activity.addScreenOrientationListener(this);
            activity.addWalletBalanceListener(this);
            if (!MainActivity.hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, context)) {
                activity.addStoragePermissionListener(this);
            }
        }
    }

    private void checkParams() {
        boolean updateRequired = false;
        Context context = getContext();
        Map<String, Object> params = getParams();
        Claim newClaim = null;
        String newUrl = null;
        if (params != null) {
            if (params.containsKey("claim")) {
                newClaim = (Claim) params.get("claim");
                if (newClaim != null && !newClaim.equals(this.claim)) {
                    updateRequired = true;
                }
            }
            if (params.containsKey("url")) {
                newUrl = params.get("url").toString();
                if (claim == null || !newUrl.equalsIgnoreCase(currentUrl)) {
                    updateRequired = true;
                }
            }
        } else if (currentUrl != null) {
            updateRequired = true;
        } else if (context instanceof MainActivity) {
            ((MainActivity) context).onBackPressed();
        }

        boolean invalidRepost = false;
        if (updateRequired) {
            if (context instanceof MainActivity) {
                ((MainActivity) context).clearNowPlayingClaim();
            }
            if (MainActivity.appPlayer != null) {
                MainActivity.appPlayer.setPlayWhenReady(false);
            }

            resetViewCount();
            resetFee();
            checkNewClaimAndUrl(newClaim, newUrl);

            if (newClaim != null) {
                claim = newClaim;
            }
            if (!Helper.isNullOrEmpty(newUrl)) {
                // check if the claim is already cached
                currentUrl = newUrl;
                ClaimCacheKey key = new ClaimCacheKey();
                key.setUrl(currentUrl);
                onNewClaim(currentUrl);
                if (Lbry.claimCache.containsKey(key)) {
                    claim = Lbry.claimCache.get(key);
                    if (Claim.TYPE_REPOST.equalsIgnoreCase(claim.getValueType())) {
                        claim = claim.getRepostedClaim();
                        if (claim == null || Helper.isNullOrEmpty(claim.getClaimId())) {
                            // Invalid repost, probably
                            invalidRepost = true;
                            renderNothingAtLocation();
                        } else if (claim.getName().startsWith("@")) {
                            // this is a reposted channel, so launch the channel url
                            if (context instanceof  MainActivity) {
                                ((MainActivity) context).openChannelUrl(!Helper.isNullOrEmpty(claim.getShortUrl()) ? claim.getShortUrl() : claim.getPermanentUrl());
                            }
                            return;
                        }
                    }
                } else {
                    resolveUrl(currentUrl);
                }
            } else if (claim == null) {
                // nothing at this location
                renderNothingAtLocation();
            }
        } else {
            checkAndResetNowPlayingClaim();
        }

        if (!Helper.isNullOrEmpty(currentUrl)) {
            Helper.saveUrlHistory(currentUrl, claim != null ? claim.getTitle() : null, UrlSuggestion.TYPE_FILE);
        }

        if (claim != null && !invalidRepost) {
            Helper.saveViewHistory(currentUrl, claim);
            checkAndLoadRelatedContent();
            checkAndLoadComments();
            renderClaim();
            if (claim.getFile() == null) {
                loadFile();
            } else {
                initialFileLoadDone = true;
            }
        }

        checkIsFileComplete();
    }

    private void renderNothingAtLocation() {
        Helper.setViewVisibility(layoutLoadingState, View.VISIBLE);
        Helper.setViewVisibility(layoutNothingAtLocation, View.VISIBLE);
        Helper.setViewVisibility(buttonPublishSomething, View.VISIBLE);
        Helper.setViewVisibility(layoutResolving, View.GONE);
        Helper.setViewVisibility(layoutDisplayArea, View.INVISIBLE);
    }

    private void checkNewClaimAndUrl(Claim newClaim, String newUrl) {
        boolean shouldResetNowPlaying = false;
        if (newClaim != null &&
                MainActivity.nowPlayingClaim != null &&
                !MainActivity.nowPlayingClaim.getClaimId().equalsIgnoreCase(newClaim.getClaimId())) {
            shouldResetNowPlaying = true;
        }
        if (!shouldResetNowPlaying &&
                newUrl != null &&
                MainActivity.nowPlayingClaim != null &&
                !newUrl.equalsIgnoreCase(MainActivity.nowPlayingClaim.getShortUrl()) &&
                !newUrl.equalsIgnoreCase(MainActivity.nowPlayingClaim.getPermanentUrl())) {
            shouldResetNowPlaying = true;
        }

        if (shouldResetNowPlaying) {
            if (MainActivity.appPlayer != null) {
                MainActivity.appPlayer.setPlayWhenReady(false);
            }
            Context context = getContext();
            if (context instanceof MainActivity) {
                ((MainActivity) context).clearNowPlayingClaim();
                resetPlayer();
            }
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            currentUrl = savedInstanceState.getString("url");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            savedInstanceState.putString("url", currentUrl);
        }
    }

    private void initWebView(View root) {
        Context ctx = getContext();
        if (ctx != null) {
            if (webView == null) {
                webView = new WebView(ctx);
                webView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
                webView.setWebViewClient(new LbryWebViewClient(ctx));
                WebSettings webSettings = webView.getSettings();
                webSettings.setAllowFileAccess(true);
                webSettings.setJavaScriptEnabled(true);
            }

            if (!webViewAdded && root != null) {
                ((RelativeLayout) root.findViewById(R.id.file_view_webview_container)).addView(webView);
                webViewAdded = true;
            }
        }
    }

    private void applyThemeToWebView() {
        Context context = getContext();
        if (context instanceof MainActivity && webView != null && WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
            MainActivity activity = (MainActivity) context;
            WebSettingsCompat.setForceDark(webView.getSettings(), activity.isDarkMode() ? WebSettingsCompat.FORCE_DARK_ON : WebSettingsCompat.FORCE_DARK_OFF);
        }
    }

    private void logUrlEvent(String url) {
        Bundle bundle = new Bundle();
        bundle.putString("uri", url);
        LbryAnalytics.logEvent(LbryAnalytics.EVENT_OPEN_FILE_PAGE, bundle);
    }

    private void checkAndResetNowPlayingClaim() {
        if (MainActivity.nowPlayingClaim != null
                && claim != null &&
                !MainActivity.nowPlayingClaim.getClaimId().equalsIgnoreCase(claim.getClaimId())) {
            Context context = getContext();
            if (context instanceof MainActivity) {
                MainActivity activity = (MainActivity) context;
                activity.clearNowPlayingClaim();
                if (claim != null && !claim.isPlayable()) {
                    activity.stopExoplayer();
                }
            }
        }
    }

    private void onNewClaim(String url) {
        loadingNewClaim = true;
        initialFileLoadDone = false;
        playbackStarted = false;
        currentUrl = url;
        logUrlEvent(url);
        resetViewCount();
        resetFee();

        View root = getView();
        if (root != null) {
            if (relatedContentAdapter != null) {
                relatedContentAdapter.clearItems();
            }
            if (commentListAdapter != null) {
                commentListAdapter.clearItems();
            }
            ((RecyclerView) root.findViewById(R.id.file_view_related_content_list)).setAdapter(null);
            ((RecyclerView) root.findViewById(R.id.file_view_comments_list)).setAdapter(null);
        }
        if (MainActivity.appPlayer != null) {
            MainActivity.appPlayer.setPlayWhenReady(false);
        }
        resetPlayer();
    }

    public void onSdkReady() {
        if (loadFilePending) {
            loadFile();
        }
        checkOwnClaim();
        fetchChannels();
        checkAndLoadComments();
    }

    private String getStreamingUrl() {
        LbryFile lbryFile = claim.getFile();
        if (lbryFile != null) {
            if (!Helper.isNullOrEmpty(lbryFile.getDownloadPath()) && lbryFile.isCompleted()) {
                File file = new File(lbryFile.getDownloadPath());
                if (file.exists()) {
                    return Uri.fromFile(file).toString();
                }
            }

            if (!Helper.isNullOrEmpty(lbryFile.getStreamingUrl()) && !Lbryio.isSignedIn()) {
                return lbryFile.getStreamingUrl();
            }
        }

        return buildLbryTvStreamingUrl();
    }

    private String buildLbryTvStreamingUrl() {
        return String.format("https://cdn.lbryplayer.xyz/content/claims/%s/%s/stream", claim.getName(), claim.getClaimId());
    }

    private void loadFile() {
        if (!Lbry.SDK_READY) {
            // make use of the lbry.tv streaming URL
            loadFilePending = true;
            return;
        }

        loadFilePending = false;
        String claimId = claim.getClaimId();
        FileListTask task = new FileListTask(claimId, null, new FileListTask.FileListResultHandler() {
            @Override
            public void onSuccess(List<LbryFile> files, boolean hasReachedEnd) {
                if (files.size() > 0) {
                    claim.setFile(files.get(0));
                    checkIsFileComplete();
                    if (!claim.isPlayable() && !claim.isViewable()) {
                        showUnsupportedView();
                    }
                } else {
                    if (!claim.isPlayable() && !claim.isViewable()) {
                        restoreMainActionButton();
                    }
                }

                initialFileLoadDone = true;
            }

            @Override
            public void onError(Exception error) {
                initialFileLoadDone = true;
            }
        });
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void openClaimUrl(String url) {
        resetViewCount();
        resetFee();
        currentUrl = url;

        ClaimCacheKey key = new ClaimCacheKey();
        key.setUrl(currentUrl);
        Claim oldClaim = claim;
        claim = null;
        if (Lbry.claimCache.containsKey(key)) {
            claim = Lbry.claimCache.get(key);
            if (oldClaim != null && oldClaim.getClaimId().equalsIgnoreCase(claim.getClaimId())) {
                // same claim
                return;
            }
        } else {
            resolveUrl(currentUrl);
        }

        resetMedia();
        onNewClaim(currentUrl);
        Helper.setWunderbarValue(currentUrl, getContext());

        if (claim != null) {
            Helper.saveViewHistory(url, claim);
            checkAndLoadRelatedContent();
            checkAndLoadComments();
            renderClaim();
        }
    }

    public void resetMedia() {
        View root = getView();
        if (root != null) {
            PlayerView view = root.findViewById(R.id.file_view_exoplayer_view);
            view.setShutterBackgroundColor(Color.BLACK);
            root.findViewById(R.id.file_view_exoplayer_container).setVisibility(View.GONE);
        }
        if (MainActivity.appPlayer != null) {
            MainActivity.appPlayer.stop();
        }
        resetPlayer();
    }

    public void onResume() {
        super.onResume();
        checkParams();

        Context context = getContext();
        Helper.setWunderbarValue(currentUrl, context);
        if (context instanceof MainActivity) {
            MainActivity activity = (MainActivity) context;
            LbryAnalytics.setCurrentScreen(activity, "File", "File");
            if (claim != null && claim.isPlayable() && activity.isInFullscreenMode()) {
                enableFullScreenMode();
            }
        }

        if (MainActivity.appPlayer != null) {
            if (MainActivity.playerReassigned) {
                setPlayerForPlayerView();
                MainActivity.playerReassigned = false;
            }

            View root = getView();
            if (root != null) {
                PlayerView playerView = root.findViewById(R.id.file_view_exoplayer_view);
                if (playerView.getPlayer() == null) {
                    playerView.setPlayer(MainActivity.appPlayer);
                }
            }

            loadAndScheduleDurations();
        }

        if (!Lbry.SDK_READY) {
            if (context instanceof MainActivity) {
                ((MainActivity) context).addSdkStatusListener(this);
            }
        } else {
            onSdkReady();
        }
        checkCommentSdkInitializing();
    }

    public void onStop() {
        super.onStop();
        Context context = getContext();
        if (context instanceof MainActivity) {
            MainActivity activity = (MainActivity) context;
            activity.removeDownloadActionListener(this);
            activity.removeFetchClaimsListener(this);
            activity.removePIPModeListener(this);
            activity.removeScreenOrientationListener(this);
            activity.removeSdkStatusListener(this);
            activity.removeStoragePermissionListener(this);
            activity.removeWalletBalanceListener(this);
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }

        closeWebView();
    }

    private void closeWebView() {
        if (webView != null) {
            webView.removeAllViews();
            webView.loadUrl("about:blank");
            webView.destroy();
            webView = null;
        }
        webViewAdded = false;
    }

    private void setPlayerForPlayerView() {
        View root = getView();
        if (root != null) {
            PlayerView view = root.findViewById(R.id.file_view_exoplayer_view);
            view.setPlayer(null);
            view.setPlayer(MainActivity.appPlayer);
        }
    }

    private View.OnClickListener followUnfollowListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (claim != null && claim.getSigningChannel() != null) {
                Claim publisher = claim.getSigningChannel();
                boolean isFollowing = Lbryio.isFollowing(publisher);
                Subscription subscription = Subscription.fromClaim(publisher);
                view.setEnabled(false);
                Context context = getContext();
                new ChannelSubscribeTask(context, publisher.getClaimId(), subscription, isFollowing, new ChannelSubscribeTask.ChannelSubscribeHandler() {
                    @Override
                    public void onSuccess() {
                        if (isFollowing) {
                            Lbryio.removeSubscription(subscription);
                            Lbryio.removeCachedResolvedSubscription(publisher);
                        } else {
                            Lbryio.addSubscription(subscription);
                            Lbryio.addCachedResolvedSubscription(publisher);
                        }
                        view.setEnabled(true);
                        checkIsFollowing();
                        FollowingFragment.resetClaimSearchContent = true;

                        // Save shared user state
                        if (context != null) {
                            context.sendBroadcast(new Intent(MainActivity.ACTION_SAVE_SHARED_USER_STATE));
                        }
                    }

                    @Override
                    public void onError(Exception exception) {
                        view.setEnabled(true);
                    }
                }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }
    };

    private void resolveUrl(String url) {
        resolving = true;
        Helper.setViewVisibility(layoutDisplayArea, View.INVISIBLE);
        Helper.setViewVisibility(layoutLoadingState, View.VISIBLE);
        Helper.setViewVisibility(layoutNothingAtLocation, View.GONE);
        ResolveTask task = new ResolveTask(url, Lbry.LBRY_TV_CONNECTION_STRING, layoutResolving, new ClaimListResultHandler() {
            @Override
            public void onSuccess(List<Claim> claims) {
                if (claims.size() > 0 && !Helper.isNullOrEmpty(claims.get(0).getClaimId())) {
                    claim = claims.get(0);
                    if (Claim.TYPE_REPOST.equalsIgnoreCase(claim.getValueType())) {
                        claim = claim.getRepostedClaim();
                        // cache the reposted claim too for subsequent loads
                        Lbry.addClaimToCache(claim);
                        if (claim.getName().startsWith("@")) {
                            // this is a reposted channel, so finish this activity and launch the channel url
                            Context context = getContext();
                            if (context instanceof  MainActivity) {
                                ((MainActivity) context).openChannelUrl(!Helper.isNullOrEmpty(claim.getShortUrl()) ? claim.getShortUrl() : claim.getPermanentUrl());
                            }
                            return;
                        }
                    } else {
                        Lbry.addClaimToCache(claim);
                    }

                    Helper.saveUrlHistory(url, claim.getTitle(), UrlSuggestion.TYPE_FILE);

                    // also save view history
                    Helper.saveViewHistory(url, claim);

                    checkAndResetNowPlayingClaim();
                    loadFile();

                    checkAndLoadRelatedContent();
                    checkAndLoadComments();
                    renderClaim();
                } else {
                    // render nothing at location
                    renderNothingAtLocation();
                }
            }

            @Override
            public void onError(Exception error) {
                resolving = false;
            }
        });
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void initUi(View root) {
        buttonPublishSomething.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Context context = getContext();
                if (!Helper.isNullOrEmpty(currentUrl) && context instanceof MainActivity) {
                    LbryUri uri = LbryUri.tryParse(currentUrl);
                    if (uri != null) {
                        Map<String, Object> params = new HashMap<>();
                        params.put("suggestedUrl", uri.getStreamName());
                        ((MainActivity) context).openFragment(PublishFragment.class, true, NavMenuItem.ID_ITEM_NEW_PUBLISH, params);
                    }
                }
            }
        });

        root.findViewById(R.id.file_view_title_area).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImageView descIndicator = root.findViewById(R.id.file_view_desc_toggle_arrow);
                View descriptionArea = root.findViewById(R.id.file_view_description_area);

                boolean hasDescription = claim != null && !Helper.isNullOrEmpty(claim.getDescription());
                boolean hasTags = claim != null && claim.getTags() != null && claim.getTags().size() > 0;

                if (descriptionArea.getVisibility() != View.VISIBLE) {
                    if (hasDescription || hasTags) {
                        descriptionArea.setVisibility(View.VISIBLE);
                    }
                    descIndicator.setImageResource(R.drawable.ic_arrow_dropup);
                } else {
                    descriptionArea.setVisibility(View.GONE);
                    descIndicator.setImageResource(R.drawable.ic_arrow_dropdown);
                }
            }
        });

        root.findViewById(R.id.file_view_action_share).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (claim != null) {
                    try {
                        String shareUrl = LbryUri.parse(
                                !Helper.isNullOrEmpty(claim.getCanonicalUrl()) ? claim.getCanonicalUrl() :
                                        (!Helper.isNullOrEmpty(claim.getShortUrl()) ? claim.getShortUrl() : claim.getPermanentUrl())).toTvString();
                        Intent shareIntent = new Intent();
                        shareIntent.setAction(Intent.ACTION_SEND);
                        shareIntent.setType("text/plain");
                        shareIntent.putExtra(Intent.EXTRA_TEXT, shareUrl);

                        MainActivity.startingShareActivity = true;
                        Intent shareUrlIntent = Intent.createChooser(shareIntent, getString(R.string.share_lbry_content));
                        shareUrlIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(shareUrlIntent);
                    } catch (LbryUriException ex) {
                        // pass
                    }
                }
            }
        });

        root.findViewById(R.id.file_view_action_tip).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!Lbry.SDK_READY) {
                    Snackbar.make(root.findViewById(R.id.file_view_claim_display_area), R.string.sdk_initializing_functionality, Snackbar.LENGTH_LONG).show();
                    return;
                }

                if (claim != null) {
                    CreateSupportDialogFragment dialog = CreateSupportDialogFragment.newInstance();
                    dialog.setClaim(claim);
                    dialog.setListener(new CreateSupportDialogFragment.CreateSupportListener() {
                        @Override
                        public void onSupportCreated(BigDecimal amount, boolean isTip) {
                            double sentAmount = amount.doubleValue();
                            String message = getResources().getQuantityString(
                                    isTip ? R.plurals.you_sent_a_tip : R.plurals.you_sent_a_support, sentAmount == 1.0 ? 1 : 2,
                                    new DecimalFormat("#,###.##").format(sentAmount));
                            Snackbar.make(root.findViewById(R.id.file_view_claim_display_area), message, Snackbar.LENGTH_LONG).show();
                        }
                    });
                    Context context = getContext();
                    if (context instanceof MainActivity) {
                        dialog.show(((MainActivity) context).getSupportFragmentManager(), CreateSupportDialogFragment.TAG);
                    }
                }
            }
        });

        root.findViewById(R.id.file_view_action_repost).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!Lbry.SDK_READY) {
                    Snackbar.make(root.findViewById(R.id.file_view_claim_display_area), R.string.sdk_initializing_functionality, Snackbar.LENGTH_LONG).show();
                    return;
                }

                if (claim != null) {
                    RepostClaimDialogFragment dialog = RepostClaimDialogFragment.newInstance();
                    dialog.setClaim(claim);
                    dialog.setListener(new RepostClaimDialogFragment.RepostClaimListener() {
                        @Override
                        public void onClaimReposted(Claim claim) {
                            Context context = getContext();
                            if (context instanceof MainActivity) {
                                ((MainActivity) context).showMessage(R.string.content_successfully_reposted);
                            }
                        }
                    });
                    Context context = getContext();
                    if (context instanceof MainActivity) {
                        dialog.show(((MainActivity) context).getSupportFragmentManager(), RepostClaimDialogFragment.TAG);
                    }
                }
            }
        });

        root.findViewById(R.id.file_view_action_edit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!Lbry.SDK_READY) {
                    Snackbar.make(root.findViewById(R.id.file_view_claim_display_area), R.string.sdk_initializing_functionality, Snackbar.LENGTH_LONG).show();
                    return;
                }

                Context context = getContext();
                if (claim != null && context instanceof MainActivity) {
                    ((MainActivity) context).openPublishForm(claim);
                }
            }
        });

        root.findViewById(R.id.file_view_action_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!Lbry.SDK_READY) {
                    Snackbar.make(root.findViewById(R.id.file_view_claim_display_area), R.string.sdk_initializing_functionality, Snackbar.LENGTH_LONG).show();
                    return;
                }

                if (claim != null) {
                    boolean isOwnClaim = Lbry.ownClaims.contains(claim);
                    if (isOwnClaim) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext()).
                                setTitle(R.string.delete_content).
                                setMessage(R.string.confirm_delete_content_message)
                                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        deleteCurrentClaim();
                                    }
                                }).setNegativeButton(R.string.no, null);
                        builder.show();
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext()).
                                setTitle(R.string.delete_file).
                                setMessage(R.string.confirm_delete_file_message)
                                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        deleteClaimFile();
                                    }
                                }).setNegativeButton(R.string.no, null);
                        builder.show();
                    }
                }
            }
        });

        root.findViewById(R.id.file_view_action_download).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!Lbry.SDK_READY) {
                    Snackbar.make(root.findViewById(R.id.file_view_claim_display_area), R.string.sdk_initializing_functionality, Snackbar.LENGTH_LONG).show();
                    return;
                }

                if (claim != null) {
                    if (downloadInProgress) {
                        onDownloadAborted();

                        // file is already downloading and not completed
                        Intent intent = new Intent(LbrynetService.ACTION_DELETE_DOWNLOAD);
                        intent.putExtra("uri", claim.getPermanentUrl());
                        intent.putExtra("nativeDelete", true);
                        Context context = getContext();
                        if (context != null) {
                            context.sendBroadcast(intent);
                        }
                    } else {
                        checkStoragePermissionAndStartDownload();
                    }
                }
            }
        });

        root.findViewById(R.id.file_view_action_report).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (claim != null) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("https://lbry.com/dmca/%s", claim.getClaimId())));
                    startActivity(intent);
                }
            }
        });

        root.findViewById(R.id.player_toggle_cast).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleCast();
            }
        });

        PlayerView playerView = root.findViewById(R.id.file_view_exoplayer_view);
        View playbackSpeedContainer = playerView.findViewById(R.id.player_playback_speed);
        TextView textPlaybackSpeed = playerView.findViewById(R.id.player_playback_speed_label);
        textPlaybackSpeed.setText(DEFAULT_PLAYBACK_SPEED);

        playerView.setControlDispatcher(new DefaultControlDispatcher() {
            @Override
            public boolean dispatchSetPlayWhenReady(Player player, boolean playWhenReady) {
                isPlaying = playWhenReady;
                return super.dispatchSetPlayWhenReady(player, playWhenReady);
            }
        });

        playbackSpeedContainer.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
                Helper.buildPlaybackSpeedMenu(contextMenu);
            }
        });
        playbackSpeedContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Context context = getContext();
                if (context instanceof MainActivity) {
                    ((MainActivity) context).openContextMenu(playbackSpeedContainer);
                }
            }
        });

        playerView.findViewById(R.id.player_toggle_fullscreen).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // check full screen mode
                if (isInFullscreenMode()) {
                    disableFullScreenMode();
                } else {
                    enableFullScreenMode();
                }
            }
        });
        playerView.findViewById(R.id.player_skip_back_10).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (MainActivity.appPlayer != null) {
                    MainActivity.appPlayer.seekTo(Math.max(0, MainActivity.appPlayer.getCurrentPosition() - 10000));
                }
            }
        });
        playerView.findViewById(R.id.player_skip_forward_10).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (MainActivity.appPlayer != null) {
                    MainActivity.appPlayer.seekTo(MainActivity.appPlayer.getCurrentPosition() + 10000);
                }
            }
        });

        root.findViewById(R.id.file_view_publisher_info_area).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (claim != null && claim.getSigningChannel() != null) {
                    Claim publisher = claim.getSigningChannel();
                    Context context = getContext();
                    if (context instanceof  MainActivity) {
                        ((MainActivity) context).openChannelUrl(
                                !Helper.isNullOrEmpty(publisher.getShortUrl()) ? publisher.getShortUrl() : publisher.getPermanentUrl());
                    }
                }
            }
        });

        View buttonFollow = root.findViewById(R.id.file_view_icon_follow);
        View buttonUnfollow = root.findViewById(R.id.file_view_icon_unfollow);
        buttonFollow.setOnClickListener(followUnfollowListener);
        buttonUnfollow.setOnClickListener(followUnfollowListener);

        commentChannelSpinnerAdapter = new InlineChannelSpinnerAdapter(getContext(), R.layout.spinner_item_channel, new ArrayList<>());
        commentChannelSpinnerAdapter.addPlaceholder(false);

        initCommentForm(root);
        setupInlineChannelCreator(
                inlineChannelCreator,
                inlineChannelCreatorInputName,
                inlineChannelCreatorInputDeposit,
                inlineChannelCreatorInlineBalance,
                inlineChannelCreatorInlineBalanceValue,
                inlineChannelCreatorCancelLink,
                inlineChannelCreatorCreateButton,
                inlineChannelCreatorProgress,
                commentChannelSpinner,
                commentChannelSpinnerAdapter
        );

        RecyclerView relatedContentList = root.findViewById(R.id.file_view_related_content_list);
        RecyclerView commentsList = root.findViewById(R.id.file_view_comments_list);
        relatedContentList.setNestedScrollingEnabled(false);
        commentsList.setNestedScrollingEnabled(false);
        LinearLayoutManager relatedContentListLLM = new LinearLayoutManager(getContext());
        LinearLayoutManager commentsListLLM = new LinearLayoutManager(getContext());
        relatedContentList.setLayoutManager(relatedContentListLLM);
        commentsList.setLayoutManager(commentsListLLM);
    }

    private void deleteCurrentClaim() {
        if (claim != null) {
            Helper.setViewVisibility(layoutDisplayArea, View.INVISIBLE);
            Helper.setViewVisibility(layoutLoadingState, View.VISIBLE);
            Helper.setViewVisibility(layoutNothingAtLocation, View.GONE);
            AbandonStreamTask task = new AbandonStreamTask(Arrays.asList(claim.getClaimId()), layoutResolving, new AbandonHandler() {
                @Override
                public void onComplete(List<String> successfulClaimIds, List<String> failedClaimIds, List<Exception> errors) {
                    Context context = getContext();
                    if (context instanceof MainActivity) {
                        if (failedClaimIds.size() == 0) {
                            MainActivity activity = (MainActivity) context;
                            activity.showMessage(R.string.content_deleted);
                            activity.onBackPressed();
                        } else {
                            showError(getString(R.string.content_failed_delete));
                        }
                    }
                }
            });
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private void checkStoragePermissionAndStartDownload() {
        Context context = getContext();
        if (MainActivity.hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, context)) {
            startDownload();
        } else {
            if (storagePermissionRefusedOnce) {
                showStoragePermissionRefusedError();
                restoreMainActionButton();
                return;
            }

            startDownloadPending = true;
            MainActivity.requestPermission(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    MainActivity.REQUEST_STORAGE_PERMISSION,
                    getString(R.string.storage_permission_rationale_download),
                    context,
                    true);
        }
    }

    private void checkStoragePermissionAndFileGet() {
        Context context = getContext();
        if (!MainActivity.hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, context)) {
            if (storagePermissionRefusedOnce) {
                showStoragePermissionRefusedError();
                restoreMainActionButton();
                return;
            }

            try {
                if (context != null) {
                    fileGetPending = true;
                    MainActivity.requestPermission(
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            MainActivity.REQUEST_STORAGE_PERMISSION,
                            getString(R.string.storage_permission_rationale_download),
                            context,
                            true);
                }
            } catch (IllegalStateException ex) {
                // pass
            }
        } else {
            fileGet(true);
        }
    }

    public void onStoragePermissionGranted() {
        Context context = getContext();
        SetSdkSettingTask task = null;
        if (startDownloadPending) {
            startDownloadPending = false;
            task = new SetSdkSettingTask("download_dir", Utils.getConfiguredDownloadDirectory(context), new GenericTaskHandler() {
                @Override
                public void beforeStart() { }
                @Override
                public void onSuccess() { startDownload(); }
                @Override
                public void onError(Exception error) {
                    // start the download anyway. Only that it will be saved in the app private folder: /sdcard/Android/io.lbry.browser/Download
                    startDownload();
                }
            });
        } else if (fileGetPending) {
            fileGetPending = false;
            task = new SetSdkSettingTask("download_dir", Utils.getConfiguredDownloadDirectory(context), new GenericTaskHandler() {
                @Override
                public void beforeStart() { }
                @Override
                public void onSuccess() { fileGet(true); }
                @Override
                public void onError(Exception error) {
                    // start the file get anyway. Only that it will be saved in the app private folder: /sdcard/Android/io.lbry.browser/Download
                    fileGet(true);
                }
            });
        }
        if (task != null) {
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }
    public void onStoragePermissionRefused() {
        storagePermissionRefusedOnce = true;
        fileGetPending = false;
        startDownloadPending = false;
        onDownloadAborted();

        showStoragePermissionRefusedError();
    }

    public void startDownload() {
        downloadInProgress = true;

        View root = getView();
        if (root != null) {
            Helper.setViewVisibility(root.findViewById(R.id.file_view_download_progress), View.VISIBLE);
            ((ImageView) root.findViewById(R.id.file_view_action_download_icon)).setImageResource(R.drawable.ic_stop);
        }

        if (!claim.isFree()) {
            downloadRequested = true;
            onMainActionButtonClicked();
        } else {
            // download the file
            fileGet(true);
        }
    }

    private void deleteClaimFile() {
        if (claim != null) {
            View actionDelete = getView().findViewById(R.id.file_view_action_delete);
            DeleteFileTask task = new DeleteFileTask(claim.getClaimId(), new GenericTaskHandler() {
                @Override
                public void beforeStart() {
                    actionDelete.setEnabled(false);
                }

                @Override
                public void onSuccess() {
                    Helper.setViewVisibility(actionDelete, View.GONE);
                    View root = getView();
                    if (root != null) {
                        root.findViewById(R.id.file_view_action_download).setVisibility(View.VISIBLE);
                        root.findViewById(R.id.file_view_unsupported_container).setVisibility(View.GONE);
                    }
                    Helper.setViewEnabled(actionDelete, true);

                    claim.setFile(null);
                    Lbry.unsetFilesForCachedClaims(Arrays.asList(claim.getClaimId()));

                    restoreMainActionButton();
                }

                @Override
                public void onError(Exception error) {
                    actionDelete.setEnabled(true);
                    if (error != null) {
                        showError(error.getMessage());
                    }
                }
            });
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private void renderClaim() {
        if (claim == null) {
            return;
        }

        if (claim.isPlayable() && MainActivity.appPlayer != null) {
            MainActivity.appPlayer.setPlayWhenReady(isPlaying);
        }

        Helper.setViewVisibility(layoutLoadingState, View.GONE);
        Helper.setViewVisibility(layoutNothingAtLocation, View.GONE);

        loadViewCount();
        checkIsFollowing();
        
        View root = getView();
        if (root != null) {
            Context context = getContext();

            root.findViewById(R.id.file_view_scroll_view).scrollTo(0, 0);
            Helper.setViewVisibility(layoutDisplayArea, View.VISIBLE);

            ImageView descIndicator = root.findViewById(R.id.file_view_desc_toggle_arrow);
            descIndicator.setImageResource(R.drawable.ic_arrow_dropdown);

            boolean hasDescription = !Helper.isNullOrEmpty(claim.getDescription());
            boolean hasTags = claim.getTags() != null && claim.getTags().size() > 0;

            root.findViewById(R.id.file_view_description).setVisibility(hasDescription ? View.VISIBLE : View.GONE);
            root.findViewById(R.id.file_view_tag_area).setVisibility(hasTags ? View.VISIBLE : View.GONE);
            if (hasTags && !hasDescription) {
                root.findViewById(R.id.file_view_tag_area).setPadding(0, 0, 0, 0);
            }

            root.findViewById(R.id.file_view_description_area).setVisibility(View.GONE);
            ((TextView) root.findViewById(R.id.file_view_title)).setText(claim.getTitle());
            ((TextView) root.findViewById(R.id.file_view_description)).setText(claim.getDescription());
            ((TextView) root.findViewById(R.id.file_view_publisher_name)).setText(
                    Helper.isNullOrEmpty(claim.getPublisherName()) ? getString(R.string.anonymous) : claim.getPublisherName());

            Claim signingChannel = claim.getSigningChannel();
            boolean hasPublisher = signingChannel != null;
            boolean hasPublisherThumbnail = hasPublisher && !Helper.isNullOrEmpty(signingChannel.getThumbnailUrl());
            root.findViewById(R.id.file_view_publisher_avatar).setVisibility(hasPublisher ? View.VISIBLE : View.GONE);
            root.findViewById(R.id.file_view_publisher_thumbnail).setVisibility(hasPublisherThumbnail ? View.VISIBLE : View.INVISIBLE);
            root.findViewById(R.id.file_view_publisher_no_thumbnail).setVisibility(!hasPublisherThumbnail ? View.VISIBLE : View.INVISIBLE);
            if (hasPublisher) {
                int bgColor = Helper.generateRandomColorForValue(signingChannel.getClaimId());
                Helper.setIconViewBackgroundColor(root.findViewById(R.id.file_view_publisher_no_thumbnail), bgColor, false, context);
                if (hasPublisherThumbnail && context != null) {
                    Glide.with(context.getApplicationContext()).load(signingChannel.getThumbnailUrl()).
                            apply(RequestOptions.circleCropTransform()).into((ImageView) root.findViewById(R.id.file_view_publisher_thumbnail));
                }
                ((TextView) root.findViewById(R.id.file_view_publisher_thumbnail_alpha)).
                        setText(signingChannel.getName() != null ? signingChannel.getName().substring(1, 2).toUpperCase() : null);

            }

            String publisherTitle = signingChannel != null ? signingChannel.getTitle() : null;
            TextView textPublisherTitle = root.findViewById(R.id.file_view_publisher_title);
            textPublisherTitle.setVisibility(Helper.isNullOrEmpty(publisherTitle) ? View.GONE : View.VISIBLE);
            textPublisherTitle.setText(publisherTitle);

            RecyclerView descTagsList = root.findViewById(R.id.file_view_tag_list);
            FlexboxLayoutManager flm = new FlexboxLayoutManager(context);
            descTagsList.setLayoutManager(flm);

            List<Tag> tags = claim.getTagObjects();
            TagListAdapter tagListAdapter = new TagListAdapter(tags, context);
            tagListAdapter.setClickListener(new TagListAdapter.TagClickListener() {
                @Override
                public void onTagClicked(Tag tag, int customizeMode) {
                    if (customizeMode == TagListAdapter.CUSTOMIZE_MODE_NONE) {
                        Context ctx = getContext();
                        if (ctx instanceof MainActivity) {
                            ((MainActivity) ctx).openAllContentFragmentWithTag(tag.getName());
                        }
                    }
                }
            });
            descTagsList.setAdapter(tagListAdapter);
            root.findViewById(R.id.file_view_tag_area).setVisibility(tags.size() > 0 ? View.VISIBLE : View.GONE);

            root.findViewById(R.id.file_view_exoplayer_container).setVisibility(View.GONE);
            root.findViewById(R.id.file_view_unsupported_container).setVisibility(View.GONE);
            root.findViewById(R.id.file_view_media_meta_container).setVisibility(View.VISIBLE);

            Claim.GenericMetadata metadata = claim.getValue();
            if (!Helper.isNullOrEmpty(claim.getThumbnailUrl())) {
                ImageView thumbnailView = root.findViewById(R.id.file_view_thumbnail);
                Glide.with(context.getApplicationContext()).asBitmap().load(claim.getThumbnailUrl()).centerCrop().into(thumbnailView);
            } else {
                // display first x letters of claim name, with random background
            }

            root.findViewById(R.id.file_view_main_action_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onMainActionButtonClicked();
                }
            });
            root.findViewById(R.id.file_view_media_meta_container).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onMainActionButtonClicked();
                }
            });

            if (metadata instanceof Claim.StreamMetadata) {
                Claim.StreamMetadata streamMetadata = (Claim.StreamMetadata) metadata;
                long publishTime = streamMetadata.getReleaseTime() > 0 ? streamMetadata.getReleaseTime() * 1000 : claim.getTimestamp() * 1000;
                ((TextView) root.findViewById(R.id.file_view_publish_time)).setText(DateUtils.getRelativeTimeSpanString(
                        publishTime, System.currentTimeMillis(), 0, DateUtils.FORMAT_ABBREV_RELATIVE));

                Fee fee = streamMetadata.getFee();
                if (fee != null && Helper.parseDouble(fee.getAmount(), 0) > 0) {
                    root.findViewById(R.id.file_view_fee_container).setVisibility(View.VISIBLE);
                    ((TextView) root.findViewById(R.id.file_view_fee)).setText(
                            Helper.shortCurrencyFormat(claim.getActualCost(Lbryio.LBCUSDRate).doubleValue()));
                }
            }

            boolean isAnonymous = claim.getSigningChannel() == null;
            View iconFollow = root.findViewById(R.id.file_view_icon_follow);
            View iconUnfollow = root.findViewById(R.id.file_view_icon_unfollow);
            if (isAnonymous) {
                if (iconFollow.getVisibility() == View.VISIBLE) {
                    iconFollow.setVisibility(View.INVISIBLE);
                }
                if (iconUnfollow.getVisibility() == View.VISIBLE) {
                    iconUnfollow.setVisibility(View.INVISIBLE);
                }
            }

            MaterialButton mainActionButton = root.findViewById(R.id.file_view_main_action_button);
            if (claim.isPlayable()) {
                mainActionButton.setText(R.string.play);
            } else if (claim.isViewable()) {
                mainActionButton.setText(R.string.view);
            } else {
                mainActionButton.setText(R.string.download);
            }
        }

        if (claim.isFree()) {
            if (claim.isPlayable() || (!Lbry.SDK_READY && Lbryio.isSignedIn())) {
                if (MainActivity.nowPlayingClaim != null && MainActivity.nowPlayingClaim.getClaimId().equalsIgnoreCase(claim.getClaimId())) {
                    // claim already playing
                    showExoplayerView();
                    playMedia();
                } else {
                    onMainActionButtonClicked();
                }
            } else if (claim.isViewable() && Lbry.SDK_READY) {
                onMainActionButtonClicked();
            } else if (!Lbry.SDK_READY) {
                restoreMainActionButton();
            }
        } else {
            restoreMainActionButton();
        }

        if (Lbry.SDK_READY && !claim.isPlayable() && !claim.isViewable()) {
            if (claim.getFile() == null) {
                loadFile();
            } else {
                // file already loaded, but it's unsupported
                showUnsupportedView();
            }
        }

        checkRewardsDriver();
        checkOwnClaim();
    }

    private void checkAndLoadRelatedContent() {
        View root = getView();
        if (root != null) {
            RecyclerView relatedContentList = root.findViewById(R.id.file_view_related_content_list);
            if (relatedContentList == null || relatedContentList.getAdapter() == null || relatedContentList.getAdapter().getItemCount() == 0) {
                loadRelatedContent();
            }
        }
    }

    private void checkAndLoadComments() {
        View root = getView();
        if (root != null) {
            checkCommentSdkInitializing();
            RecyclerView commentsList = root.findViewById(R.id.file_view_comments_list);
            if (commentsList == null || commentsList.getAdapter() == null || commentsList.getAdapter().getItemCount() == 0) {
                loadComments();
            }
        }
    }

    private void checkCommentSdkInitializing() {
        View root = getView();
        if (root != null) {
            TextView commentsSDKInitializing = root.findViewById(R.id.file_view_comments_sdk_initializing);
            Helper.setViewVisibility(commentsSDKInitializing, Lbry.SDK_READY ? View.GONE : View.VISIBLE);
        }
    }

    private void showUnsupportedView() {
        View root = getView();
        if (root != null) {
            root.findViewById(R.id.file_view_exoplayer_container).setVisibility(View.GONE);
            root.findViewById(R.id.file_view_unsupported_container).setVisibility(View.VISIBLE);
            String fileNameString = "";
            if (claim.getFile() != null && !Helper.isNullOrEmpty(claim.getFile().getDownloadPath())) {
                LbryFile lbryFile = claim.getFile();
                File file = new File(lbryFile.getDownloadPath());
                fileNameString = String.format("\"%s\" ", file.getName());
            }
            ((TextView) root.findViewById(R.id.file_view_unsupported_text)).setText(getString(R.string.unsupported_content_desc, fileNameString));
        }
    }

    private void showExoplayerView() {
        View root = getView();
        if (root != null) {
            root.findViewById(R.id.file_view_unsupported_container).setVisibility(View.GONE);
            root.findViewById(R.id.file_view_exoplayer_container).setVisibility(View.VISIBLE);
        }
    }

    private void playMedia() {
        boolean newPlayerCreated = false;

        Context context = getContext();
        if (MainActivity.appPlayer == null && context != null) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.CONTENT_TYPE_MOVIE)
                    .build();

            MainActivity.appPlayer = new SimpleExoPlayer.Builder(context).build();
            MainActivity.appPlayer.setAudioAttributes(audioAttributes, true);
            MainActivity.playerCache =
                    new SimpleCache(context.getCacheDir(),
                            new LeastRecentlyUsedCacheEvictor(1024 * 1024 * 256), new ExoDatabaseProvider(context));
            if (context instanceof MainActivity) {
                MainActivity activity = (MainActivity) context;
                activity.initMediaSession();
                activity.initPlaybackNotification();
            }

            newPlayerCreated = true;
        }

        if (context instanceof MainActivity) {
            MainActivity activity = (MainActivity) context;
            activity.initPlaybackNotification();
        }

        View root = getView();
        if (root != null) {
            PlayerView view = root.findViewById(R.id.file_view_exoplayer_view);
            view.setShutterBackgroundColor(Color.TRANSPARENT);
            view.setPlayer(MainActivity.appPlayer);
            view.setUseController(true);
            if (context instanceof MainActivity) {
                ((MainActivity) context).getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }

            if (MainActivity.nowPlayingClaim != null &&
                    MainActivity.nowPlayingClaim.getClaimId().equalsIgnoreCase(claim.getClaimId()) &&
                    !newPlayerCreated) {
                // if the claim is already playing, we don't need to reload the media source
                return;
            }

            if (MainActivity.appPlayer != null) {
                showBuffering();
                if (fileViewPlayerListener != null) {
                    MainActivity.appPlayer.addListener(fileViewPlayerListener);
                }
                if (context instanceof MainActivity) {
                    ((MainActivity) context).setNowPlayingClaim(claim, currentUrl);
                }

                MainActivity.appPlayer.setPlayWhenReady(true);
                String userAgent = Util.getUserAgent(context, getString(R.string.app_name));
                String mediaSourceUrl = getStreamingUrl();
                MediaSource mediaSource = new ProgressiveMediaSource.Factory(
                        new CacheDataSourceFactory(MainActivity.playerCache, new DefaultDataSourceFactory(context, userAgent)),
                        new DefaultExtractorsFactory()
                ).setLoadErrorHandlingPolicy(new StreamLoadErrorPolicy()).createMediaSource(Uri.parse(mediaSourceUrl));

                MainActivity.appPlayer.prepare(mediaSource, true, true);
            }
        }
    }

    private void setCurrentPlayer(Player currentPlayer) {
        if (this.currentPlayer == currentPlayer) {
            return;
        }

        // View management.
        if (currentPlayer == MainActivity.appPlayer) {
            //localPlayerView.setVisibility(View.VISIBLE);
            castControlView.hide();
            ((ImageView) getView().findViewById(R.id.player_image_cast_toggle)).setImageResource(R.drawable.ic_cast);
        } else /* currentPlayer == castPlayer */ {
            castControlView.show();
            ((ImageView) getView().findViewById(R.id.player_image_cast_toggle)).setImageResource(R.drawable.ic_cast_connected);
        }

        // Player state management.
        long playbackPositionMs = C.TIME_UNSET;
        int windowIndex = C.INDEX_UNSET;
        boolean playWhenReady = false;

        Player previousPlayer = this.currentPlayer;
        if (previousPlayer != null) {
            // Save state from the previous player.
            int playbackState = previousPlayer.getPlaybackState();
            if (playbackState != Player.STATE_ENDED) {
                playbackPositionMs = previousPlayer.getCurrentPosition();
                playWhenReady = previousPlayer.getPlayWhenReady();
            }
            previousPlayer.stop(true);
        }

        this.currentPlayer = currentPlayer;

        // Media queue management.
        /*if (currentPlayer == exoPlayer) {
            exoPlayer.prepare(concatenatingMediaSource);
        }*/
        currentPlayer.seekTo(playbackPositionMs);
        currentPlayer.setPlayWhenReady(true);
    }

    private void resetViewCount() {
        View root = getView();
        if (root != null) {
            TextView textViewCount = root.findViewById(R.id.file_view_view_count);
            Helper.setViewText(textViewCount, null);
            Helper.setViewVisibility(textViewCount, View.GONE);
        }
    }
    private void resetFee() {
        View root = getView();
        if (root != null) {
            TextView feeView = root.findViewById(R.id.file_view_fee);
            feeView.setText(null);
            Helper.setViewVisibility(root.findViewById(R.id.file_view_fee_container), View.GONE);
        }
    }

    private void loadViewCount() {
        if (claim != null) {
            FetchStatCountTask task = new FetchStatCountTask(
                    FetchStatCountTask.STAT_VIEW_COUNT, claim.getClaimId(), null, new FetchStatCountTask.FetchStatCountHandler() {
                @Override
                public void onSuccess(int count) {
                    try {
                        String displayText = getResources().getQuantityString(R.plurals.view_count, count, NumberFormat.getInstance().format(count));
                        View root = getView();
                        if (root != null) {
                            TextView textViewCount = root.findViewById(R.id.file_view_view_count);
                            Helper.setViewText(textViewCount, displayText);
                            Helper.setViewVisibility(textViewCount, View.VISIBLE);
                        }
                    } catch (IllegalStateException ex) {
                        // pass
                    }
                }

                @Override
                public void onError(Exception error) {
                    // pass
                }
            });
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private void onMainActionButtonClicked() {
        // Check if the claim is free
        Claim.GenericMetadata metadata = claim.getValue();
        if (metadata instanceof Claim.StreamMetadata) {
            View root = getView();
            if (root != null) {
                root.findViewById(R.id.file_view_main_action_button).setVisibility(View.INVISIBLE);
                root.findViewById(R.id.file_view_main_action_loading).setVisibility(View.VISIBLE);
            }
            if (claim.getFile() == null && !claim.isFree()) {
                if (!Lbry.SDK_READY) {
                    if (root != null) {
                        Snackbar.make(root.findViewById(R.id.file_view_global_layout),
                                R.string.sdk_initializing_functionality, Snackbar.LENGTH_LONG).show();
                    }
                    restoreMainActionButton();
                    return;
                }

                checkAndConfirmPurchaseUrl();
            } else {
                if (claim != null && !claim.isPlayable() && !Lbry.SDK_READY) {
                    if (root != null) {
                        Snackbar.make(root.findViewById(R.id.file_view_global_layout),
                                R.string.sdk_initializing_functionality, Snackbar.LENGTH_LONG).show();
                    }
                    restoreMainActionButton();
                    return;
                }

                handleMainActionForClaim();
            }
        } else {
            showError(getString(R.string.cannot_view_claim));
        }
    }

    private void checkAndConfirmPurchaseUrl() {
        if (claim != null) {
            PurchaseListTask task = new PurchaseListTask(claim.getClaimId(), null, new ClaimSearchResultHandler() {
                @Override
                public void onSuccess(List<Claim> claims, boolean hasReachedEnd) {
                    boolean purchased = false;
                    if (claims.size() == 1) {
                        Claim purchasedClaim = claims.get(0);
                        if (claim.getClaimId().equalsIgnoreCase(purchasedClaim.getClaimId())) {
                            // already purchased
                            purchased = true;
                        }
                    }

                    if (purchased) {
                        handleMainActionForClaim();
                    } else {
                        restoreMainActionButton();
                        confirmPurchaseUrl();
                    }
                }

                @Override
                public void onError(Exception error) {
                    // pass
                    restoreMainActionButton();
                }
            });
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private void confirmPurchaseUrl() {
        if (claim != null) {
            Fee fee = ((Claim.StreamMetadata) claim.getValue()).getFee();
            double cost = claim.getActualCost(Lbryio.LBCUSDRate).doubleValue();
            String formattedCost = Helper.LBC_CURRENCY_FORMAT.format(cost);
            Context context = getContext();
            if (context != null) {
                try {
                    String message = getResources().getQuantityString(
                            R.plurals.confirm_purchase_message,
                            cost == 1 ? 1 : 2,
                            claim.getTitle(),
                            formattedCost.equals("0") ? Helper.FULL_LBC_CURRENCY_FORMAT.format(cost) : formattedCost);
                    AlertDialog.Builder builder = new AlertDialog.Builder(context).
                            setTitle(R.string.confirm_purchase).
                            setMessage(message)
                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Bundle bundle = new Bundle();
                                    bundle.putString("uri", currentUrl);
                                    bundle.putString("paid", "true");
                                    bundle.putDouble("amount", Helper.parseDouble(fee.getAmount(), 0));
                                    bundle.putDouble("lbc_amount", cost);
                                    bundle.putString("currency", fee.getCurrency());
                                    LbryAnalytics.logEvent(LbryAnalytics.EVENT_PURCHASE_URI, bundle);

                                    View root = getView();
                                    if (root != null) {
                                        root.findViewById(R.id.file_view_main_action_button).setVisibility(View.INVISIBLE);
                                        root.findViewById(R.id.file_view_main_action_loading).setVisibility(View.VISIBLE);
                                    }
                                    handleMainActionForClaim();
                                }
                            }).setNegativeButton(R.string.no, null);
                    builder.show();
                } catch (IllegalStateException ex) {
                    // pass
                }
            }
        }
    }

    private void tryOpenFileOrFileGet() {
        if (claim != null) {
            String claimId = claim.getClaimId();
            FileListTask task = new FileListTask(claimId, null, new FileListTask.FileListResultHandler() {
                @Override
                public void onSuccess(List<LbryFile> files, boolean hasReachedEnd) {
                    if (files.size() > 0) {
                        claim.setFile(files.get(0));
                        handleMainActionForClaim();
                        checkIsFileComplete();
                    } else {
                        checkStoragePermissionAndFileGet();
                    }
                }

                @Override
                public void onError(Exception error) {
                    checkStoragePermissionAndFileGet();
                }
            });
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private void handleMainActionForClaim() {
        if (claim.isPlayable() && Lbryio.isSignedIn())  {
            // always use lbry.tv streaming when signed in and playabble
            startTimeMillis = System.currentTimeMillis();
            showExoplayerView();
            playMedia();
            return;
        }

        if (Lbry.SDK_READY) {
            // Check if the file already exists for the claim
            if (claim.getFile() != null) {
                playOrViewMedia();
            } else {
                // check if the file exists from file list
                boolean saveFile = downloadRequested || !claim.isPlayable();
                if (!saveFile) {
                    startTimeMillis = System.currentTimeMillis();
                    fileGet(false);
                    return;
                } else {
                    tryOpenFileOrFileGet();
                }
            }
        } else {
            if (claim.isPlayable()) {
                startTimeMillis = System.currentTimeMillis();
                showExoplayerView();
                playMedia();
            } else {
                Snackbar.make(getView().findViewById(R.id.file_view_global_layout), R.string.sdk_initializing_functionality, Snackbar.LENGTH_LONG).show();
            }
        }
    }

    private void fileGet(boolean save) {
        if (getFileTask != null && getFileTask.getStatus() != AsyncTask.Status.FINISHED) {
            return;
        }
        getFileTask = new GetFileTask(claim.getPermanentUrl(), save, null, new GetFileTask.GetFileHandler() {
            @Override
            public void beforeStart() {

            }

            @Override
            public void onSuccess(LbryFile file, boolean saveFile) {
                // queue the download
                if (claim != null) {
                    if (claim.isFree()) {
                        // paid is handled differently
                        Bundle bundle = new Bundle();
                        bundle.putString("uri", currentUrl);
                        bundle.putString("paid", "false");
                        LbryAnalytics.logEvent(LbryAnalytics.EVENT_PURCHASE_URI, bundle);
                    }

                    if (!claim.isPlayable()) {
                        logFileView(claim.getPermanentUrl(), 0);
                    }

                    claim.setFile(file);
                    if (saveFile) {
                        // download
                        String outpoint = String.format("%s:%d", claim.getTxid(), claim.getNout());
                        Intent intent = new Intent(LbrynetService.ACTION_QUEUE_DOWNLOAD);
                        intent.putExtra("outpoint", outpoint);
                        Context context = getContext();
                        if (context != null) {
                            context.sendBroadcast(intent);
                        }
                    } else {
                        // streaming
                        playOrViewMedia();
                    }
                }
            }

            @Override
            public void onError(Exception error, boolean saveFile) {
                try {
                    showError(getString(R.string.unable_to_view_url, currentUrl));
                    if (saveFile) {
                        onDownloadAborted();
                    }
                    restoreMainActionButton();
                } catch (IllegalStateException ex) {
                    // pass
                }
            }
        });
        getFileTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void playOrViewMedia() {
        boolean handled = false;
        String mediaType = claim.getMediaType();
        if (!Helper.isNullOrEmpty(mediaType)) {
            if (claim.isPlayable()) {
                startTimeMillis = System.currentTimeMillis();
                showExoplayerView();
                playMedia();
                handled = true;
            } else if (claim.isViewable()) {
                // check type and display
                boolean fileExists = false;
                LbryFile claimFile = claim.getFile();
                Uri fileUri  = null;
                if (claimFile != null && !Helper.isNullOrEmpty(claimFile.getDownloadPath())) {
                    File file = new File(claimFile.getDownloadPath());
                    fileUri = Uri.fromFile(file);
                    fileExists = file.exists();
                }
                if (!fileExists) {
                    showError(getString(R.string.claim_file_not_found, claimFile != null ? claimFile.getDownloadPath() : ""));
                } else if (fileUri != null) {
                    View root = getView();
                    Context context = getContext();
                    if (root != null) {
                        if (mediaType.startsWith("image")) {
                            // display the image
                            View container = root.findViewById(R.id.file_view_imageviewer_container);
                            PhotoView photoView = root.findViewById(R.id.file_view_imageviewer);

                            if (context != null) {
                                Glide.with(context.getApplicationContext()).load(fileUri).centerInside().into(photoView);
                            }
                            hideFloatingWalletBalance();
                            container.setVisibility(View.VISIBLE);
                        } else if (mediaType.startsWith("text")) {
                            // show web view (and parse markdown too)
                            View container = root.findViewById(R.id.file_view_webview_container);
                            initWebView(root);
                            applyThemeToWebView();

                            if (Arrays.asList("text/markdown", "text/md").contains(mediaType.toLowerCase())) {
                                loadMarkdownFromFile(claimFile.getDownloadPath());
                            } else {
                                webView.loadUrl(fileUri.toString());
                            }
                            hideFloatingWalletBalance();
                            container.setVisibility(View.VISIBLE);
                        }
                    }
                    handled = true;
                }
            }
        }

        if (!handled) {
            showUnsupportedView();
        }
    }

    private long loadLastPlaybackPosition() {
        long position = -1;
        if (claim != null) {
            String key = String.format("PlayPos_%s", !Helper.isNullOrEmpty(claim.getShortUrl()) ? claim.getShortUrl() : claim.getPermanentUrl());
            Context context = getContext();
            if (context != null) {
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
                position = sp.getLong(key, -1);
            }
        }
        return position;
    }

    private void savePlaybackPosition() {
        if (MainActivity.appPlayer != null && claim != null) {
            String key = String.format("PlayPos_%s", !Helper.isNullOrEmpty(claim.getShortUrl()) ? claim.getShortUrl() : claim.getPermanentUrl());
            long position = MainActivity.appPlayer.getCurrentPosition();
            Context context = getContext();
            if (context != null) {
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
                sp.edit().putLong(key, position).apply();
            }
        }
    }

    private void loadMarkdownFromFile(String filePath) {
        ReadTextFileTask task = new ReadTextFileTask(filePath, new ReadTextFileTask.ReadTextFileHandler() {
            @Override
            public void onSuccess(String text) {
                String html = buildMarkdownHtml(text);
                if (webView != null) {
                    webView.loadData(html, "text/html", "utf-8");
                }
            }

            @Override
            public void onError(Exception error) {
                showError(error.getMessage());
            }
        });
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private String buildMarkdownHtml(String markdown) {
        Parser parser = Parser.builder().build();
        Node document = parser.parse(markdown);
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        String markdownHtml = renderer.render(document);

        return "<!doctype html>\n" +
                "        <html>\n" +
                "          <head>\n" +
                "            <meta charset=\"utf-8\"/>\n" +
                "            <meta name=\"viewport\" content=\"width=device-width, user-scalable=no\"/>\n" +
                "            <style type=\"text/css\">\n" +
                "              body { font-family: 'Inter', sans-serif; margin: 16px }\n" +
                "              img { width: 100%; }\n" +
                "              pre { white-space: pre-wrap; word-wrap: break-word }\n" +
                "            </style>\n" +
                "          </head>\n" +
                "          <body>\n" +
                "            <div id=\"content\">\n" +
                markdownHtml +
                "            </div>\n" +
                "          </body>\n" +
                "        </html>";
    }

    public void showError(String message) {
        View root = getView();
        if (root != null) {
            Snackbar.make(root, message, Snackbar.LENGTH_LONG).setBackgroundTint(Color.RED).setTextColor(Color.WHITE).show();
        }
    }

    private void loadRelatedContent() {
        // reset the list view
        View root = getView();
        if (claim != null && root != null) {
            String title = claim.getTitle();
            String claimId = claim.getClaimId();
            ProgressBar relatedLoading = root.findViewById(R.id.file_view_related_content_progress);
            Context context = getContext();
            boolean canShowMatureContent = false;
            if (context != null) {
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
                canShowMatureContent = sp.getBoolean(MainActivity.PREFERENCE_KEY_SHOW_MATURE_CONTENT, false);
            }

            LighthouseSearchTask relatedTask = new LighthouseSearchTask(
                    title, RELATED_CONTENT_SIZE, 0, canShowMatureContent, claimId, relatedLoading, new ClaimSearchResultHandler() {
                @Override
                public void onSuccess(List<Claim> claims, boolean hasReachedEnd) {
                    List<Claim> filteredClaims = new ArrayList<>();
                    for (Claim c : claims) {
                        if (!c.getClaimId().equalsIgnoreCase(claim.getClaimId())) {
                            filteredClaims.add(c);
                        }
                    }

                    Context ctx = getContext();
                    if (ctx != null) {
                        relatedContentAdapter = new ClaimListAdapter(filteredClaims, ctx);
                        relatedContentAdapter.setListener(new ClaimListAdapter.ClaimListItemListener() {
                            @Override
                            public void onClaimClicked(Claim claim) {
                                if (context instanceof MainActivity) {
                                    MainActivity activity = (MainActivity) context;
                                    if (claim.getName().startsWith("@")) {
                                        activity.openChannelUrl(claim.getPermanentUrl());
                                    } else {
                                        activity.openFileUrl(claim.getPermanentUrl()); //openClaimUrl(claim.getPermanentUrl());
                                    }
                                }
                            }
                        });

                        View v = getView();
                        if (v != null) {
                            RecyclerView relatedContentList = root.findViewById(R.id.file_view_related_content_list);
                            relatedContentList.setAdapter(relatedContentAdapter);
                            relatedContentAdapter.notifyDataSetChanged();

                            Helper.setViewVisibility(
                                    v.findViewById(R.id.file_view_no_related_content),
                                    relatedContentAdapter == null || relatedContentAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
                        }
                    }
                }

                @Override
                public void onError(Exception error) {

                }
            });
            relatedTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private void loadComments() {
        View root = getView();
        ProgressBar relatedLoading = root.findViewById(R.id.file_view_comments_progress);
        if (claim != null && root != null) {
            CommentListTask task = new CommentListTask(1, 500, claim.getClaimId(), relatedLoading, new CommentListHandler() {
                @Override
                public void onSuccess(List<Comment> comments, boolean hasReachedEnd) {
                    Context ctx = getContext();
                    View root = getView();
                    if (ctx != null && root != null) {
                        commentListAdapter = new CommentListAdapter(comments, ctx);
                        commentListAdapter.setListener(new ClaimListAdapter.ClaimListItemListener() {
                            @Override
                            public void onClaimClicked(Claim claim) {
                                if (!Helper.isNullOrEmpty(claim.getName()) &&
                                        claim.getName().startsWith("@") &&
                                        ctx instanceof MainActivity) {
                                    ((MainActivity) ctx).openChannelClaim(claim);
                                }
                            }
                        });
                        commentListAdapter.setReplyListener(new CommentListAdapter.ReplyClickListener() {
                            @Override
                            public void onReplyClicked(Comment comment) {
                                setReplyToComment(comment);
                            }
                        });

                        RecyclerView relatedContentList = root.findViewById(R.id.file_view_comments_list);
                        relatedContentList.setAdapter(commentListAdapter);
                        commentListAdapter.notifyDataSetChanged();

                        checkNoComments();
                        resolveCommentPosters();
                    }
                }

                @Override
                public void onError(Exception error) {
                    // pass
                }
            });
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private void checkNoComments() {
        View root = getView();
        if (root != null) {
            Helper.setViewVisibility(root.findViewById(R.id.file_view_no_comments),
                    commentListAdapter == null || commentListAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
        }
    }

    private void resolveCommentPosters() {
        if (commentListAdapter != null) {
            long st = System.currentTimeMillis();;
            List<String> urlsToResolve = new ArrayList<>(commentListAdapter.getClaimUrlsToResolve());
            if (urlsToResolve.size() > 0) {
                ResolveTask task = new ResolveTask(urlsToResolve, Lbry.SDK_CONNECTION_STRING, null, new ClaimListResultHandler() {
                    @Override
                    public void onSuccess(List<Claim> claims) {
                        if (commentListAdapter != null) {
                            for (Claim claim : claims) {
                                if (claim.getClaimId() != null) {
                                    commentListAdapter.updatePosterForComment(claim.getClaimId(), claim);
                                }
                            }
                            commentListAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onError(Exception error) {
                        // pass
                    }
                });
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }
    }

    public boolean onBackPressed() {
        if (isInFullscreenMode()) {
            disableFullScreenMode();
            return true;
        }

        if (isImageViewerVisible()) {
            View root = getView();
            if (root != null) {
                root.findViewById(R.id.file_view_imageviewer_container).setVisibility(View.GONE);
            }
            restoreMainActionButton();
            showFloatingWalletBalance();
            return true;
        }
        if (isWebViewVisible()) {
            View root = getView();
            if (root != null) {
                root.findViewById(R.id.file_view_webview_container).setVisibility(View.GONE);
                ((RelativeLayout) root.findViewById(R.id.file_view_webview_container)).removeAllViews();
            }
            closeWebView();
            restoreMainActionButton();
            showFloatingWalletBalance();
            return true;
        }

        return false;
    }

    private boolean isImageViewerVisible() {
        View view = getView();
        return view != null && view.findViewById(R.id.file_view_imageviewer_container).getVisibility() == View.VISIBLE;
    }

    private boolean isWebViewVisible() {
        View view = getView();
        return view != null && view.findViewById(R.id.file_view_webview_container).getVisibility() == View.VISIBLE;
    }

    @SuppressLint("SourceLockedOrientationActivity")
    private void enableFullScreenMode() {
        Context context = getContext();
        if (context instanceof MainActivity) {
            View root = getView();
            ConstraintLayout globalLayout = root.findViewById(R.id.file_view_global_layout);
            View exoplayerContainer = root.findViewById(R.id.file_view_exoplayer_container);
            ((ViewGroup) exoplayerContainer.getParent()).removeView(exoplayerContainer);
            globalLayout.addView(exoplayerContainer);

            View playerView = root.findViewById(R.id.file_view_exoplayer_view);
            ((ImageView) playerView.findViewById(R.id.player_image_full_screen_toggle)).setImageResource(R.drawable.ic_fullscreen_exit);

            MainActivity activity = (MainActivity) context;
            activity.enterFullScreenMode();

            int statusBarHeight = activity.getStatusBarHeight();
            exoplayerContainer.setPadding(0, 0, 0, statusBarHeight);

            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    private void disableFullScreenMode() {
        Context context = getContext();
        if (context instanceof MainActivity) {
            MainActivity activity = (MainActivity) context;
            View root = getView();
            RelativeLayout mediaContainer = root.findViewById(R.id.file_view_media_container);
            View exoplayerContainer = root.findViewById(R.id.file_view_exoplayer_container);
            ((ViewGroup) exoplayerContainer.getParent()).removeView(exoplayerContainer);
            mediaContainer.addView(exoplayerContainer);

            View playerView = root.findViewById(R.id.file_view_exoplayer_view);
            ((ImageView) playerView.findViewById(R.id.player_image_full_screen_toggle)).setImageResource(R.drawable.ic_fullscreen);
            exoplayerContainer.setPadding(0, 0, 0, 0);

            activity.exitFullScreenMode();
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }
    }

    private boolean isInFullscreenMode() {
        View view = getView();
        if (view != null) {
            View exoplayerContainer = view.findViewById(R.id.file_view_exoplayer_container);
            return exoplayerContainer.getParent() instanceof ConstraintLayout;
        }
        return false;
    }

    private void scheduleElapsedPlayback() {
        if (!elapsedPlaybackScheduled) {
            elapsedPlaybackScheduler = Executors.newSingleThreadScheduledExecutor();
            elapsedPlaybackScheduler.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    Context context = getContext();
                    if (context instanceof MainActivity) {
                        ((MainActivity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (MainActivity.appPlayer != null) {
                                    elapsedDuration = MainActivity.appPlayer.getCurrentPosition();
                                    int elapsedSeconds = Double.valueOf(elapsedDuration / 1000.0).intValue();
                                    if (elapsedDuration > 0 && elapsedSeconds % 5 == 0 && elapsedSeconds != lastPositionSaved) {
                                        // save playback position every 5 seconds
                                        savePlaybackPosition();
                                        lastPositionSaved = elapsedSeconds;
                                    }

                                    renderElapsedDuration();
                                }
                            }
                        });
                    }
                }
            }, 0, 500, TimeUnit.MILLISECONDS);
            elapsedPlaybackScheduled = true;
        }
    }

    private void resetPlayer() {
        elapsedDuration = 0;
        totalDuration = 0;
        renderElapsedDuration();
        renderTotalDuration();

        elapsedPlaybackScheduled = false;
        if (elapsedPlaybackScheduler != null) {
            elapsedPlaybackScheduler.shutdownNow();
            elapsedPlaybackScheduler = null;
        }

        playbackStarted = false;
        startTimeMillis = 0;
        isPlaying = false;

        if (MainActivity.appPlayer != null) {
            MainActivity.appPlayer.stop(true);
            MainActivity.appPlayer.removeListener(fileViewPlayerListener);
            PlaybackParameters params = new PlaybackParameters(1.0f);
            MainActivity.appPlayer.setPlaybackParameters(params);
        }
    }

    private void showBuffering() {
        View root = getView();
        if (root != null) {
            root.findViewById(R.id.player_buffering_progress).setVisibility(View.VISIBLE);

            PlayerView playerView = root.findViewById(R.id.file_view_exoplayer_view);
            playerView.findViewById(R.id.player_skip_back_10).setVisibility(View.INVISIBLE);
            playerView.findViewById(R.id.player_skip_forward_10).setVisibility(View.INVISIBLE);
        }
    }

    private void hideBuffering() {
        View root = getView();
        if (root != null) {
            root.findViewById(R.id.player_buffering_progress).setVisibility(View.INVISIBLE);

            PlayerView playerView = root.findViewById(R.id.file_view_exoplayer_view);
            playerView.findViewById(R.id.player_skip_back_10).setVisibility(View.VISIBLE);
            playerView.findViewById(R.id.player_skip_forward_10).setVisibility(View.VISIBLE);
        }
    }

    private void renderElapsedDuration() {
        View view = getView();
        if (view != null) {
            Helper.setViewText(view.findViewById(R.id.player_duration_elapsed), Helper.formatDuration(Double.valueOf(elapsedDuration / 1000.0).longValue()));
        }
    }

    private void renderTotalDuration() {
        View view = getView();
        if (view != null) {
            Helper.setViewText(view.findViewById(R.id.player_duration_total), Helper.formatDuration(Double.valueOf(totalDuration / 1000.0).longValue()));
        }
    }

    private void loadAndScheduleDurations() {
        if (MainActivity.appPlayer != null && playbackStarted) {
            elapsedDuration = MainActivity.appPlayer.getCurrentPosition() < 0 ? 0 : MainActivity.appPlayer.getCurrentPosition();
            totalDuration = MainActivity.appPlayer.getDuration() < 0 ? 0 : MainActivity.appPlayer.getDuration();

            renderElapsedDuration();
            renderTotalDuration();
            scheduleElapsedPlayback();
        }
    }

    private void logPlay(String url, long startTimeMillis) {
        long timeToStartMillis = startTimeMillis > 0 ? System.currentTimeMillis() - startTimeMillis : 0;

        Bundle bundle = new Bundle();
        bundle.putString("uri", url);
        bundle.putLong("time_to_start_ms", timeToStartMillis);
        bundle.putLong("time_to_start_seconds", Double.valueOf(timeToStartMillis / 1000.0).longValue());
        LbryAnalytics.logEvent(LbryAnalytics.EVENT_PLAY, bundle);

        logFileView(claim.getPermanentUrl(), timeToStartMillis);
    }

    private void logFileView(String url, long timeToStart) {
        if (claim != null) {
            LogFileViewTask task = new LogFileViewTask(url, claim, timeToStart, new GenericTaskHandler() {
                @Override
                public void beforeStart() { }

                @Override
                public void onSuccess() {
                    claimEligibleRewards();
                }

                @Override
                public void onError(Exception error) { }
            });
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private void checkIsFollowing() {
        if (claim != null && claim.getSigningChannel() != null) {
            boolean isFollowing = Lbryio.isFollowing(claim.getSigningChannel());
            Context context = getContext();
            View root = getView();
            if (context != null && root != null) {
                OutlineIconView iconFollow = root.findViewById(R.id.file_view_icon_follow);
                SolidIconView iconUnfollow = root.findViewById(R.id.file_view_icon_unfollow);
                Helper.setViewVisibility(iconFollow, !isFollowing ? View.VISIBLE: View.INVISIBLE);
                Helper.setViewVisibility(iconUnfollow, isFollowing ? View.VISIBLE : View.INVISIBLE);
            }
        }
    }

    private void claimEligibleRewards() {
        // attempt to claim eligible rewards after viewing or playing a file (fail silently)
        Context context = getContext();
        ClaimRewardTask firstStreamTask = new ClaimRewardTask(Reward.TYPE_FIRST_STREAM, null, null, context, eligibleRewardHandler);
        ClaimRewardTask dailyViewTask = new ClaimRewardTask(Reward.TYPE_DAILY_VIEW, null, null, context, eligibleRewardHandler);
        firstStreamTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        dailyViewTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private ClaimRewardTask.ClaimRewardHandler eligibleRewardHandler = new ClaimRewardTask.ClaimRewardHandler() {
        @Override
        public void onSuccess(double amountClaimed, String message) {
            if (Helper.isNullOrEmpty(message)) {
                message = getResources().getQuantityString(
                        R.plurals.claim_reward_message,
                        amountClaimed == 1 ? 1 : 2,
                        new DecimalFormat(Helper.LBC_CURRENCY_FORMAT_PATTERN).format(amountClaimed));
            }
            Context context = getContext();
            if (context instanceof MainActivity) {
                ((MainActivity) context).showMessage(message);
            }
        }

        @Override
        public void onError(Exception error) {
            // pass
        }
    };

    private void checkIsFileComplete() {
        if (claim == null) {
            return;
        }
        View root = getView();
        if (root != null) {
            if (claim.getFile() != null && claim.getFile().isCompleted()) {
                Helper.setViewVisibility(root.findViewById(R.id.file_view_action_delete), View.VISIBLE);
                Helper.setViewVisibility(root.findViewById(R.id.file_view_action_download), View.GONE);
            } else {
                Helper.setViewVisibility(root.findViewById(R.id.file_view_action_delete), View.GONE);
                Helper.setViewVisibility(root.findViewById(R.id.file_view_action_download), View.VISIBLE);
            }

        }
    }

    private void hideFloatingWalletBalance() {
        Context context = getContext();
        if (context instanceof MainActivity) {
            ((MainActivity) context).hideFloatingWalletBalance();
        }
    }
    private void showFloatingWalletBalance() {
        Context context = getContext();
        if (context instanceof MainActivity) {
            ((MainActivity) context).showFloatingWalletBalance();
        }
    }

    private void toggleCast() {
        if (!MainActivity.castPlayer.isCastSessionAvailable()) {
            showError(getString(R.string.no_cast_session_available));
            return;
        }

        if (currentPlayer == MainActivity.appPlayer) {
            setCurrentPlayer(MainActivity.castPlayer);
        } else {
            setCurrentPlayer(MainActivity.appPlayer);
        }
    }

    private void onDownloadAborted() {
        downloadInProgress = false;

        if (claim != null) {
            claim.setFile(null);
        }
        View root = getView();
        if (root != null) {
            ((ImageView) root.findViewById(R.id.file_view_action_download_icon)).setImageResource(R.drawable.ic_download);
            Helper.setViewVisibility(root.findViewById(R.id.file_view_download_progress), View.GONE);
            Helper.setViewVisibility(root.findViewById(R.id.file_view_unsupported_container), View.GONE);
        }

        checkIsFileComplete();
        restoreMainActionButton();
    }

    private void restoreMainActionButton() {
        View root = getView();
        if (root != null) {
            root.findViewById(R.id.file_view_main_action_loading).setVisibility(View.INVISIBLE);
            root.findViewById(R.id.file_view_main_action_button).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDownloadAction(String downloadAction, String uri, String outpoint, String fileInfoJson, double progress) {
        if (uri == null || outpoint == null || (fileInfoJson == null && !"abort".equals(downloadAction))) {
            return;
        }
        onRelatedDownloadAction(downloadAction, uri, outpoint, fileInfoJson, progress);
        if (claim == null || claim != null && !claim.getPermanentUrl().equalsIgnoreCase(uri)) {
            return;
        }
        if ("abort".equals(downloadAction)) {
            onDownloadAborted();
            return;
        }

        View root = getView();
        if (root != null) {
            ImageView downloadIconView = root.findViewById(R.id.file_view_action_download_icon);
            ProgressBar downloadProgressView = root.findViewById(R.id.file_view_download_progress);

            try {
                JSONObject fileInfo = new JSONObject(fileInfoJson);
                LbryFile claimFile = LbryFile.fromJSONObject(fileInfo);
                claim.setFile(claimFile);

                if (DownloadManager.ACTION_START.equals(downloadAction)) {
                    downloadInProgress = true;
                    Helper.setViewVisibility(downloadProgressView, View.VISIBLE);
                    downloadProgressView.setProgress(0);
                    downloadIconView.setImageResource(R.drawable.ic_stop);
                } else if (DownloadManager.ACTION_UPDATE.equals(downloadAction)) {
                    // handle download updated
                    downloadInProgress = true;
                    Helper.setViewVisibility(downloadProgressView, View.VISIBLE);
                    downloadProgressView.setProgress(Double.valueOf(progress).intValue());
                    downloadIconView.setImageResource(R.drawable.ic_stop);
                } else if (DownloadManager.ACTION_COMPLETE.equals(downloadAction)) {
                    downloadInProgress = false;
                    downloadProgressView.setProgress(100);
                    Helper.setViewVisibility(downloadProgressView, View.GONE);
                    playOrViewMedia();
                }
                checkIsFileComplete();
            } catch (JSONException ex) {
                // invalid file info for download
            }
        }
    }

    @Override
    public void onClaimsFetched(List<Claim> claims) {
        checkOwnClaim();
    }

    @Override
    public void onPortraitOrientationEntered() {
        // Skip this for now. User restores default view mode by pressing fullscreen toggle
        /*Context context = getContext();
        if (context instanceof MainActivity && ((MainActivity) context).isInFullscreenMode()) {
            disableFullScreenMode();
        }*/
    }

    @Override
    public void onLandscapeOrientationEntered() {
        Context context = getContext();
        if (context instanceof MainActivity) {
            MainActivity activity = (MainActivity) context;
            if (activity.isEnteringPIPMode() || activity.isInPictureInPictureMode()) {
                return;
            }
            if (claim != null && claim.isPlayable() && !activity.isInFullscreenMode()) {
                enableFullScreenMode();
            }
        }
    }

    @Override
    public void onWalletBalanceUpdated(WalletBalance walletBalance) {
        if (walletBalance != null && inlineChannelCreatorInlineBalanceValue != null) {
            inlineChannelCreatorInlineBalanceValue.setText(Helper.shortCurrencyFormat(walletBalance.getAvailable().doubleValue()));
        }
        checkRewardsDriver();
    }

    private void checkRewardsDriver() {
        Context ctx = getContext();
        if (ctx != null && claim != null && !claim.isFree() && claim.getFile() == null) {
            String rewardsDriverText = getString(R.string.earn_some_credits_to_access);
            checkRewardsDriverCard(rewardsDriverText, claim.getActualCost(Lbryio.LBCUSDRate).doubleValue());
        }
    }

    private static class LbryWebViewClient extends WebViewClient {
        private Context context;
        public LbryWebViewClient(Context context) {
            this.context = context;
        }
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            Uri url = request.getUrl();
            if (context != null) {
                Intent intent = new Intent(Intent.ACTION_VIEW, url);
                context.startActivity(intent);
            }
            return true;
        }
    }

    private void onRelatedDownloadAction(String downloadAction, String uri, String outpoint, String fileInfoJson, double progress) {
        if ("abort".equals(downloadAction)) {
            if (relatedContentAdapter != null) {
                relatedContentAdapter.clearFileForClaimOrUrl(outpoint, uri);
            }
            return;
        }

        try {
            JSONObject fileInfo = new JSONObject(fileInfoJson);
            LbryFile claimFile = LbryFile.fromJSONObject(fileInfo);
            String claimId = claimFile.getClaimId();
            if (relatedContentAdapter != null) {
                relatedContentAdapter.updateFileForClaimByIdOrUrl(claimFile, claimId, uri);
            }
        } catch (JSONException ex) {
            // invalid file info for download
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        View root = getView();
        if (root != null) {
            float speed = item.getItemId() / 100.0f;
            String speedString = String.format("%sx", new DecimalFormat("0.##").format(speed));
            PlayerView playerView = root.findViewById(R.id.file_view_exoplayer_view);
            ((TextView) playerView.findViewById(R.id.player_playback_speed_label)).setText(speedString);

            if (MainActivity.appPlayer != null) {
                PlaybackParameters params = new PlaybackParameters(speed);
                MainActivity.appPlayer.setPlaybackParameters(params);
            }
        }
        return true;
    }

    @Override
    public boolean shouldHideGlobalPlayer() {
        return true;
    }

    private void checkOwnClaim() {
        if (claim != null) {
            boolean isOwnClaim = Lbry.ownClaims.contains(claim);
            View root = getView();
            if (root != null) {
                Helper.setViewVisibility(root.findViewById(R.id.file_view_action_download), isOwnClaim ? View.GONE : View.VISIBLE);
                Helper.setViewVisibility(root.findViewById(R.id.file_view_action_report), isOwnClaim ? View.GONE : View.VISIBLE);
                Helper.setViewVisibility(root.findViewById(R.id.file_view_action_edit), isOwnClaim ? View.VISIBLE : View.GONE);
                Helper.setViewVisibility(root.findViewById(R.id.file_view_action_delete), isOwnClaim ? View.VISIBLE : View.GONE);
            }
        }
    }

    private static class StreamLoadErrorPolicy extends DefaultLoadErrorHandlingPolicy {
        @Override
        public long getRetryDelayMsFor(int dataType, long loadDurationMs, IOException exception, int errorCount) {
            return exception instanceof ParserException
                    || exception instanceof FileNotFoundException
                    || exception instanceof Loader.UnexpectedLoaderException
                    ? C.TIME_UNSET
                    : Math.min((errorCount - 1) * 1000, 5000);
        }

        @Override
        public int getMinimumLoadableRetryCount(int dataType) {
            return Integer.MAX_VALUE;
        }
    }

    public void onEnterPIPMode() {
        View root = getView();
        if (root != null) {
            PlayerView playerView = root.findViewById(R.id.file_view_exoplayer_view);
            playerView.setVisibility(View.GONE);
        }
    }

    public void onExitPIPMode() {
        View root = getView();
        if (root != null) {
            PlayerView playerView = root.findViewById(R.id.file_view_exoplayer_view);
            playerView.setVisibility(View.VISIBLE);
        }
    }

    private void showStoragePermissionRefusedError() {
        View root = getView();
        if (root != null) {
            Snackbar.make(root, R.string.storage_permission_rationale_download, Snackbar.LENGTH_LONG).
                    setBackgroundTint(Color.RED).setTextColor(Color.WHITE).show();
        }
    }

    private void fetchChannels() {
        if (Lbry.ownChannels != null && Lbry.ownChannels.size() > 0) {
            updateChannelList(Lbry.ownChannels);
            return;
        }

        fetchingChannels = true;
        disableChannelSpinner();
        ClaimListTask task = new ClaimListTask(Claim.TYPE_CHANNEL, progressLoadingChannels, new ClaimListResultHandler() {
            @Override
            public void onSuccess(List<Claim> claims) {
                Lbry.ownChannels = new ArrayList<>(claims);
                updateChannelList(Lbry.ownChannels);
                enableChannelSpinner();
                fetchingChannels = false;
            }

            @Override
            public void onError(Exception error) {
                enableChannelSpinner();
                fetchingChannels = false;
            }
        });
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
    private void disableChannelSpinner() {
        Helper.setViewEnabled(commentChannelSpinner, false);
        hideInlineChannelCreator();
    }
    private void enableChannelSpinner() {
        Helper.setViewEnabled(commentChannelSpinner, true);
        if (commentChannelSpinner != null) {
            Claim selectedClaim = (Claim) commentChannelSpinner.getSelectedItem();
            if (selectedClaim != null) {
                if (selectedClaim.isPlaceholder()) {
                    showInlineChannelCreator();
                } else {
                    hideInlineChannelCreator();
                }
            }
        }
    }
    private void showInlineChannelCreator() {
        Helper.setViewVisibility(inlineChannelCreator, View.VISIBLE);
    }
    private void hideInlineChannelCreator() {
        Helper.setViewVisibility(inlineChannelCreator, View.GONE);
    }

    private void updateChannelList(List<Claim> channels) {
        if (commentChannelSpinnerAdapter == null) {
            Context context = getContext();
            if (context != null) {
                commentChannelSpinnerAdapter = new InlineChannelSpinnerAdapter(context, R.layout.spinner_item_channel, new ArrayList<>(channels));
                commentChannelSpinnerAdapter.addPlaceholder(false);
                commentChannelSpinnerAdapter.notifyDataSetChanged();
            }
        } else {
            commentChannelSpinnerAdapter.clear();
            commentChannelSpinnerAdapter.addAll(channels);
            commentChannelSpinnerAdapter.addPlaceholder(false);
            commentChannelSpinnerAdapter.notifyDataSetChanged();
        }

        if (commentChannelSpinner != null) {
            commentChannelSpinner.setAdapter(commentChannelSpinnerAdapter);
        }

        if (commentChannelSpinnerAdapter != null && commentChannelSpinner != null) {
            if (commentChannelSpinnerAdapter.getCount() > 1) {
                commentChannelSpinner.setSelection(1);
            }
        }
    }

    private void initCommentForm(View root) {
        double amount = Comment.LBC_COST;
        String buttonText = getResources().getQuantityString(R.plurals.post_for_credits, amount == 1 ? 1 : 2, Helper.LBC_CURRENCY_FORMAT.format(amount));
        buttonPostComment.setText(buttonText);
        textCommentLimit.setText(String.format("%d / %d", Helper.getValue(inputComment.getText()).length(), Comment.MAX_LENGTH));

        buttonClearReplyToComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearReplyToComment();
            }
        });

        buttonPostComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!Lbry.SDK_READY) {
                    Snackbar.make(root.findViewById(R.id.file_view_claim_display_area), R.string.sdk_initializing_functionality, Snackbar.LENGTH_LONG).show();
                    return;
                }

                validateAndCheckPostComment(amount);
            }
        });

        inputComment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                int len = charSequence.length();
                textCommentLimit.setText(String.format("%d / %d", len, Comment.MAX_LENGTH));
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        commentChannelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                Object item = adapterView.getItemAtPosition(position);
                if (item instanceof Claim) {
                    Claim claim = (Claim) item;
                    if (claim.isPlaceholder()) {
                        if (!fetchingChannels) {
                            showInlineChannelCreator();
                        }
                    } else {
                        hideInlineChannelCreator();
                        updatePostAsChannel(claim);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void validateAndCheckPostComment(double amount) {
        String comment = Helper.getValue(inputComment.getText());
        Claim channel = (Claim) commentChannelSpinner.getSelectedItem();

        if (Helper.isNullOrEmpty(comment)) {
            showError(getString(R.string.please_enter_comment));
            return;
        }
        if (channel == null || Helper.isNullOrEmpty(channel.getClaimId())) {
            showError(getString(R.string.please_select_channel));
            return;
        }
        if (Lbry.walletBalance == null || amount > Lbry.walletBalance.getAvailable().doubleValue()) {
            showError(getString(R.string.insufficient_balance));
            return;
        }

        Context context = getContext();
        if (context != null) {
            String titleText = getResources().getQuantityString(
                    R.plurals.post_and_tip,
                    amount == 1 ? 1 : 2,
                    Helper.LBC_CURRENCY_FORMAT.format(amount));
            String confirmText = getResources().getQuantityString(
                    R.plurals.confirm_post_comment,
                    amount == 1 ? 1 : 2,
                    Helper.LBC_CURRENCY_FORMAT.format(amount),
                    claim.getTitleOrName());
            AlertDialog.Builder builder = new AlertDialog.Builder(context).
                    setTitle(titleText).
                    setMessage(confirmText)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            postComment(amount);
                        }
                    }).setNegativeButton(R.string.no, null);
            builder.show();
        }
    }

    private void updatePostAsChannel(Claim channel) {
        boolean hasThumbnail = !Helper.isNullOrEmpty(channel.getThumbnailUrl());
        Helper.setViewVisibility(commentPostAsThumbnail, hasThumbnail ? View.VISIBLE : View.INVISIBLE);
        Helper.setViewVisibility(commentPostAsNoThumbnail, !hasThumbnail ? View.VISIBLE : View.INVISIBLE);
        Helper.setViewText(commentPostAsAlpha, channel.getName() != null ? channel.getName().substring(1, 2).toUpperCase() : null);

        Context context = getContext();
        int bgColor = Helper.generateRandomColorForValue(channel.getClaimId());
        Helper.setIconViewBackgroundColor(commentPostAsNoThumbnail, bgColor, false, context);

        if (hasThumbnail && context != null) {
            Glide.with(context.getApplicationContext()).
                    asBitmap().
                    load(channel.getThumbnailUrl()).
                    apply(RequestOptions.circleCropTransform()).
                    into(commentPostAsThumbnail);
        }
    }

    private void beforePostComment() {
        postingComment = true;
        Helper.setViewEnabled(commentChannelSpinner, false);
        Helper.setViewEnabled(inputComment, false);
        Helper.setViewEnabled(buttonClearReplyToComment, false);
        Helper.setViewEnabled(buttonPostComment, false);
    }

    private void afterPostComment() {
        Helper.setViewEnabled(commentChannelSpinner, true);
        Helper.setViewEnabled(inputComment, true);
        Helper.setViewEnabled(buttonClearReplyToComment, true);
        Helper.setViewEnabled(buttonPostComment, true);
        postingComment = false;
    }

    private Comment buildPostComment() {
        Comment comment = new Comment();
        Claim channel = (Claim) commentChannelSpinner.getSelectedItem();
        comment.setClaimId(claim.getClaimId());
        comment.setChannelId(channel.getClaimId());
        comment.setChannelName(channel.getName());
        comment.setText(Helper.getValue(inputComment.getText()));
        comment.setPoster(channel);
        if (replyToComment != null) {
            comment.setParentId(replyToComment.getId());
        }

        return comment;
    }

    private void setReplyToComment(Comment comment) {
        replyToComment = comment;
        Helper.setViewText(textReplyingTo, getString(R.string.replying_to, comment.getChannelName()));
        Helper.setViewText(textReplyToBody, comment.getText());
        Helper.setViewVisibility(containerReplyToComment, View.VISIBLE);

        inputComment.requestFocus();
        Context context = getContext();
        if (context != null) {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(inputComment, InputMethodManager.SHOW_FORCED);
        }
    }

    private void clearReplyToComment() {
        Helper.setViewText(textReplyingTo, null);
        Helper.setViewText(textReplyToBody, null);
        Helper.setViewVisibility(containerReplyToComment, View.GONE);
        replyToComment = null;
    }

    private void postComment(double tipAmount) {
        if (postingComment) {
            return;
        }

        Comment comment = buildPostComment();
        // only use 2 decimal places
        BigDecimal amount = new BigDecimal(String.valueOf(tipAmount));

        beforePostComment();
        CommentCreateWithTipTask task = new CommentCreateWithTipTask(comment, amount, progressPostComment, new CommentCreateWithTipTask.CommentCreateWithTipHandler() {
            @Override
            public void onSuccess(Comment createdComment) {
                inputComment.setText(null);
                clearReplyToComment();

                if (commentListAdapter != null) {
                    createdComment.setPoster(comment.getPoster());
                    if (!Helper.isNullOrEmpty(createdComment.getParentId())) {
                        commentListAdapter.addReply(createdComment);
                    } else {
                        commentListAdapter.insert(0, createdComment);
                    }
                }
                afterPostComment();
                checkNoComments();

                Bundle bundle = new Bundle();
                bundle.putDouble("amount", amount.doubleValue());
                bundle.putString("claim_id", claim != null ? claim.getClaimId() : null);
                bundle.putString("claim_name", claim != null ? claim.getName() : null);
                LbryAnalytics.logEvent(LbryAnalytics.EVENT_COMMENT_CREATE, bundle);

                Context context = getContext();
                if (context instanceof MainActivity) {
                    ((MainActivity) context).showMessage(R.string.comment_posted);
                }
            }

            @Override
            public void onError(Exception error) {
                try {
                    showError(error != null ? error.getMessage() : getString(R.string.comment_error));
                } catch (IllegalStateException ex) {
                    // pass
                }
                afterPostComment();
            }
        });
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}
