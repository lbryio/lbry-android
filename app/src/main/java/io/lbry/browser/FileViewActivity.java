package io.lbry.browser;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.PictureInPictureParams;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ext.cast.CastPlayer;
import com.google.android.exoplayer2.ext.cast.SessionAvailabilityListener;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import io.lbry.browser.adapter.ClaimListAdapter;
import io.lbry.browser.adapter.TagListAdapter;
import io.lbry.browser.dialog.RepostClaimDialogFragment;
import io.lbry.browser.dialog.SendTipDialogFragment;
import io.lbry.browser.exceptions.LbryUriException;
import io.lbry.browser.listener.WalletBalanceListener;
import io.lbry.browser.model.Claim;
import io.lbry.browser.model.ClaimCacheKey;
import io.lbry.browser.model.Fee;
import io.lbry.browser.model.LbryFile;
import io.lbry.browser.model.Tag;
import io.lbry.browser.model.UrlSuggestion;
import io.lbry.browser.model.lbryinc.Reward;
import io.lbry.browser.model.lbryinc.Subscription;
import io.lbry.browser.tasks.ReadTextFileTask;
import io.lbry.browser.tasks.claim.ClaimListResultHandler;
import io.lbry.browser.tasks.claim.ClaimSearchTask;
import io.lbry.browser.tasks.file.DeleteFileTask;
import io.lbry.browser.tasks.file.FileListTask;
import io.lbry.browser.tasks.GenericTaskHandler;
import io.lbry.browser.tasks.file.GetFileTask;
import io.lbry.browser.tasks.LighthouseSearchTask;
import io.lbry.browser.tasks.claim.ResolveTask;
import io.lbry.browser.tasks.lbryinc.ChannelSubscribeTask;
import io.lbry.browser.tasks.lbryinc.ClaimRewardTask;
import io.lbry.browser.tasks.lbryinc.FetchStatCountTask;
import io.lbry.browser.tasks.lbryinc.LogFileViewTask;
import io.lbry.browser.ui.controls.SolidIconView;
import io.lbry.browser.ui.following.FollowingFragment;
import io.lbry.browser.utils.Helper;
import io.lbry.browser.utils.Lbry;
import io.lbry.browser.utils.LbryAnalytics;
import io.lbry.browser.utils.LbryUri;
import io.lbry.browser.utils.Lbryio;
import io.lbry.lbrysdk.DownloadManager;
import io.lbry.lbrysdk.LbrynetService;

public class FileViewActivity extends AppCompatActivity {

    public static FileViewActivity instance = null;
    private static final int RELATED_CONTENT_SIZE = 16;
    private static boolean startingShareActivity;

    private PlayerControlView castControlView;
    private Player currentPlayer;
    private boolean backStackLost;
    private boolean loadingNewClaim;
    private boolean stopServiceReceived;
    private boolean downloadInProgress;
    private boolean downloadRequested;
    private boolean walletBalanceInitialized;
    private boolean inPictureInPictureMode;
    private boolean hasLoadedFirstBalance;
    private boolean loadFilePending;
    private boolean resolving;
    private boolean initialFileLoadDone;
    private Claim claim;
    private String currentUrl;
    private ClaimListAdapter relatedContentAdapter;
    private BroadcastReceiver sdkReceiver;
    private Player.EventListener fileViewPlayerListener;

    private long elapsedDuration = 0;
    private long totalDuration = 0;
    private boolean elapsedPlaybackScheduled;
    private ScheduledExecutorService elapsedPlaybackScheduler;
    private boolean playbackStarted;
    private long startTimeMillis;
    private GetFileTask getFileTask;

    private List<WalletBalanceListener> walletBalanceListeners;
    private BroadcastReceiver downloadEventReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String claimId = null;
        String url = null;
        Intent intent = getIntent();
        if (intent != null) {
            claimId = intent.getStringExtra("claimId");
            url = intent.getStringExtra("url");
        }
        if (Helper.isNullOrEmpty(url)) {
            // This activity should not be opened without a url set
            finish();
            return;
        }

        instance = this;
        ClaimCacheKey key = new ClaimCacheKey();
        key.setClaimId(claimId);
        key.setUrl(url); // use the same url for the key so that we can match the key for any value that's the same

        if (Lbry.claimCache.containsKey(key)) {
            claim = Lbry.claimCache.get(key);
            checkAndResetNowPlayingClaim();
            if (claim.getFile() == null) {
                loadFile();
            }
        }

        setContentView(R.layout.activity_file_view);
        checkIsFileComplete();

        currentUrl = url;
        logUrlEvent(url);
        Helper.saveUrlHistory(url, claim != null ? claim.getTitle() : null, UrlSuggestion.TYPE_FILE);
        if (claim == null) {
            MainActivity.clearNowPlayingClaim(this);
            resolveUrl(url);
        }

        walletBalanceListeners = new ArrayList<>();
        registerDownloadEventReceiver();
        registerSdkReceiver();

        fileViewPlayerListener = new Player.EventListener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if (playbackState == Player.STATE_READY) {
                    if (totalDuration == 0) {
                        elapsedDuration = MainActivity.appPlayer.getCurrentPosition();
                        totalDuration = MainActivity.appPlayer.getDuration();
                    }
                    if (!playbackStarted) {
                        logPlay(currentUrl, startTimeMillis);
                        playbackStarted = true;
                    }
                    renderTotalDuration();
                    scheduleElapsedPlayback();
                    hideBuffering();

                    if (loadingNewClaim) {
                        MainActivity.appPlayer.setPlayWhenReady(true);
                        loadingNewClaim = false;
                    }
                } else if (playbackState == Player.STATE_BUFFERING) {
                    showBuffering();
                } else {
                    hideBuffering();
                }
            }
        };

        castControlView = findViewById(R.id.file_view_cast_control_view);
        initUi();
        onWalletBalanceUpdated();
        renderClaim();
    }

    public void addWalletBalanceListener(WalletBalanceListener listener) {
        if (!walletBalanceListeners.contains(listener)) {
            walletBalanceListeners.add(listener);
        }
    }

    public void removeWalletBalanceListener(WalletBalanceListener listener) {
        walletBalanceListeners.remove(listener);
    }

    private void initWebView() {
        WebView webView = findViewById(R.id.file_view_webview);
        webView.setWebViewClient(new LbryWebViewClient(this));
        WebSettings webSettings = webView.getSettings();
        webSettings.setAllowFileAccess(true);
        webSettings.setJavaScriptEnabled(true);
    }

    private void logUrlEvent(String url) {
        Bundle bundle = new Bundle();
        bundle.putString("uri", url);
        LbryAnalytics.logEvent(LbryAnalytics.EVENT_OPEN_FILE_PAGE, bundle);
    }

    private void checkAndResetNowPlayingClaim() {
        if (MainActivity.nowPlayingClaim != null &&
                !MainActivity.nowPlayingClaim.getClaimId().equalsIgnoreCase(claim.getClaimId())) {
            MainActivity.clearNowPlayingClaim(this);
        }
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        MainActivity.startingFileViewActivity = false;
        if (intent != null) {
            String newClaimId = intent.getStringExtra("claimId");
            String newUrl = intent.getStringExtra("url");

            String oldClaimId = claim != null ? claim.getClaimId() : null;
            if (!Helper.isNullOrEmpty(newClaimId)) {
                if (newClaimId.equalsIgnoreCase(oldClaimId)) {
                    // it's the same claim, so we do nothing
                    if (MainActivity.appPlayer != null) {
                        PlayerView view = findViewById(R.id.file_view_exoplayer_view);
                        view.setPlayer(null);
                        view.setPlayer(MainActivity.appPlayer);
                    }

                    return;
                }

                onNewClaim(newUrl);
                ClaimCacheKey key = new ClaimCacheKey();
                key.setClaimId(newClaimId);
                if (!Helper.isNullOrEmpty(newUrl) && newUrl.contains("#")) {
                    key.setUrl(newUrl);
                }
                loadClaimForCacheKey(key, newUrl);
            } else if (!Helper.isNullOrEmpty(newUrl)) {
                if (currentUrl != null && currentUrl.equalsIgnoreCase(newUrl)) {
                    return;
                }

                onNewClaim(newUrl);
                ClaimCacheKey key = new ClaimCacheKey();
                key.setUrl(newUrl);
                loadClaimForCacheKey(key, newUrl);
            }
        }
    }

    private void onNewClaim(String url) {
        loadingNewClaim = true;
        initialFileLoadDone = false;
        currentUrl = url;
        logUrlEvent(url);
        resetViewCount();

        if (MainActivity.appPlayer != null) {
            MainActivity.appPlayer.setPlayWhenReady(false);
        }
    }

    private void loadClaimForCacheKey(ClaimCacheKey key, String url) {
        if (Lbry.claimCache.containsKey(key)) {
            claim = Lbry.claimCache.get(key);
            Helper.saveUrlHistory(url, claim.getTitle(), UrlSuggestion.TYPE_FILE);
            checkAndResetNowPlayingClaim();
            if (claim.getFile() == null) {
                loadFile();
            } else {
                initialFileLoadDone = true;
                checkInitialFileLoadDone();
            }
            renderClaim();
        } else {
            Helper.saveUrlHistory(url, null, UrlSuggestion.TYPE_FILE);
            findViewById(R.id.file_view_claim_display_area).setVisibility(View.INVISIBLE);
            MainActivity.clearNowPlayingClaim(this);
            resolveUrl(url);
        }
    }

    private void registerSdkReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(LbrynetService.ACTION_STOP_SERVICE);
        filter.addAction(MainActivity.ACTION_SDK_READY);
        filter.addAction(MainActivity.ACTION_WALLET_BALANCE_UPDATED);
        sdkReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (LbrynetService.ACTION_STOP_SERVICE.equalsIgnoreCase(action)) {
                    stopServiceReceived = true;
                    finish();
                } else if (MainActivity.ACTION_SDK_READY.equalsIgnoreCase(action)) {
                    if (loadFilePending) {
                        loadFile();
                    }

                    initFloatingWalletBalance();
                } else if (MainActivity.ACTION_WALLET_BALANCE_UPDATED.equalsIgnoreCase(action)) {
                    onWalletBalanceUpdated();
                }
            }
        };
        registerReceiver(sdkReceiver, filter);
    }

    private void initFloatingWalletBalance() {
        if (walletBalanceInitialized) {
            return;
        }
        findViewById(R.id.floating_balance_container).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendBroadcast(new Intent(MainActivity.ACTION_OPEN_WALLET_PAGE));
                bringMainTaskToFront();
                finish();
            }
        });
        walletBalanceInitialized = true;
    }

    private void onWalletBalanceUpdated() {
        if (Lbry.SDK_READY) {
            if (!hasLoadedFirstBalance) {
                findViewById(R.id.floating_balance_loading).setVisibility(View.GONE);
                findViewById(R.id.floating_balance_value).setVisibility(View.VISIBLE);
                hasLoadedFirstBalance = true;
            }

            ((TextView) findViewById(R.id.floating_balance_value)).setText(
                    Helper.shortCurrencyFormat(Lbry.walletBalance.getAvailable().doubleValue()));

            for (WalletBalanceListener listener : walletBalanceListeners) {
                if (listener != null) {
                    listener.onWalletBalanceUpdated(Lbry.walletBalance);
                }
            }
        }
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

            if (!Helper.isNullOrEmpty(lbryFile.getStreamingUrl())) {
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
            public void onSuccess(List<LbryFile> files) {
                if (files.size() > 0) {
                    claim.setFile(files.get(0));
                    checkIsFileComplete();
                }
                initialFileLoadDone = true;
                checkInitialFileLoadDone();
            }

            @Override
            public void onError(Exception error) {
                initialFileLoadDone = true;
                checkInitialFileLoadDone();
            }
        });
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void checkInitialFileLoadDone() {
        if (initialFileLoadDone) {
            restoreMainActionButton();
        }
        if (claim != null && claim.isFree()) {
            onMainActionButtonClicked();
        }
    }

    protected void onResume() {
        super.onResume();
        MainActivity.mainActive = false;
        MainActivity.startingFileViewActivity = false;

        loadAndScheduleDurations();
        if (Lbry.SDK_READY) {
            initFloatingWalletBalance();
        }
    }

    private void resolveUrl(String url) {
        resolving = true;
        findViewById(R.id.file_view_claim_display_area).setVisibility(View.INVISIBLE);
        View loadingView = findViewById(R.id.file_view_loading_container);
        ResolveTask task = new ResolveTask(url, Lbry.LBRY_TV_CONNECTION_STRING, loadingView, new ClaimListResultHandler() {
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
                            Intent intent = new Intent(MainActivity.ACTION_OPEN_CHANNEL_URL);
                            intent.putExtra("url", !Helper.isNullOrEmpty(claim.getShortUrl()) ? claim.getShortUrl() : claim.getPermanentUrl());
                            sendBroadcast(intent);

                            bringMainTaskToFront();
                            finish();
                            return;
                        }
                    } else {
                        Lbry.addClaimToCache(claim);
                    }

                    Helper.saveUrlHistory(url, claim.getTitle(), UrlSuggestion.TYPE_FILE);

                    // also save view history
                    checkAndResetNowPlayingClaim();
                    loadFile();
                    renderClaim();
                } else {
                    // render nothing at location
                }
            }

            @Override
            public void onError(Exception error) {
                resolving = false;
            }
        });
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void initUi() {
        initWebView();

        findViewById(R.id.file_view_title_area).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImageView descIndicator = findViewById(R.id.file_view_desc_toggle_arrow);
                View descriptionArea = findViewById(R.id.file_view_description_area);
                if (descriptionArea.getVisibility() != View.VISIBLE) {
                    descriptionArea.setVisibility(View.VISIBLE);
                    descIndicator.setImageResource(R.drawable.ic_arrow_dropup);
                } else {
                    descriptionArea.setVisibility(View.GONE);
                    descIndicator.setImageResource(R.drawable.ic_arrow_dropdown);
                }
            }
        });

        findViewById(R.id.file_view_action_share).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (claim != null) {
                    try {
                        String shareUrl = LbryUri.parse(
                                !Helper.isNullOrEmpty(claim.getShortUrl()) ? claim.getShortUrl() : claim.getPermanentUrl()).toTvString();
                        Intent shareIntent = new Intent();
                        shareIntent.setAction(Intent.ACTION_SEND);
                        shareIntent.setType("text/plain");
                        shareIntent.putExtra(Intent.EXTRA_TEXT, shareUrl);

                        startingShareActivity = true;
                        Intent shareUrlIntent = Intent.createChooser(shareIntent, getString(R.string.share_lbry_content));
                        shareUrlIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(shareUrlIntent);
                    } catch (LbryUriException ex) {
                        // pass
                    }
                }
            }
        });

        findViewById(R.id.file_view_action_tip).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!Lbry.SDK_READY) {
                    Snackbar.make(findViewById(R.id.file_view_claim_display_area), R.string.sdk_initializing_functionality, Snackbar.LENGTH_LONG).show();
                    return;
                }

                if (claim != null) {
                    SendTipDialogFragment dialog = SendTipDialogFragment.newInstance();
                    dialog.setClaim(claim);
                    dialog.setListener(new SendTipDialogFragment.SendTipListener() {
                        @Override
                        public void onTipSent(BigDecimal amount) {
                            double sentAmount = amount.doubleValue();
                            String message = getResources().getQuantityString(
                                    R.plurals.you_sent_a_tip, sentAmount == 1.0 ? 1 : 2,
                                    new DecimalFormat("#,###.##").format(sentAmount));
                            Snackbar.make(findViewById(R.id.file_view_claim_display_area), message, Snackbar.LENGTH_LONG).show();
                        }
                    });
                    dialog.show(getSupportFragmentManager(), SendTipDialogFragment.TAG);
                }
            }
        });

        findViewById(R.id.file_view_action_repost).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!Lbry.SDK_READY) {
                    Snackbar.make(findViewById(R.id.file_view_claim_display_area), R.string.sdk_initializing_functionality, Snackbar.LENGTH_LONG).show();
                    return;
                }

                if (claim != null) {
                    RepostClaimDialogFragment dialog = RepostClaimDialogFragment.newInstance();
                    dialog.setClaim(claim);
                    dialog.setListener(new RepostClaimDialogFragment.RepostClaimListener() {
                        @Override
                        public void onClaimReposted(Claim claim) {
                            Snackbar.make(findViewById(R.id.file_view_claim_display_area), R.string.content_successfully_reposted, Snackbar.LENGTH_LONG).show();
                        }
                    });
                    dialog.show(getSupportFragmentManager(), RepostClaimDialogFragment.TAG);
                }
            }
        });

        findViewById(R.id.file_view_action_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!Lbry.SDK_READY) {
                    Snackbar.make(findViewById(R.id.file_view_claim_display_area), R.string.sdk_initializing_functionality, Snackbar.LENGTH_LONG).show();
                    return;
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(FileViewActivity.this).
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
        });

        findViewById(R.id.file_view_action_download).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!Lbry.SDK_READY) {
                    Snackbar.make(findViewById(R.id.file_view_claim_display_area), R.string.sdk_initializing_functionality, Snackbar.LENGTH_LONG).show();
                    return;
                }

                if (claim != null) {
                    if (downloadInProgress) {
                        onDownloadAborted();

                        // file is already downloading and not completed
                        Intent intent = new Intent(LbrynetService.ACTION_DELETE_DOWNLOAD);
                        intent.putExtra("uri", claim.getPermanentUrl());
                        intent.putExtra("nativeDelete", true);
                        sendBroadcast(intent);
                    } else {
                        downloadInProgress = true;
                        Helper.setViewVisibility(findViewById(R.id.file_view_download_progress), View.VISIBLE);
                        ((ImageView) findViewById(R.id.file_view_action_download_icon)).setImageResource(R.drawable.ic_stop);

                        if (!claim.isFree()) {
                            downloadRequested = true;
                            onMainActionButtonClicked();
                        } else {
                            // download the file
                            fileGet(true);
                        }
                    }
                }
            }
        });

        findViewById(R.id.file_view_action_report).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (claim != null) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("https://lbry.com/dmca/%s", claim.getClaimId())));
                    startActivity(intent);
                }
            }
        });

        findViewById(R.id.player_toggle_cast).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleCast();
            }
        });

        findViewById(R.id.player_toggle_fullscreen).setOnClickListener(new View.OnClickListener() {
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

        findViewById(R.id.file_view_publisher_name).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (claim != null && claim.getSigningChannel() != null) {
                    Claim publisher = claim.getSigningChannel();
                    Intent intent = new Intent(MainActivity.ACTION_OPEN_CHANNEL_URL);
                    intent.putExtra("url", !Helper.isNullOrEmpty(publisher.getShortUrl()) ? publisher.getShortUrl() : publisher.getPermanentUrl());
                    sendBroadcast(intent);
                    bringMainTaskToFront();
                    finish();
                }
            }
        });

        View buttonFollowUnfollow = findViewById(R.id.file_view_icon_follow_unfollow);
        buttonFollowUnfollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (claim != null && claim.getSigningChannel() != null) {
                    Claim publisher = claim.getSigningChannel();
                    boolean isFollowing = Lbryio.isFollowing(publisher);
                    Subscription subscription = Subscription.fromClaim(publisher);
                    buttonFollowUnfollow.setEnabled(false);
                    new ChannelSubscribeTask(FileViewActivity.this, publisher.getClaimId(), subscription, isFollowing, new ChannelSubscribeTask.ChannelSubscribeHandler() {
                        @Override
                        public void onSuccess() {
                            if (isFollowing) {
                                Lbryio.removeSubscription(subscription);
                                Lbryio.removeCachedResolvedSubscription(publisher);
                            } else {
                                Lbryio.addSubscription(subscription);
                                Lbryio.addCachedResolvedSubscription(publisher);
                            }
                            buttonFollowUnfollow.setEnabled(true);
                            checkIsFollowing();
                            FollowingFragment.resetClaimSearchContent = true;

                            // Save shared user state
                            sendBroadcast(new Intent(MainActivity.ACTION_SAVE_SHARED_USER_STATE));
                        }

                        @Override
                        public void onError(Exception exception) {
                            buttonFollowUnfollow.setEnabled(true);
                        }
                    }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            }
        });

        RecyclerView relatedContentList = findViewById(R.id.file_view_related_content_list);
        relatedContentList.setNestedScrollingEnabled(false);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        relatedContentList.setLayoutManager(llm);
    }

    private void deleteClaimFile() {
        if (claim != null) {
            View actionDelete = findViewById(R.id.file_view_action_delete);
            DeleteFileTask task = new DeleteFileTask(claim.getClaimId(), new GenericTaskHandler() {
                @Override
                public void beforeStart() {
                    actionDelete.setEnabled(false);
                }

                @Override
                public void onSuccess() {
                    actionDelete.setVisibility(View.GONE);
                    findViewById(R.id.file_view_action_download).setVisibility(View.VISIBLE);
                    findViewById(R.id.file_view_unsupported_container).setVisibility(View.GONE);
                    actionDelete.setEnabled(true);
                    restoreMainActionButton();
                }

                @Override
                public void onError(Exception error) {
                    actionDelete.setEnabled(true);
                    showError(error.getMessage());
                }
            });
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private void renderClaim() {
        if (claim == null) {
            return;
        }

        loadViewCount();
        checkIsFollowing();

        ((NestedScrollView) findViewById(R.id.file_view_scroll_view)).scrollTo(0, 0);
        findViewById(R.id.file_view_claim_display_area).setVisibility(View.VISIBLE);

        ImageView descIndicator = findViewById(R.id.file_view_desc_toggle_arrow);
        descIndicator.setImageResource(R.drawable.ic_arrow_dropdown);

        findViewById(R.id.file_view_description_area).setVisibility(View.GONE);
        ((TextView) findViewById(R.id.file_view_title)).setText(claim.getTitle());
        ((TextView) findViewById(R.id.file_view_description)).setText(claim.getDescription());
        ((TextView) findViewById(R.id.file_view_publisher_name)).setText(
                Helper.isNullOrEmpty(claim.getPublisherName()) ? getString(R.string.anonymous) : claim.getPublisherName());

        RecyclerView descTagsList = findViewById(R.id.file_view_tag_list);
        FlexboxLayoutManager flm = new FlexboxLayoutManager(this);
        descTagsList.setLayoutManager(flm);

        List<Tag> tags = claim.getTagObjects();
        TagListAdapter tagListAdapter = new TagListAdapter(tags, this);
        tagListAdapter.setClickListener(new TagListAdapter.TagClickListener() {
            @Override
            public void onTagClicked(Tag tag, int customizeMode) {
                if (customizeMode == TagListAdapter.CUSTOMIZE_MODE_NONE) {
                    Intent intent = new Intent(MainActivity.ACTION_OPEN_ALL_CONTENT_TAG);
                    intent.putExtra("tag", tag.getName());
                    sendBroadcast(intent);
                    bringMainTaskToFront();
                    finish();
                }
            }
        });
        descTagsList.setAdapter(tagListAdapter);
        findViewById(R.id.file_view_tag_area).setVisibility(tags.size() > 0 ? View.VISIBLE : View.GONE);

        findViewById(R.id.file_view_exoplayer_container).setVisibility(View.GONE);
        findViewById(R.id.file_view_unsupported_container).setVisibility(View.GONE);
        findViewById(R.id.file_view_media_meta_container).setVisibility(View.VISIBLE);

        Claim.GenericMetadata metadata = claim.getValue();
        if (!Helper.isNullOrEmpty(claim.getThumbnailUrl())) {
            ImageView thumbnailView = findViewById(R.id.file_view_thumbnail);
            Glide.with(getApplicationContext()).load(claim.getThumbnailUrl()).centerCrop().into(thumbnailView);
        } else {
            // display first x letters of claim name, with random background
        }

        findViewById(R.id.file_view_main_action_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onMainActionButtonClicked();
            }
        });
        findViewById(R.id.file_view_media_meta_container).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onMainActionButtonClicked();
            }
        });

        if (metadata instanceof Claim.StreamMetadata) {
            Claim.StreamMetadata streamMetadata = (Claim.StreamMetadata) metadata;
            long publishTime = streamMetadata.getReleaseTime() > 0 ? streamMetadata.getReleaseTime() * 1000 : claim.getTimestamp() * 1000;
            ((TextView) findViewById(R.id.file_view_publish_time)).setText(DateUtils.getRelativeTimeSpanString(
                    publishTime, System.currentTimeMillis(), 0, DateUtils.FORMAT_ABBREV_RELATIVE));

            Fee fee = streamMetadata.getFee();
            if (fee != null && Helper.parseDouble(fee.getAmount(), 0) > 0) {
                findViewById(R.id.file_view_fee_container).setVisibility(View.VISIBLE);
                ((TextView) findViewById(R.id.file_view_fee)).setText(
                        Helper.shortCurrencyFormat(claim.getActualCost(Lbryio.LBCUSDRate).divide(new BigDecimal(100000000)).doubleValue()));
            }
        }

        findViewById(R.id.file_view_icon_follow_unfollow).setVisibility(claim.getSigningChannel() != null ? View.VISIBLE : View.GONE);

        MaterialButton mainActionButton = findViewById(R.id.file_view_main_action_button);
        if (claim.isPlayable()) {
            mainActionButton.setText(R.string.play);
        } else if (claim.isViewable()) {
            mainActionButton.setText(R.string.view);
        } else {
            mainActionButton.setText(R.string.download);
        }

        if (claim.isFree()) {
            if (claim.isPlayable() || (claim.isViewable() && Lbry.SDK_READY)) {
                onMainActionButtonClicked();
            }
        }

        loadRelatedContent();
    }

    private void showUnsupportedView() {
        findViewById(R.id.file_view_exoplayer_container).setVisibility(View.GONE);
        findViewById(R.id.file_view_unsupported_container).setVisibility(View.VISIBLE);
        String fileNameString = "";
        if (claim.getFile() != null) {
            LbryFile lbryFile = claim.getFile();
            File file = new File(lbryFile.getDownloadPath());
            fileNameString = String.format("\"%s\" ", file.getName());
        }
        ((TextView) findViewById(R.id.file_view_unsupported_text)).setText(getString(R.string.unsupported_content_desc, fileNameString));
    }

    private void showExoplayerView() {
        findViewById(R.id.file_view_unsupported_container).setVisibility(View.GONE);
        findViewById(R.id.file_view_exoplayer_container).setVisibility(View.VISIBLE);
    }

    private void playMedia() {
        boolean newPlayerCreated = false;
        if (MainActivity.appPlayer == null) {
            MainActivity.appPlayer = new SimpleExoPlayer.Builder(this).build();
            MainActivity.castPlayer = new CastPlayer(MainActivity.castContext);

            newPlayerCreated = true;
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        PlayerView view = findViewById(R.id.file_view_exoplayer_view);
        view.setPlayer(MainActivity.appPlayer);
        if (MainActivity.nowPlayingClaim != null &&
                MainActivity.nowPlayingClaim.getClaimId().equalsIgnoreCase(claim.getClaimId()) &&
                !newPlayerCreated) {
            // if the claim is already playing, we don't need to reload the media source
            return;
        }

        resetPlayer();
        showBuffering();

        MainActivity.appPlayer.addListener(fileViewPlayerListener);
        MainActivity.castPlayer.addListener(fileViewPlayerListener);
        MainActivity.castPlayer.setSessionAvailabilityListener(new SessionAvailabilityListener() {
            @Override
            public void onCastSessionAvailable() {
                setCurrentPlayer(MainActivity.castPlayer);
            }

            @Override
            public void onCastSessionUnavailable() {
                setCurrentPlayer(MainActivity.appPlayer);
            }
        });

        castControlView.setPlayer(MainActivity.castPlayer);
        MainActivity.setNowPlayingClaim(claim, FileViewActivity.this);
        String userAgent = Util.getUserAgent(this, getString(R.string.app_name));

        String mediaSourceUrl = getStreamingUrl();
        MediaSource mediaSource = new ProgressiveMediaSource.Factory(
                new DefaultDataSourceFactory(this, userAgent),
                new DefaultExtractorsFactory()
        ).createMediaSource(Uri.parse(mediaSourceUrl));
        MainActivity.appPlayer.setPlayWhenReady(true);
        MainActivity.appPlayer.prepare(mediaSource, true, true);
    }

    private void setCurrentPlayer(Player currentPlayer) {
        if (this.currentPlayer == currentPlayer) {
            return;
        }

        // View management.
        if (currentPlayer == MainActivity.appPlayer) {
            //localPlayerView.setVisibility(View.VISIBLE);
            castControlView.hide();
            ((ImageView) findViewById(R.id.player_image_cast_toggle)).setImageResource(R.drawable.ic_cast);
        } else /* currentPlayer == castPlayer */ {
            castControlView.show();
            ((ImageView) findViewById(R.id.player_image_cast_toggle)).setImageResource(R.drawable.ic_cast_connected);
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
        TextView textViewCount = findViewById(R.id.file_view_view_count);
        Helper.setViewText(textViewCount, null);
        Helper.setViewVisibility(textViewCount, View.GONE);
    }

    private void loadViewCount() {
        if (claim != null) {
            FetchStatCountTask task = new FetchStatCountTask(
                    FetchStatCountTask.STAT_VIEW_COUNT, claim.getClaimId(), null, new FetchStatCountTask.FetchStatCountHandler() {
                @Override
                public void onSuccess(int count) {
                    try {
                        String displayText = getResources().getQuantityString(R.plurals.view_count, count, NumberFormat.getInstance().format(count));
                        TextView textViewCount = findViewById(R.id.file_view_view_count);
                        Helper.setViewText(textViewCount, displayText);
                        Helper.setViewVisibility(textViewCount, View.VISIBLE);
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
            Claim.StreamMetadata streamMetadata = (Claim.StreamMetadata) metadata;
            if (claim.getFile() == null && !claim.isFree()) {
                // not free (and the user does not own the claim yet), perform a purchase
                confirmPurchaseUrl();
            } else {
                findViewById(R.id.file_view_main_action_button).setVisibility(View.INVISIBLE);
                findViewById(R.id.file_view_main_action_loading).setVisibility(View.VISIBLE);
                handleMainActionForClaim();
            }
        } else {
            showError(getString(R.string.cannot_view_claim));
        }
    }

    private void confirmPurchaseUrl() {
        if (claim != null) {
            Fee fee = ((Claim.StreamMetadata) claim.getValue()).getFee();
            double cost = claim.getActualCost(Lbryio.LBCUSDRate).doubleValue();
            String message = getResources().getQuantityString(R.plurals.confirm_purchase_message, cost == 1 ? 1 : 2, claim.getTitle(), cost);
            AlertDialog.Builder builder = new AlertDialog.Builder(this).
                    setTitle(R.string.confirm_purchase).
                    setMessage(message)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Bundle bundle = new Bundle();
                            bundle.putString("uri", currentUrl);
                            bundle.putBoolean("paid", true);
                            bundle.putDouble("amount", Helper.parseDouble(fee.getAmount(), 0));
                            bundle.putDouble("lbc_amount", cost);
                            bundle.putString("currency", fee.getCurrency());
                            LbryAnalytics.logEvent(LbryAnalytics.EVENT_PURCHASE_URI, bundle);

                            findViewById(R.id.file_view_main_action_button).setVisibility(View.INVISIBLE);
                            findViewById(R.id.file_view_main_action_loading).setVisibility(View.VISIBLE);
                            handleMainActionForClaim();
                        }
                    }).setNegativeButton(R.string.no, null);
            builder.show();
        }
    }

    private void handleMainActionForClaim() {
        if (Lbry.SDK_READY) {
            // Check if the file already exists for the claim
            if (claim.getFile() != null) {
                playOrViewMedia();
            } else {
                fileGet(downloadRequested || !claim.isPlayable());
                downloadRequested = false;
            }
        } else {
            if (claim.isPlayable()) {
                startTimeMillis = System.currentTimeMillis();
                showExoplayerView();
                playMedia();
            } else {
                Snackbar.make(findViewById(R.id.file_view_global_layout), R.string.sdk_initializing_functionality, Snackbar.LENGTH_LONG).show();
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
                        sendBroadcast(intent);
                    } else {
                        // streaming
                        playOrViewMedia();
                    }
                }
            }

            @Override
            public void onError(Exception error, boolean saveFile) {
                showError(getString(R.string.unable_to_view_url, currentUrl));
                if (saveFile) {
                    onDownloadAborted();
                }
                restoreMainActionButton();
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
                    if (mediaType.startsWith("image")) {
                        // display the image
                        View container = findViewById(R.id.file_view_imageviewer_container);
                        PhotoView photoView = findViewById(R.id.file_view_imageviewer);

                        Glide.with(getApplicationContext()).load(fileUri).centerInside().into(photoView);
                        hideFloatingWalletBalance();
                        container.setVisibility(View.VISIBLE);
                    } else if (mediaType.startsWith("text")) {
                        // show web view (and parse markdown too)
                        View container = findViewById(R.id.file_view_webview_container);
                        WebView webView = findViewById(R.id.file_view_webview);
                        if (Arrays.asList("text/markdown", "text/md").contains(mediaType.toLowerCase())) {
                            loadMarkdownFromFile(claimFile.getDownloadPath());
                        } else {
                            webView.loadUrl(fileUri.toString());
                        }
                        hideFloatingWalletBalance();
                        container.setVisibility(View.VISIBLE);
                    }
                    handled = true;
                }
            }
        }

        if (!handled) {
            showUnsupportedView();
        }
    }

    private void loadMarkdownFromFile(String filePath) {
        ReadTextFileTask task = new ReadTextFileTask(filePath, new ReadTextFileTask.ReadTextFileHandler() {
            @Override
            public void onSuccess(String text) {
                String html = buildMarkdownHtml(text);
                WebView webView = findViewById(R.id.file_view_webview);
                webView.loadData(html, "text/html", "utf-8");
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
                "              @font-face {\n" +
                "                  font-family: 'Inter';\n" +
                "                  src: url('file:///android_res/font/inter_regular.otf');\n" +
                "                  font-weight: normal;\n" +
                "              }\n" +
                "             @font-face {\n" +
                "                 font-family: 'Inter';\n" +
                "                 src: url('file:///android_res/font/inter_bold.otf');\n" +
                "                 font-weight: bold;\n" +
                "              }\n" +
                "              body { font-family: 'Inter', sans-serif; margin: 16px }\n" +
                "              img { width: 100%; }\n" +
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
        Snackbar.make(findViewById(R.id.file_view_claim_display_area), message, Snackbar.LENGTH_LONG).
                setTextColor(Color.WHITE).
                setBackgroundTint(Color.RED).
                show();
    }

    private void loadRelatedContent() {
        // reset the list view
        ((RecyclerView) findViewById(R.id.file_view_related_content_list)).setAdapter(null);

        String title = claim.getTitle();
        String claimId = claim.getClaimId();
        ProgressBar relatedLoading = findViewById(R.id.file_view_related_content_progress);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        boolean canShowMatureContent = sp.getBoolean(MainActivity.PREFERENCE_KEY_SHOW_MATURE_CONTENT, false);
        LighthouseSearchTask relatedTask = new LighthouseSearchTask(
                title, RELATED_CONTENT_SIZE, 0, canShowMatureContent, claimId, relatedLoading, new ClaimSearchTask.ClaimSearchResultHandler() {
            @Override
            public void onSuccess(List<Claim> claims, boolean hasReachedEnd) {
                List<Claim> filteredClaims = new ArrayList<>();
                for (Claim c : claims) {
                    if (!c.getClaimId().equalsIgnoreCase(claim.getClaimId())) {
                        filteredClaims.add(c);
                    }
                }

                relatedContentAdapter = new ClaimListAdapter(filteredClaims, FileViewActivity.this);
                relatedContentAdapter.setListener(new ClaimListAdapter.ClaimListItemListener() {
                    @Override
                    public void onClaimClicked(Claim claim) {
                        if (claim.getName().startsWith("@")) {
                            // opening a channel
                            Intent intent = new Intent(MainActivity.ACTION_OPEN_CHANNEL_URL);
                            intent.putExtra("url", claim.getPermanentUrl());
                            sendBroadcast(intent);
                            bringMainTaskToFront();
                            finish();
                        } else {
                            Intent intent = new Intent(FileViewActivity.this, FileViewActivity.class);
                            //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            intent.putExtra("claimId", claim.getClaimId());
                            intent.putExtra("url", !Helper.isNullOrEmpty(claim.getShortUrl()) ? claim.getShortUrl() : claim.getPermanentUrl());
                            MainActivity.startingFileViewActivity = true;
                            startActivity(intent);
                        }
                    }
                });

                RecyclerView relatedContentList = findViewById(R.id.file_view_related_content_list);
                relatedContentList.setAdapter(relatedContentAdapter);
                relatedContentAdapter.notifyDataSetChanged();

                Helper.setViewVisibility(findViewById(R.id.file_view_no_related_content), relatedContentAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onError(Exception error) {

            }
        });
        relatedTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void onBackPressed() {
        if (isInFullscreenMode()) {
            disableFullScreenMode();
            return;
        }

        if (isImageViewerVisible()) {
            findViewById(R.id.file_view_imageviewer_container).setVisibility(View.GONE);
            restoreMainActionButton();
            showFloatingWalletBalance();
            return;
        }
        if (isWebViewVisible()) {
            findViewById(R.id.file_view_webview_container).setVisibility(View.GONE);
            restoreMainActionButton();
            showFloatingWalletBalance();
            return;
        }

        bringMainTaskToFront();
    }

    private void startMainActivity() {
        MainActivity.mainActive = true;
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private boolean isImageViewerVisible() {
        return findViewById(R.id.file_view_imageviewer_container).getVisibility() == View.VISIBLE;
    }

    private boolean isWebViewVisible() {
        return findViewById(R.id.file_view_webview_container).getVisibility() == View.VISIBLE;
    }

    protected void onUserLeaveHint() {
        if (stopServiceReceived || claim == null || !claim.isPlayable() || !playbackStarted) {
            return;
        }

        if (startingShareActivity) {
            // share activity triggered this, so reset the flag at this point
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    startingShareActivity = false;
                }
            }, 1000);
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !MainActivity.mainActive) {
            PictureInPictureParams params = new PictureInPictureParams.Builder().build();
            enterPictureInPictureMode(params);
        }
    }

    protected void onStop() {
        if (inPictureInPictureMode && MainActivity.appPlayer != null) {
            MainActivity.appPlayer.setPlayWhenReady(false);
        }
        super.onStop();
    }


    protected void onDestroy() {
        Helper.unregisterReceiver(downloadEventReceiver, this);
        Helper.unregisterReceiver(sdkReceiver, this);
        if (MainActivity.appPlayer != null && fileViewPlayerListener != null) {
            MainActivity.appPlayer.removeListener(fileViewPlayerListener);
        }
        instance = null;

        if (stopServiceReceived) {
            MainActivity.stopExoplayer();
        }

        super.onDestroy();
    }

    private void renderPictureInPictureMode() {
        findViewById(R.id.file_view_scroll_view).setVisibility(View.GONE);
        findViewById(R.id.floating_balance_main_container).setVisibility(View.GONE);
    }
    private void renderFullMode() {
        findViewById(R.id.file_view_scroll_view).setVisibility(View.VISIBLE);
        if (!isInFullscreenMode()) {
            findViewById(R.id.floating_balance_main_container).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, Configuration newConfig) {
        inPictureInPictureMode = isInPictureInPictureMode;
        if (isInPictureInPictureMode) {
            renderPictureInPictureMode();
        } else {
            backStackLost = true;
            renderFullMode();
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    private void enableFullScreenMode() {
        findViewById(R.id.floating_balance_main_container).setVisibility(View.INVISIBLE);

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

        ConstraintLayout globalLayout = findViewById(R.id.file_view_global_layout);
        View exoplayerContainer = findViewById(R.id.file_view_exoplayer_container);
        ((ViewGroup) exoplayerContainer.getParent()).removeView(exoplayerContainer);
        globalLayout.addView(exoplayerContainer);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        ((ImageView) findViewById(R.id.player_image_full_screen_toggle)).setImageResource(R.drawable.ic_fullscreen_exit);
    }

    private void disableFullScreenMode() {
        RelativeLayout mediaContainer = findViewById(R.id.file_view_media_container);
        View exoplayerContainer = findViewById(R.id.file_view_exoplayer_container);
        ((ViewGroup) exoplayerContainer.getParent()).removeView(exoplayerContainer);
        mediaContainer.addView(exoplayerContainer);

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_VISIBLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);

        ((ImageView) findViewById(R.id.player_image_full_screen_toggle)).setImageResource(R.drawable.ic_fullscreen);
        findViewById(R.id.floating_balance_main_container).setVisibility(View.VISIBLE);
    }

    private boolean isInFullscreenMode() {
        View exoplayerContainer = findViewById(R.id.file_view_exoplayer_container);
        return exoplayerContainer.getParent() instanceof ConstraintLayout;
    }

    private void scheduleElapsedPlayback() {
        if (!elapsedPlaybackScheduled) {
            elapsedPlaybackScheduler = Executors.newSingleThreadScheduledExecutor();
            elapsedPlaybackScheduler.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (MainActivity.appPlayer != null) {
                                elapsedDuration = MainActivity.appPlayer.getCurrentPosition();
                                renderElapsedDuration();
                            }
                        }
                    });
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

        MainActivity.appPlayer.removeListener(fileViewPlayerListener);
    }

    private void showBuffering() {
        findViewById(R.id.player_buffering_progress).setVisibility(View.VISIBLE);
    }

    private void hideBuffering() {
        findViewById(R.id.player_buffering_progress).setVisibility(View.INVISIBLE);
    }

    private void renderElapsedDuration() {
        Helper.setViewText(findViewById(R.id.player_duration_elapsed), Helper.formatDuration(Double.valueOf(elapsedDuration / 1000.0).longValue()));
    }

    private void renderTotalDuration() {
        Helper.setViewText(findViewById(R.id.player_duration_total), Helper.formatDuration(Double.valueOf(totalDuration / 1000.0).longValue()));
    }

    private void loadAndScheduleDurations() {
        if (MainActivity.appPlayer != null) {
            if (totalDuration == 0) {
                elapsedDuration = MainActivity.appPlayer.getCurrentPosition();
                totalDuration = MainActivity.appPlayer.getDuration();
            }
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
            SolidIconView iconFollowUnfollow = findViewById(R.id.file_view_icon_follow_unfollow);
            if (iconFollowUnfollow != null) {
                iconFollowUnfollow.setText(isFollowing ? R.string.fa_heart_broken : R.string.fa_heart);
                iconFollowUnfollow.setTextColor(ContextCompat.getColor(this, isFollowing ? R.color.foreground : R.color.red));
            }
        }
    }

    private void claimEligibleRewards() {
        // attempt to claim eligible rewards after viewing or playing a file (fail silently)
        ClaimRewardTask firstStreamTask = new ClaimRewardTask(Reward.TYPE_FIRST_STREAM, null, null, this, eligibleRewardHandler);
        ClaimRewardTask dailyViewTask = new ClaimRewardTask(Reward.TYPE_DAILY_VIEW, null, null, this, eligibleRewardHandler);
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
            Snackbar.make(findViewById(R.id.file_view_global_layout), message, Snackbar.LENGTH_LONG).show();
        }

        @Override
        public void onError(Exception error) {
            // pass
        }
    };

    private void registerDownloadEventReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DownloadManager.ACTION_DOWNLOAD_EVENT);
        downloadEventReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String downloadAction = intent.getStringExtra("action");
                String uri = intent.getStringExtra("uri");
                String outpoint = intent.getStringExtra("outpoint");
                String fileInfoJson = intent.getStringExtra("file_info");
                if (claim == null || uri == null || outpoint == null || (fileInfoJson == null && !"abort".equals(downloadAction))) {
                    return;
                }
                if (claim != null && !claim.getPermanentUrl().equalsIgnoreCase(uri)) {
                    return;
                }
                if ("abort".equals(downloadAction)) {
                    // handle download aborted
                    onDownloadAborted();
                    return;
                }

                ImageView downloadIconView = findViewById(R.id.file_view_action_download_icon);
                ProgressBar downloadProgressView = findViewById(R.id.file_view_download_progress);

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
                        double progress = intent.getDoubleExtra("progress", 0);
                        Helper.setViewVisibility(downloadProgressView, View.VISIBLE);
                        downloadProgressView.setProgress(Double.valueOf(progress).intValue());
                        downloadIconView.setImageResource(R.drawable.ic_stop);
                    }
                    else if (DownloadManager.ACTION_COMPLETE.equals(downloadAction)) {
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
        };
        registerReceiver(downloadEventReceiver, intentFilter);
    }

    private void checkIsFileComplete() {
        if (claim == null) {
            return;
        }
        if (claim.getFile() != null && claim.getFile().isCompleted()) {
            Helper.setViewVisibility(findViewById(R.id.file_view_action_delete), View.VISIBLE);
            Helper.setViewVisibility(findViewById(R.id.file_view_action_download), View.GONE);
        } else {
            Helper.setViewVisibility(findViewById(R.id.file_view_action_delete), View.GONE);
            Helper.setViewVisibility(findViewById(R.id.file_view_action_download), View.VISIBLE);
        }
    }

    private void hideFloatingWalletBalance() {
        findViewById(R.id.floating_balance_main_container).setVisibility(View.GONE);
    }
    private void showFloatingWalletBalance() {
        findViewById(R.id.floating_balance_main_container).setVisibility(View.VISIBLE);
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
        ((ImageView) findViewById(R.id.file_view_action_download_icon)).setImageResource(R.drawable.ic_download);
        Helper.setViewVisibility(findViewById(R.id.file_view_download_progress), View.GONE);
        Helper.setViewVisibility(findViewById(R.id.file_view_unsupported_container), View.GONE);

        checkIsFileComplete();
        restoreMainActionButton();
    }

    private void restoreMainActionButton() {
        findViewById(R.id.file_view_main_action_loading).setVisibility(View.INVISIBLE);
        findViewById(R.id.file_view_main_action_button).setVisibility(View.VISIBLE);
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

    private void bringMainTaskToFront() {
        if (backStackLost) {
            ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            final List<ActivityManager.AppTask> appTasks = activityManager.getAppTasks();
            for (ActivityManager.AppTask task : appTasks) {
                final Intent baseIntent = task.getTaskInfo().baseIntent;
                final Set<String> categories = baseIntent.getCategories();
                if (categories != null && categories.contains(Intent.CATEGORY_LAUNCHER)) {
                    task.moveToFront();
                    finish();
                    return;
                }
            }
        }

        startMainActivity();
    }
}
