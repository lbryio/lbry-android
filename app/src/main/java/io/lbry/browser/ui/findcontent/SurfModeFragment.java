package io.lbry.browser.ui.findcontent;

import android.content.Context;
import android.content.SharedPreferences;
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
import io.lbry.browser.utils.Lbryio;
import io.lbry.browser.utils.Predefined;

public class SurfModeFragment extends BaseFragment {

    private static final int PAGE_SIZE = 50;

    private int currentClaimSearchPage;
    private int playlistIndex;
    private Claim current;
    private List<Claim> playlist;

    private ProgressBar surfModeLoading;
    private TextView textTitle;
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
        View root = inflater.inflate(R.layout.fragment_surf_mode, container, false);

        surfModeLoading = root.findViewById(R.id.surf_mode_loading);
        textTitle = root.findViewById(R.id.surf_mode_content_title);
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
        PlayerView playerView = root.findViewById(R.id.surf_mode_exoplayer_view);
        playerView.setOnTouchListener(new SwipeListener(playerView, context) {
            @Override
            public void onSwipeLeft() { playNextClaim(); }
            @Override
            public void onSwipeRight() { playPreviousClaim(); }
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
                Arrays.asList(Claim.ORDER_BY_TRENDING_GROUP),
                121, // 2 minutes or less
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
        if (playlist == null) {
            loadContent();
        } else {
            if (current != null) {
                playbackCurrentClaim();
            } else {
                startPlaylist();
            }
        }
    }

    public void onStop() {
        super.onStop();
        MainActivity activity = (MainActivity) getContext();
        if (activity != null) {
            activity.hideFloatingWalletBalance();
        }
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
        playbackCurrentClaim();
    }

    private void checkCurrentClaimIsVideo(boolean previous) {
        while (current == null || current.getMediaType() == null || !current.getMediaType().startsWith("video")) {
            // only play videos
            if (previous) {
                playlistIndex--;
            } else {
                playlistIndex++;
            }
            current = playlist.get(playlistIndex);
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
        playbackCurrentClaim();
    }
    private void playNextClaim() {
        if (playlist == null || playlist.size() == 0) {
            return;
        }
        if (playlistIndex < playlist.size() - 1) {
            playlistIndex++;
        }
        if (playlist.size() - playlistIndex < 10) {
            currentClaimSearchPage++;
            loadContent();
        }
        current = playlist.get(playlistIndex);
        checkCurrentClaimIsVideo(false);
        playbackCurrentClaim();
    }

    private void playbackCurrentClaim() {
        resetPlayer();
        textTitle.setText(current.getTitle());

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
            PlayerView view = root.findViewById(R.id.surf_mode_exoplayer_view);
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

        /*
        if (MainActivity.appPlayer != null) {
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

            PlayerView playerView = root.findViewById(R.id.surf_mode_exoplayer_view);
            playerView.findViewById(R.id.player_skip_back_10).setVisibility(View.INVISIBLE);
            playerView.findViewById(R.id.player_skip_forward_10).setVisibility(View.INVISIBLE);
        }
    }

    private void hideBuffering() {
        View root = getView();
        if (root != null) {
            root.findViewById(R.id.player_buffering_progress).setVisibility(View.INVISIBLE);

            PlayerView playerView = root.findViewById(R.id.surf_mode_exoplayer_view);
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
}
