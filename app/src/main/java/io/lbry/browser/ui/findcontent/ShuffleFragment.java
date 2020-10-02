package io.lbry.browser.ui.findcontent;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.database.ExoDatabaseProvider;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;
import com.google.android.exoplayer2.util.Util;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import io.lbry.browser.MainActivity;
import io.lbry.browser.R;
import io.lbry.browser.data.DatabaseHelper;
import io.lbry.browser.exceptions.LbryUriException;
import io.lbry.browser.model.Claim;
import io.lbry.browser.model.lbryinc.Reward;
import io.lbry.browser.tasks.BufferEventTask;
import io.lbry.browser.tasks.GenericTaskHandler;
import io.lbry.browser.tasks.claim.ClaimSearchResultHandler;
import io.lbry.browser.tasks.claim.ClaimSearchTask;
import io.lbry.browser.tasks.lbryinc.ClaimRewardTask;
import io.lbry.browser.tasks.lbryinc.LogFileViewTask;
import io.lbry.browser.ui.BaseFragment;
import io.lbry.browser.utils.Helper;
import io.lbry.browser.utils.Lbry;
import io.lbry.browser.utils.LbryAnalytics;
import io.lbry.browser.utils.LbryUri;
import io.lbry.browser.utils.Lbryio;
import io.lbry.browser.utils.Predefined;

public class ShuffleFragment extends BaseFragment {

    private static final int PAGE_SIZE = 50;

    private int currentClaimSearchPage;
    private int playlistIndex;
    private Claim current;
    private List<Claim> playlist;
    private List<String> watchedContentClaimIds;

    private long sessionStart;
    private ProgressBar surfModeLoading;
    private TextView textTitle;
    private TextView textPublisher;
    private Player player;
    private long elapsedDuration = 0;
    private long totalDuration = 0;
    private boolean elapsedPlaybackScheduled;
    private ScheduledExecutorService elapsedPlaybackScheduler;
    private boolean playbackStarted;
    private long startTimeMillis;
    private boolean isPlaying;
    private boolean newPlayerCreated;
    private String currentUrl;
    private Player.EventListener playerListener;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_shuffle, container, false);

        surfModeLoading = root.findViewById(R.id.shuffle_loading);
        textTitle = root.findViewById(R.id.shuffle_content_title);
        textPublisher = root.findViewById(R.id.shuffle_content_publisher);
        playerListener = new Player.EventListener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if (playbackState == Player.STATE_READY) {
                    elapsedDuration = MainActivity.appPlayer.getCurrentPosition();
                    totalDuration = MainActivity.appPlayer.getDuration() < 0 ? 0 : MainActivity.appPlayer.getDuration();
                    if (!playbackStarted) {
                        logPlay(currentUrl, startTimeMillis);
                        playbackStarted = true;
                        isPlaying = true;

                        if (current != null) {
                            saveWatchedContent(current.getClaimId());
                        }
                    }

                    renderTotalDuration();
                    scheduleElapsedPlayback();
                    hideBuffering();
                } else if (playbackState == Player.STATE_BUFFERING) {
                    Context ctx = getContext();
                    boolean sendBufferingEvents = true;

                    if (ctx != null) {
                        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
                        sendBufferingEvents = sp.getBoolean(MainActivity.PREFERENCE_KEY_SEND_BUFFERING_EVENTS, true);
                    }

                    if (MainActivity.appPlayer != null && MainActivity.appPlayer.getCurrentPosition() > 0 && sendBufferingEvents) {
                        // we only want to log a buffer event after the media has already started playing
                        String mediaSourceUrl = getStreamingUrl();
                        long duration = MainActivity.appPlayer.getDuration();
                        long position = MainActivity.appPlayer.getCurrentPosition();
                        // TODO: Determine a hash for the userId
                        String userIdHash = Helper.SHA256(Lbryio.currentUser != null ? String.valueOf(Lbryio.currentUser.getId()) : "0");
                        if (mediaSourceUrl.startsWith(FileViewFragment.CDN_PREFIX)) {
                            BufferEventTask bufferEvent = new BufferEventTask(current.getPermanentUrl(), duration, position, 1, userIdHash);
                            bufferEvent.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
                        }
                    }

                    showBuffering();
                } else if (playbackState == Player.STATE_ENDED) {
                    playNextClaim();
                } else {
                    hideBuffering();
                }
            }
        };

        Context context = getContext();
        PlayerView playerView = root.findViewById(R.id.shuffle_exoplayer_view);
        playerView.setOnTouchListener(new SwipeListener(playerView, context) {
            @Override
            public void onSwipeLeft() { playNextClaim(); }
            @Override
            public void onSwipeRight() { playPreviousClaim(); }
        });

        root.findViewById(R.id.shuffle_share_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (current != null) {
                    try {
                        String shareUrl = LbryUri.parse(
                                !Helper.isNullOrEmpty(current.getCanonicalUrl()) ? current.getCanonicalUrl() :
                                        (!Helper.isNullOrEmpty(current.getShortUrl()) ? current.getShortUrl() : current.getPermanentUrl())).toTvString();
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


        root.setOnTouchListener(new SwipeListener(root, context) {
            @Override
            public void onSwipeLeft() { playNextClaim(); }
            @Override
            public void onSwipeRight() { playPreviousClaim(); }
        });

        return root;
    }

    private String getStreamingUrl() {
        return current != null ? String.format("https://cdn.lbryplayer.xyz/content/claims/%s/%s/stream", current.getName(), current.getClaimId()) : "";
    }

    private Map<String, Object> buildContentOptions() {
        Context context = getContext();
        boolean canShowMatureContent = false;
        if (context != null) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
            canShowMatureContent = sp.getBoolean(MainActivity.PREFERENCE_KEY_SHOW_MATURE_CONTENT, false);
        }

        return Lbry.buildClaimSearchOptions(
                Claim.TYPE_STREAM,
                canShowMatureContent ? null : new ArrayList<>(Predefined.MATURE_TAGS),
                null/*contentChannelIds*/,
                Arrays.asList(Claim.ORDER_BY_TRENDING_GROUP),
                121, // 2 minutes or less
                1,
                currentClaimSearchPage == 0 ? 1 : currentClaimSearchPage,
                PAGE_SIZE);
    }

    public void onStart() {
        super.onStart();
        MainActivity activity = (MainActivity) getContext();
        if (activity != null) {
            activity.hideFloatingWalletBalance();
        }
    }

    public void onResume() {
        super.onResume();
        sessionStart = System.currentTimeMillis();
        MainActivity activity = (MainActivity) getContext();
        if (activity != null) {
            LbryAnalytics.setCurrentScreen(activity, "Shuffle", "Shuffle");
        }
        if (MainActivity.appPlayer != null && MainActivity.nowPlayingSource != MainActivity.SOURCE_NOW_PLAYING_SHUFFLE) {
            MainActivity.appPlayer.setPlayWhenReady(false);
        }

        if (playlist == null) {
            loadWatchedContentList();
        } else {
            if (current != null) {
                playbackCurrentClaim();
            } else {
                startPlaylist();
            }
            loadAndScheduleDurations();
        }

        if (MainActivity.appPlayer != null) {
            if (MainActivity.playerReassigned) {
                setPlayerForPlayerView();
                MainActivity.playerReassigned = false;
            }
        }
    }

    private void setPlayerForPlayerView() {
        View root = getView();
        if (root != null) {
            PlayerView view = root.findViewById(R.id.shuffle_exoplayer_view);
            view.setVisibility(View.VISIBLE);
            view.setPlayer(null);
            view.setPlayer(MainActivity.appPlayer);
        }
    }

    public void onPause() {
        if (MainActivity.appPlayer != null && MainActivity.appPlayer.isPlaying()) {
            MainActivity.nowPlayingSource = MainActivity.SOURCE_NOW_PLAYING_SHUFFLE;
        }
        super.onPause();
    }

    public void onStop() {
        long sessionDuration = System.currentTimeMillis() - sessionStart;
        if (sessionStart > 0 && sessionDuration > 0) {
            Bundle bundle = new Bundle();
            bundle.putLong("duration_ms", sessionDuration);
            bundle.putInt("duration", Double.valueOf(Math.ceil(sessionDuration / 1000.0)).intValue());
            LbryAnalytics.logEvent(LbryAnalytics.EVENT_SHUFFLE_SESSION, bundle);
        }
        sessionStart = 0;

        MainActivity activity = (MainActivity) getContext();
        if (activity != null) {
            activity.hideFloatingWalletBalance();

        }

        super.onStop();

    }

    private void loadWatchedContentList() {
        (new AsyncTask<Void, Void, List<String>>() {
            protected List<String> doInBackground(Void... params) {
                MainActivity activity = (MainActivity) getContext();
                if (activity != null) {
                    try {
                        SQLiteDatabase db = activity.getDbHelper().getReadableDatabase();
                        return DatabaseHelper.getShuffleWatchedClaims(db);
                    } catch (Exception ex) {
                        // pass
                    }
                }
                return null;
            }
            protected void onPostExecute(List<String> claimIds) {
                watchedContentClaimIds = new ArrayList<>(claimIds);
                if (playlist == null) {
                    loadContent();
                }
            }
        }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void saveWatchedContent(final String claimId) {
        (new AsyncTask<Void, Void, Void>() {
            protected Void doInBackground(Void... params) {
                MainActivity activity = (MainActivity) getContext();
                if (activity != null) {
                    try {
                        SQLiteDatabase db = activity.getDbHelper().getWritableDatabase();
                        DatabaseHelper.createOrUpdateShuffleWatched(claimId, db);
                    } catch (Exception ex) {
                        // pass
                    }
                }
                return  null;
            }
        }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void loadContent() {
        if (playlist == null || playlist.size() == 0) {
            Helper.setViewVisibility(surfModeLoading, View.VISIBLE);
        }

        Map<String, Object> claimSearchOptions = buildContentOptions();
        ClaimSearchTask task = new ClaimSearchTask(claimSearchOptions, Lbry.LBRY_TV_CONNECTION_STRING, null, new ClaimSearchResultHandler() {
            @Override
            public void onSuccess(List<Claim> claims, boolean hasReachedEnd) {
                if (playlist == null) {
                    playlist = new ArrayList<>(claims);
                    startPlaylist();
                } else {
                    for (Claim claim : claims) {
                        if (!playlist.contains(claim)) {
                            playlist.add(claim);
                        }
                    }
                }
                Helper.setViewVisibility(surfModeLoading, View.GONE);
            }

            @Override
            public void onError(Exception error) {
                Helper.setViewVisibility(surfModeLoading, View.GONE);
            }
        });
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void startPlaylist() {
        if (playlist == null || playlist.size() == 0) {
            return;
        }
        playlistIndex = 0;
        current = playlist.get(playlistIndex);
        checkCurrentClaimIsVideo(false);
        checkCurrentClaimWatched(false);
        playbackCurrentClaim();
    }

    private void checkCurrentClaimIsVideo(boolean previous) {
        while (current == null || current.getMediaType() == null || !current.getMediaType().startsWith("video")) {
            // only play videos
            if (previous) {
                playlistIndex--;
            } else {
                playlistIndex++;
                checkPlaylistSize();
            }
            current = playlist.get(playlistIndex);
        }
    }

    private void checkCurrentClaimWatched(boolean previous) {
        if (current != null && watchedContentClaimIds != null) {
            while (watchedContentClaimIds.contains(current.getClaimId())) {
                if (previous) {
                    playlistIndex--;
                } else {
                    playlistIndex++;
                    checkPlaylistSize();
                }
                current = playlist.get(playlistIndex);
            }
        }
    }

    private void playPreviousClaim() {
        if (playlist == null || playlist.size() == 0) {
            return;
        }
        if (playlistIndex > 0) {
            playlistIndex--;
        }
        current = playlist.get(playlistIndex);
        checkCurrentClaimIsVideo(true);
        checkCurrentClaimWatched(true);
        playbackCurrentClaim();
    }
    private void playNextClaim() {
        if (playlist == null || playlist.size() == 0) {
            return;
        }
        if (playlistIndex < playlist.size() - 1) {
            playlistIndex++;
        }
        checkPlaylistSize();
        current = playlist.get(playlistIndex);
        checkCurrentClaimIsVideo(false);
        checkCurrentClaimWatched(false);
        playbackCurrentClaim();
    }

    private void checkPlaylistSize() {
        if (playlist.size() - playlistIndex < 10) {
            currentClaimSearchPage++;
            loadContent();
        }
    }

    private void playbackCurrentClaim() {
        resetPlayer();
        String publisherText = !Helper.isNullOrEmpty(current.getPublisherTitle()) ?
                String.format("%s (%s)", current.getPublisherTitle(), current.getPublisherName()) :
                !Helper.isNullOrEmpty(current.getPublisherName()) ? current.getPublisherName() : getString(R.string.anonymous);

        textTitle.setText(current.getTitle());
        textPublisher.setText(publisherText);

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
            if (playerListener != null) {
                MainActivity.appPlayer.addListener(playerListener);
            }

            newPlayerCreated = true;
        }

        if (context instanceof MainActivity) {
            MainActivity activity = (MainActivity) context;
            activity.initPlaybackNotification();
        }

        View root = getView();
        if (root != null) {
            PlayerView view = root.findViewById(R.id.shuffle_exoplayer_view);
            view.setShutterBackgroundColor(Color.TRANSPARENT);
            view.setPlayer(MainActivity.appPlayer);
            view.setUseController(true);
            if (context instanceof MainActivity) {
                ((MainActivity) context).getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }

            if (MainActivity.nowPlayingClaim != null &&
                    MainActivity.nowPlayingClaim.getClaimId().equalsIgnoreCase(current.getClaimId()) &&
                    !newPlayerCreated) {
                // if the claim is already playing, we don't need to reload the media source
                return;
            }

            if (MainActivity.appPlayer != null) {
                showBuffering();
                if (context instanceof MainActivity) {
                    ((MainActivity) context).setNowPlayingClaim(current, current.getPermanentUrl());
                }

                MainActivity.appPlayer.setPlayWhenReady(true);
                String userAgent = Util.getUserAgent(context, getString(R.string.app_name));
                String mediaSourceUrl = getStreamingUrl();
                MediaSource mediaSource = new ProgressiveMediaSource.Factory(
                        new CacheDataSourceFactory(MainActivity.playerCache, new DefaultDataSourceFactory(context, userAgent)),
                        new DefaultExtractorsFactory()
                ).setLoadErrorHandlingPolicy(new FileViewFragment.StreamLoadErrorPolicy()).createMediaSource(Uri.parse(mediaSourceUrl));

                MainActivity.appPlayer.prepare(mediaSource, true, true);
            }
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

        /*if (MainActivity.appPlayer != null) {
            MainActivity.appPlayer.stop(true);
            MainActivity.appPlayer.removeListener(fileViewPlayerListener);
            PlaybackParameters params = new PlaybackParameters(1.0f);
            MainActivity.appPlayer.setPlaybackParameters(params);
        }*/
    }

    private void logPlay(String url, long startTimeMillis) {
        long timeToStartMillis = startTimeMillis > 0 ? System.currentTimeMillis() - startTimeMillis : 0;

        Bundle bundle = new Bundle();
        bundle.putString("uri", url);
        bundle.putLong("time_to_start_ms", timeToStartMillis);
        bundle.putLong("time_to_start_seconds", Double.valueOf(timeToStartMillis / 1000.0).longValue());
        LbryAnalytics.logEvent(LbryAnalytics.EVENT_PLAY, bundle);

        logFileView(current.getPermanentUrl(), timeToStartMillis);
    }

    private void logFileView(String url, long timeToStart) {
        if (current != null) {
            LogFileViewTask task = new LogFileViewTask(url, current, timeToStart, new GenericTaskHandler() {
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

    private void showBuffering() {
        View root = getView();
        if (root != null) {
            root.findViewById(R.id.player_buffering_progress).setVisibility(View.VISIBLE);

            PlayerView playerView = root.findViewById(R.id.shuffle_exoplayer_view);
            playerView.findViewById(R.id.player_skip_back_10).setVisibility(View.INVISIBLE);
            playerView.findViewById(R.id.player_skip_forward_10).setVisibility(View.INVISIBLE);
        }
    }

    private void hideBuffering() {
        View root = getView();
        if (root != null) {
            root.findViewById(R.id.player_buffering_progress).setVisibility(View.INVISIBLE);

            PlayerView playerView = root.findViewById(R.id.shuffle_exoplayer_view);
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

    // TODO: Move this call to MainActivity?
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

    private static class SwipeListener implements View.OnTouchListener {
        private final GestureDetector gestureDetector;
        private final View control;
        public SwipeListener(View control, Context context) {
            this.control = control;
            gestureDetector = new GestureDetector(context, new SwipeGestureListener());
        }
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            return gestureDetector.onTouchEvent(motionEvent);
        }

        public void onSwipeLeft() { }
        public void onSwipeRight() { }
        public void onSwipeTop() { }
        public void onSwipeBottom() { }

        private final class SwipeGestureListener extends GestureDetector.SimpleOnGestureListener {
            private static final int SWIPE_THRESHOLD = 100;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;

            @Override
            public boolean onDown(MotionEvent e) {
                if (control instanceof PlayerView) {
                    return false;
                }
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                boolean result = false;
                try {
                    float diffY = e2.getY() - e1.getY();
                    float diffX = e2.getX() - e1.getX();
                    if (Math.abs(diffX) > Math.abs(diffY)) {
                        if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                            if (diffX > 0) {
                                onSwipeRight();
                            } else {
                                onSwipeLeft();
                            }
                            result = true;
                        }
                    }
                    else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffY > 0) {
                            onSwipeBottom();
                        } else {
                            onSwipeTop();
                        }
                        result = true;
                    }
                } catch (Exception ex) {
                    // pass
                }
                return result;
            }
        }
    }

    @Override
    public boolean shouldHideGlobalPlayer() {
        return true;
    }
    
    private static final List<String> contentChannelIds = Arrays.asList(
            "3fec094c5937e9eb4e8f5e71e4ca430e8a993d03",
            "6184648aab0431c4c95c649072d1f9ff08b9bb7c",
            "b5d31cde873073718c033076656a27471e392afc",
            "7317cdf6f62be93b22295062e191f6ba59a5db26",
            "1cdb5d0bdcb484907d0a2fea4efdfe0153838642",
            "b516294f541a18ce00b71a60b2c82ad2f87ff78d",
            "91e42cc450075f2c4c245bac7617bf903f16b4ce",
            "b6e207c5f8c58e7c8362cd05a1501bf2f5b694f2",
            "25f384bd95e218f6ac37fcaca99ed40f36760d8c",
            "f33657a2fcbab2dc3ce555d5d6728f8758af7bc7",
            "294f5c164da5ac9735658b2d58d8fee6745dfc45",
            "119a2e8c0b50f78d3861636d37c3b44ba8e689b5",
            "7b23cca3f49059f005e812be03931c81272eaac4",
            "fb0efeaa3788d1292bb49a94d77622503fe08129",
            "797a528c49b6535560f7fd8222b121b0223287c8",
            "bc490776f367b8afccf0ea7349d657431ba1ded6",
            "48c7ea8bc2c4adba09bf21a29689e3b8c2967522",
            "bf7490f905904e79de5c90e472bb9e6f26e634a0",
            "df961194a798cc76306b9290701130c592530fb6",
            "cf0be9078d76951e2e228df68b5b0bbf71313aaa",
            "d746ac8d782f94d12d176c7a591f5bf8365bef3d",
            "1f30267438257020f08abf452746a48e53a71ad5",
            "4ad942982e43326c7700b1b6443049b3cfd82161",
            "1cdb5d0bdcb484907d0a2fea4efdfe0153838642",
            "6616707e1109aaa1c11b9f399f914d0cfb4f5303",
            "4ee7cfaf1fc50a6df858ed0b99c278d633bccca9",
            "b7d02b4a0036114732c072269adb891dc5e34ca4",
            "9c51c1a119137cd17ed5ae09daa80c1cab6ac01d",
            "5f2a5c14b971a6f5eed0a67dc7af3a3fe5c0b6a4",
            "0e2b5b4cf59e859860000ff123dc12a317ad416b",
            "3fe68ad3da93065e35c37b14fbeef88b4b7785ed",
            "fd7ffcbafb74412a8812df4720feaf11fe70fe12",
            "b4c30fe36b79870a79c55e1e909adb5ad23f323f",
            "92c0f2f3239f1f61496997bd2cdc197ec51bd423",
            "29193e9240a71a735639c66ee954e68414f11236",
            "25f384bd95e218f6ac37fcaca99ed40f36760d8c",
            "87b13b074936b1f42a7c6758c7c2995f58c602e7",
            "8d935c6c30510e1dfc10f803a9646fa8aa128b07",
            "8f4fecfc836ea33798ee3e5cef56926fa54e2cf9",
            "9a5dfcb1a4b29c3a1598392d039744b9938b5a26",
            "c5724e280283cd985186af9a62494aae377daabd",
            "b39833be3032bbe1005f4f719f379a4621faeb13",
            "589276465a23c589801d874f484cc39f307d7ec7",
            "fb364ef587872515f545a5b4b3182b58073f230f",
            "6c0bf1fed2705d675da950a94e4af004ec975a06",
            "b924ac36b7499591f7929d9c4903de79b07b1cb9",
            "113515e893b8186595595e594ecc410bae50c026",
            "72f9815b087b6d346745e3de71a6ce5fe73a8677",
            "b0198a465290f065378f3535666bee0653d6a9bb",
            "020ebeb40642bfb4bc3d9f6d28c098afc0a47481",
            "5c15e604c4207f52c8cf58fe21e63164c230e257",
            "273a2fa759f1a9f56b078633ea2f08fc2406002a",
            "930fc43ca7bae20d4706543e97175d1872b0671f",
            "0cb2ec46f06ba85520a1c1a56706acf35d5176dd",
            "057053dfb657aaa98553e2c544b06e1a2371557e",
            "64e091964a611a48424d254a3de2b952d0d6565a",
            "50ebba2b06908f93d7963b1c6826cc0fd6104477",
            "374ff82251a384601da73f30485c3ac8d7f4176b",
            "1487afc813124abbeb0629d2172be0f01ccec3bf",
            "6a4fa1a68b92336e64006a4310cb160b07854329",
            "15f986a262fc6eff5774050c94d174c0533d505d",
            "6184648aab0431c4c95c649072d1f9ff08b9bb7c",
            "1efa9b640ad980b2ec53834d60e9cff9554979cd",
            "064d4999ea15e433e06f16f391922390acab01cb",
            "4884e30b93b3c4c123a83154516196095f9e831e",
            "2827bfc459c12d7c6d280cbacee750811291d4ba",
            "9626816275585ac3443e7cddd1272c8652c23f1d",
            "a2e1bb1fed32c6a6290b679785dd31ca5c59cb5f",
            "d9535951222dd7a1ff7f763872cb0df44f7962bf",
            "243b6f18093ff97c861d0568c7d3379606201a4b",
            "1ce5ac7bab7f2e82af02305ced3c095e320b52e5",
            "3e63119a8503a6f666b0a736c8fdeb9e79d11eb4",
            "e33372c0d8b2cdd3e12252962ee1671d66143075",
            "7364ba2ac9090e468855ce9074bb50c306196f4c",
            "d6350f9158825662b99e4b5e0442bcc94d39bc11",
            "2a294ea41312c6da8de5ebd0f18dbfb2963bb1a4",
            "44c49bbab8a3e9999f3ba9dee0186288b1d960a7",
            "2305db36455aa0d18571015b9e9bd0950262aa0f",
            "82f1d8c257d3e76b711a5cecd1e49bd3fa6a9de9",
            "faed2b028a9b5a712d5180eaa6fd2aa619f941bc",
            "39ac239b5687f7d1c2ba74cd020b3547545dfdaf",
            "5737eb22f6119f27c9afccfe73ba710afd885371",
            "5f22b6daf7204d73cf79d3ff0b46fc4fe237c7f7",
            "3583f5a570af6870504eea5a5f7afad6e1508508",
            "b0e489f986c345aef23c4a48d91cbcf5a6fdb9ac",
            "ba79c80788a9e1751e49ad401f5692d86f73a2db",
            "c54323436f50c633c870298bb374ac8e7560e6cd",
            "83725c7ee23bd4a8ca28a4fab0e313409def1dc7",
            "61c4e8636704f2f38bbe88b1f30ef0d74d6c0f49",
            "87ef9ba36019f7f3bf217cf47511645893b13f2e",
            "1bd0adfcf1c75bafc1ba3fc9b65a1a0470df6a91",
            "1be15348c51955179b7bf9aa90230a9425927ef6",
            "1f45ab3495df2c3be7d9c882bca9966305115cbb",
            "7317777e3751efa66218f6da5ef0d01dda69af48",
            "3346a4ff80b70ee7eea8a79fc79f19c43bb4464a",
            "452916a904030d2ce4ea2440fad2d0774e7296d9",
            "2675ef3adf52ebf8a44ff5da4306c293dfa6f901",
            "e2a76643735f8611a561794509c6bb2aac70eb04",
            "dc577db3caf5ff83a3b573ba92f2d447f067eee1",
            "89c4cf244099918b1d3ed413df27d4216e97b499",
            "4b2b5822c8af3074c6ef9b789a8142d0ef623402",
            "3c5794f775975669745c412b0c30f48991d9e455",
            "1df464dbb302ced815c61431a5548a273e6de8e1"
    );
}
