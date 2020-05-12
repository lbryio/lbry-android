package io.lbry.browser;

import android.annotation.SuppressLint;
import android.app.PictureInPictureParams;
import android.content.BroadcastReceiver;
import android.content.Context;
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
import android.service.voice.VoiceInteractionSession;
import android.text.format.DateUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.widget.NestedScrollView;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
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

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import io.lbry.browser.adapter.ClaimListAdapter;
import io.lbry.browser.adapter.TagListAdapter;
import io.lbry.browser.dialog.SendTipDialogFragment;
import io.lbry.browser.exceptions.LbryUriException;
import io.lbry.browser.model.Claim;
import io.lbry.browser.model.ClaimCacheKey;
import io.lbry.browser.model.Fee;
import io.lbry.browser.model.File;
import io.lbry.browser.model.Tag;
import io.lbry.browser.model.lbryinc.Reward;
import io.lbry.browser.tasks.ClaimListResultHandler;
import io.lbry.browser.tasks.ClaimSearchTask;
import io.lbry.browser.tasks.FileListTask;
import io.lbry.browser.tasks.GenericTaskHandler;
import io.lbry.browser.tasks.LighthouseSearchTask;
import io.lbry.browser.tasks.ResolveTask;
import io.lbry.browser.tasks.lbryinc.ClaimRewardTask;
import io.lbry.browser.tasks.lbryinc.FetchStatCountTask;
import io.lbry.browser.tasks.lbryinc.LogFileViewTask;
import io.lbry.browser.utils.Helper;
import io.lbry.browser.utils.Lbry;
import io.lbry.browser.utils.LbryAnalytics;
import io.lbry.browser.utils.LbryUri;

public class FileViewActivity extends AppCompatActivity {

    public static FileViewActivity instance = null;
    private static final int RELATED_CONTENT_SIZE = 16;
    private static final int SHARE_REQUEST_CODE = 3001;
    private static boolean startingShareActivity;

    private boolean hasLoadedFirstBalance;
    private boolean loadFilePending;
    private boolean resolving;
    private Claim claim;
    private String currentUrl;
    private ClaimListAdapter relatedContentAdapter;
    private File file;
    private BroadcastReceiver sdkReceiver;
    private Player.EventListener fileViewPlayerListener;

    private long elapsedDuration = 0;
    private long totalDuration = 0;
    private boolean elapsedPlaybackScheduled;
    private ScheduledExecutorService elapsedPlaybackScheduler;
    private boolean playbackStarted;
    private long startTimeMillis;

    private View buttonShareAction;
    private View buttonTipAction;
    private View buttonRepostAction;
    private View buttonDownloadAction;
    private View buttonEditAction;
    private View buttonDeleteAction;
    private View buttonReportAction;

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
        if (url.contains("#")) {
            key.setPermanentUrl(url); // use the same url for the key so that we can match the key for any value that's the same
            key.setCanonicalUrl(url);
            key.setShortUrl(url);
        }
        if (Lbry.claimCache.containsKey(key)) {
            claim = Lbry.claimCache.get(key);
            checkAndResetNowPlayingClaim();
            file = claim.getFile();
            if (file == null) {
                loadFile();
            }
        }
        setContentView(R.layout.activity_file_view);

        currentUrl = url;
        logUrlEvent(url);
        if (claim == null) {
            resolveUrl(url);
        }

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
                } else if (playbackState == Player.STATE_BUFFERING) {
                    showBuffering();
                } else {
                    hideBuffering();
                }
            }
        };

        initUi();
        onWalletBalanceUpdated();
        renderClaim();
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

                currentUrl = newUrl;
                logUrlEvent(newUrl);
                resetViewCount();
                ClaimCacheKey key = new ClaimCacheKey();
                key.setClaimId(newClaimId);
                if (!Helper.isNullOrEmpty(newUrl) && newUrl.contains("#")) {
                    key.setPermanentUrl(newUrl);
                    key.setCanonicalUrl(newUrl);
                    key.setShortUrl(newUrl);
                }
                if (Lbry.claimCache.containsKey(key)) {
                    claim = Lbry.claimCache.get(key);
                    checkAndResetNowPlayingClaim();
                    file = claim.getFile();
                    if (file == null) {
                        loadFile();
                    }
                    renderClaim();
                } else {
                    findViewById(R.id.file_view_claim_display_area).setVisibility(View.INVISIBLE);
                    resolveUrl(newUrl);
                }
            }
        }
    }

    private void registerSdkReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(MainActivity.ACTION_SDK_READY);
        filter.addAction(MainActivity.ACTION_WALLET_BALANCE_UPDATED);
        sdkReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equalsIgnoreCase(MainActivity.ACTION_SDK_READY)) {
                    // authenticate after we receive the sdk ready event
                    if (loadFilePending) {
                        loadFile();
                    }
                } else if (action.equalsIgnoreCase(MainActivity.ACTION_WALLET_BALANCE_UPDATED)) {
                    onWalletBalanceUpdated();
                }
            }
        };
        registerReceiver(sdkReceiver, filter);
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
        }
    }

    private String getStreamingUrl() {
        if (file != null && !Helper.isNullOrEmpty(file.getStreamingUrl())) {
            return file.getStreamingUrl();
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
        // TODO: Check if it's paid content and then wait for the user to explicitly request the file
        String claimId = claim.getClaimId();
        FileListTask task = new FileListTask(claimId, null, new FileListTask.FileListResultHandler() {
            @Override
            public void onSuccess(List<File> files) {
                if (files.size() > 0) {
                    file = files.get(0);
                    claim.setFile(file);
                }
            }

            @Override
            public void onError(Exception error) {

            }
        });
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    protected void onResume() {
        super.onResume();
        MainActivity.startingFileViewActivity = false;
    }

    private void resolveUrl(String url) {
        resolving = true;
        findViewById(R.id.file_view_claim_display_area).setVisibility(View.INVISIBLE);
        View loadingView = findViewById(R.id.file_view_loading_container);
        ResolveTask task = new ResolveTask(url, Lbry.LBRY_TV_CONNECTION_STRING, loadingView, new ClaimListResultHandler() {
            @Override
            public void onSuccess(List<Claim> claims) {
                if (claims.size() > 0) {
                    claim = claims.get(0);
                    if (Claim.TYPE_REPOST.equalsIgnoreCase(claim.getValueType())) {
                        claim = claim.getRepostedClaim();

                        // cache the reposted claim too for subsequent loads
                        ClaimCacheKey key = ClaimCacheKey.fromClaim(claim);
                        Lbry.claimCache.put(key, claim);
                    }

                    checkAndResetNowPlayingClaim();
                    loadFile();
                    renderClaim();
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

        findViewById(R.id.player_toggle_full_screen).setOnClickListener(new View.OnClickListener() {
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

        RecyclerView relatedContentList = findViewById(R.id.file_view_related_content_list);
        relatedContentList.setNestedScrollingEnabled(false);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        relatedContentList.setLayoutManager(llm);
    }

    private void renderClaim() {
        if (claim == null) {
            return;
        }

        loadViewCount();

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
                    moveTaskToBack(true);
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

        boolean isFree = true;
        if (metadata instanceof Claim.StreamMetadata) {
            Claim.StreamMetadata streamMetadata = (Claim.StreamMetadata) metadata;
            long publishTime = streamMetadata.getReleaseTime() > 0 ? streamMetadata.getReleaseTime() * 1000 : claim.getTimestamp() * 1000;
            ((TextView) findViewById(R.id.file_view_publish_time)).setText(DateUtils.getRelativeTimeSpanString(
                    publishTime, System.currentTimeMillis(), 0, DateUtils.FORMAT_ABBREV_RELATIVE));

            Fee fee = streamMetadata.getFee();
            if (fee != null && Helper.parseDouble(fee.getAmount(), 0) > 0) {
                isFree = false;
                findViewById(R.id.file_view_fee_container).setVisibility(View.VISIBLE);
                ((TextView) findViewById(R.id.file_view_fee)).setText(Helper.shortCurrencyFormat(Helper.parseDouble(fee.getAmount(), 0)));
            }

            MaterialButton mainActionButton = findViewById(R.id.file_view_main_action_button);
            String mediaType = streamMetadata.getSource().getMediaType();
            if (mediaType.startsWith("audio") || mediaType.startsWith("video")) {
                mainActionButton.setText(R.string.play);
            } else if (mediaType.startsWith("text") || mediaType.startsWith("image")) {
                mainActionButton.setText(R.string.view);
            } else {
                mainActionButton.setText(R.string.download);
            }
        }

        if (isFree) {
            onMainActionButtonClicked();
        }

        loadRelatedContent();
    }

    private void showUnsupportedView() {
        findViewById(R.id.file_view_exoplayer_container).setVisibility(View.GONE);

        findViewById(R.id.file_view_unsupported_container).setVisibility(View.VISIBLE);
    }

    private void showExoplayerView() {
        findViewById(R.id.file_view_unsupported_container).setVisibility(View.GONE);

        findViewById(R.id.file_view_exoplayer_container).setVisibility(View.VISIBLE);
    }

    private void playMedia() {
        boolean newPlayerCreated = false;
        if (MainActivity.appPlayer == null) {
            MainActivity.appPlayer = new SimpleExoPlayer.Builder(this).build();
            MainActivity.appPlayer.setPlayWhenReady(true);
            MainActivity.appPlayer.addListener(fileViewPlayerListener);

            newPlayerCreated = true;
        }

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
        MainActivity.setNowPlayingClaim(claim, FileViewActivity.this);
        String userAgent = Util.getUserAgent(this, getString(R.string.app_name));
        MediaSource mediaSource = new ProgressiveMediaSource.Factory(
                new DefaultDataSourceFactory(this, userAgent),
                new DefaultExtractorsFactory()
        ).createMediaSource(Uri.parse(getStreamingUrl()));
        MainActivity.appPlayer.prepare(mediaSource, true, true);
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

            Fee fee = streamMetadata.getFee();
            if (fee != null && Helper.parseDouble(fee.getAmount(), 0) > 0) {
                // not free, perform a purchase

            } else {
                handleMainActionForClaim();
            }
        } else {
            showError(getString(R.string.cannot_view_claim));
        }
    }

    private void handleMainActionForClaim() {
        startTimeMillis = System.currentTimeMillis();
        Claim.GenericMetadata metadata = claim.getValue();
        if (metadata instanceof Claim.StreamMetadata) {
            Claim.StreamMetadata streamMetadata = (Claim.StreamMetadata) metadata;
            // Check the metadata type
            String mediaType = streamMetadata.getSource().getMediaType();
            // Use Exoplayer view if it's video / audio
            if (mediaType.startsWith("audio") || mediaType.startsWith("video")) {
                showExoplayerView();
                playMedia();
            } else if (mediaType.startsWith("text")) {

            } else if (mediaType.startsWith("image")) {

            } else {
                // unsupported type
                showUnsupportedView();
            }
        } else {
            showError(getString(R.string.cannot_view_claim));
        }
    }

    private void showError(String message) {
        Snackbar.make(findViewById(R.id.file_view_claim_display_area), message, Snackbar.LENGTH_LONG).setBackgroundTint(Color.RED).show();
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
                        Intent intent = new Intent(FileViewActivity.this, FileViewActivity.class);
                        intent.putExtra("claimId", claim.getClaimId());
                        intent.putExtra("url", claim.getPermanentUrl());
                        MainActivity.startingFileViewActivity = true;
                        startActivity(intent);
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

        MainActivity.mainActive = true;
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    protected void onUserLeaveHint() {
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (isInPictureInPictureMode() && MainActivity.appPlayer != null) {
                MainActivity.appPlayer.setPlayWhenReady(false);
            }
        }
        super.onStop();
    }

    protected void onDestroy() {
        Helper.unregisterReceiver(sdkReceiver, this);
        if (MainActivity.appPlayer != null && fileViewPlayerListener != null) {
            MainActivity.appPlayer.removeListener(fileViewPlayerListener);
        }
        instance = null;
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
        if (isInPictureInPictureMode) {
            renderPictureInPictureMode();
        } else {
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
        elapsedPlaybackScheduled = false;
        if (elapsedPlaybackScheduler != null) {
            elapsedPlaybackScheduler.shutdownNow();
            elapsedPlaybackScheduler = null;
        }

        playbackStarted = false;
        startTimeMillis = 0;
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

    private void logPlay(String url, long startTimeMillis) {
        long timeToStartMillis = startTimeMillis > 0 ? System.currentTimeMillis() - startTimeMillis : 0;

        Bundle bundle = new Bundle();
        bundle.putString("uri", url);
        bundle.putLong("time_to_start_ms", timeToStartMillis);
        bundle.putLong("time_to_start_seconds", Double.valueOf(timeToStartMillis / 1000.0).longValue());
        LbryAnalytics.logEvent(LbryAnalytics.EVENT_PLAY, bundle);

        logFileView(url, timeToStartMillis);
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
}
