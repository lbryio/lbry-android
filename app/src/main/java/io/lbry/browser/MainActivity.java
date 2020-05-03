package io.lbry.browser;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.PictureInPictureParams;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.preference.PreferenceManager;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import io.lbry.browser.adapter.NavigationMenuAdapter;
import io.lbry.browser.adapter.UrlSuggestionListAdapter;
import io.lbry.browser.data.DatabaseHelper;
import io.lbry.browser.dialog.ContentScopeDialogFragment;
import io.lbry.browser.exceptions.LbryUriException;
import io.lbry.browser.listener.SdkStatusListener;
import io.lbry.browser.listener.WalletBalanceListener;
import io.lbry.browser.model.Claim;
import io.lbry.browser.model.ClaimCacheKey;
import io.lbry.browser.model.NavMenuItem;
import io.lbry.browser.model.Tag;
import io.lbry.browser.model.UrlSuggestion;
import io.lbry.browser.model.WalletBalance;
import io.lbry.browser.model.WalletSync;
import io.lbry.browser.model.lbryinc.Subscription;
import io.lbry.browser.tasks.LighthouseAutoCompleteTask;
import io.lbry.browser.tasks.MergeSubscriptionsTask;
import io.lbry.browser.tasks.ResolveTask;
import io.lbry.browser.tasks.wallet.DefaultSyncTaskHandler;
import io.lbry.browser.tasks.wallet.LoadSharedUserStateTask;
import io.lbry.browser.tasks.wallet.SaveSharedUserStateTask;
import io.lbry.browser.tasks.wallet.SyncApplyTask;
import io.lbry.browser.tasks.wallet.SyncGetTask;
import io.lbry.browser.tasks.wallet.SyncSetTask;
import io.lbry.browser.tasks.wallet.WalletBalanceTask;
import io.lbry.browser.ui.BaseFragment;
import io.lbry.browser.ui.channel.ChannelFragment;
import io.lbry.browser.ui.editorschoice.EditorsChoiceFragment;
import io.lbry.browser.ui.following.FollowingFragment;
import io.lbry.browser.ui.search.SearchFragment;
import io.lbry.browser.ui.settings.SettingsFragment;
import io.lbry.browser.ui.allcontent.AllContentFragment;
import io.lbry.browser.ui.wallet.WalletFragment;
import io.lbry.browser.utils.Helper;
import io.lbry.browser.utils.Lbry;
import io.lbry.browser.utils.LbryUri;
import io.lbry.browser.utils.Lbryio;
import io.lbry.lbrysdk.LbrynetService;
import io.lbry.lbrysdk.ServiceHelper;
import io.lbry.lbrysdk.Utils;
import lombok.Getter;

public class MainActivity extends AppCompatActivity implements SdkStatusListener {

    public static SimpleExoPlayer appPlayer;
    public static Claim nowPlayingClaim;
    public static boolean startingShareActivity = false;
    public static boolean startingFileViewActivity = false;
    public static boolean mainActive = false;
    private boolean enteringPIPMode = false;

    private Map<String, Fragment> openNavFragments;
    private static final Map<Class, Integer> fragmentClassNavIdMap = new HashMap<>();
    static {
        fragmentClassNavIdMap.put(FollowingFragment.class, NavMenuItem.ID_ITEM_FOLLOWING);
        fragmentClassNavIdMap.put(WalletFragment.class, NavMenuItem.ID_ITEM_WALLET);
        fragmentClassNavIdMap.put(SettingsFragment.class, NavMenuItem.ID_ITEM_SETTINGS);
        fragmentClassNavIdMap.put(AllContentFragment.class, NavMenuItem.ID_ITEM_ALL_CONTENT);

        // Internal (sub-)pages
        fragmentClassNavIdMap.put(ChannelFragment.class, NavMenuItem.ID_ITEM_FOLLOWING);
        fragmentClassNavIdMap.put(SearchFragment.class, NavMenuItem.ID_ITEM_FOLLOWING);
    }

    public static final int REQUEST_SIMPLE_SIGN_IN = 2001;
    public static final int REQUEST_WALLET_SYNC_SIGN_IN = 2002;

    // broadcast action names
    public static final String ACTION_SDK_READY = "io.lbry.browser.Broadcast.SdkReady";
    public static final String ACTION_AUTH_TOKEN_GENERATED = "io.lbry.browser.Broadcast.AuthTokenGenerated";
    public static final String ACTION_USER_AUTHENTICATION_SUCCESS = "io.lbry.browser.Broadcast.UserAuthenticationSuccess";
    public static final String ACTION_USER_SIGN_IN_SUCCESS = "io.lbry.browser.Broadcast.UserSignInSuccess";
    public static final String ACTION_USER_AUTHENTICATION_FAILED = "io.lbry.browser.Broadcast.UserAuthenticationFailed";
    public static final String ACTION_NOW_PLAYING_CLAIM_UPDATED = "io.lbry.browser.Broadcast.NowPlayingClaimUpdated";
    public static final String ACTION_NOW_PLAYING_CLAIM_CLEARED = "io.lbry.browser.Broadcast.NowPlayingClaimCleared";
    public static final String ACTION_OPEN_ALL_CONTENT_TAG = "io.lbry.browser.Broadcast.OpenAllContentTag";
    public static final String ACTION_WALLET_BALANCE_UPDATED = "io.lbry.browser.Broadcast.WalletBalanceUpdated";

    // preference keys
    public static final String PREFERENCE_KEY_DARK_MODE = "io.lbry.browser.preference.userinterface.DarkMode";
    public static final String PREFERENCE_KEY_SHOW_MATURE_CONTENT = "io.lbry.browser.preference.userinterface.ShowMatureContent";
    public static final String PREFERENCE_KEY_NOTIFICATION_URL_SUGGESTIONS = "io.lbry.browser.preference.userinterface.UrlSuggestions";
    public static final String PREFERENCE_KEY_NOTIFICATION_SUBSCRIPTIONS = "io.lbry.browser.preference.notifications.Subscriptions";
    public static final String PREFERENCE_KEY_NOTIFICATION_REWARDS = "io.lbry.browser.preference.notifications.Rewards";
    public static final String PREFERENCE_KEY_NOTIFICATION_CONTENT_INTERESTS = "io.lbry.browser.preference.notifications.ContentInterests";
    public static final String PREFERENCE_KEY_KEEP_SDK_BACKGROUND = "io.lbry.browser.preference.other.KeepSdkInBackground";
    public static final String PREFERENCE_KEY_PARTICIPATE_DATA_NETWORK = "io.lbry.browser.preference.other.ParticipateInDataNetwork";

    // Internal flags / setting preferences
    public static final String PREFERENCE_KEY_INTERNAL_SKIP_WALLET_ACCOUNT = "io.lbry.browser.preference.internal.WalletSkipAccount";
    public static final String PREFERENCE_KEY_INTERNAL_WALLET_SYNC_ENABLED = "io.lbry.browser.preference.internal.WalletSyncEnabled";
    public static final String PREFERENCE_KEY_INTERNAL_WALLET_RECEIVE_ADDRESS = "io.lbry.browser.preference.internal.WalletReceiveAddress";

    private final int CHECK_SDK_READY_INTERVAL = 1000;

    public static final String PREFERENCE_KEY_FIRST_RUN_COMPLETED = "io.lbry.browser.Preference.FirstRunCompleted";
    public static final String PREFERENCE_KEY_AUTH_TOKEN = "io.lbry.browser.Preference.AuthToken";

    public static final String SECURE_VALUE_KEY_SAVED_PASSWORD = "io.lbry.browser.PX";

    private static final String TAG = "io.lbry.browser.Main";

    private NavigationMenuAdapter navMenuAdapter;
    private UrlSuggestionListAdapter urlSuggestionListAdapter;
    private List<UrlSuggestion> recentHistory;
    private boolean hasLoadedFirstBalance;

    // broadcast receivers
    private BroadcastReceiver serviceActionsReceiver;
    private BroadcastReceiver requestsReceiver;
    private BroadcastReceiver userActionsReceiver;

    private boolean userAuthenticated = false;

    private boolean appStarted;
    private boolean serviceRunning;
    private CheckSdkReadyTask checkSdkReadyTask;
    private boolean receivedStopService;
    private AppBarConfiguration mAppBarConfiguration;
    private ActionBarDrawerToggle toggle;
    @Getter
    private DatabaseHelper dbHelper;
    private int selectedMenuItemId = -1;
    private List<SdkStatusListener> sdkStatusListeners;
    private List<WalletBalanceListener> walletBalanceListeners;
    @Getter
    private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private boolean walletBalanceUpdateScheduled;
    private boolean walletSyncScheduled;
    private String pendingAllContentTag;
    private String pendingChannelUrl;
    private boolean pendingFollowingReload;

    private final List<Integer> supportedMenuItemIds = Arrays.asList(
            NavMenuItem.ID_ITEM_FOLLOWING,
            NavMenuItem.ID_ITEM_EDITORS_CHOICE,
            NavMenuItem.ID_ITEM_ALL_CONTENT,
            NavMenuItem.ID_ITEM_WALLET,
            NavMenuItem.ID_ITEM_SETTINGS
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        boolean darkMode = sp.getBoolean(PREFERENCE_KEY_DARK_MODE, false);
        AppCompatDelegate.setDefaultNightMode(darkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

        initKeyStore();
        loadAuthToken();

        dbHelper = new DatabaseHelper(this);
        if (!darkMode) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // register receivers
        registerRequestsReceiver();
        registerServiceActionsReceiver();
        registerUserActionsReceiver();

        // setup uri bar
        setupUriBar();

        // other
        openNavFragments = new HashMap<>();
        sdkStatusListeners = new ArrayList<>();
        walletBalanceListeners = new ArrayList<>();

        sdkStatusListeners.add(this);

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.addOnBackStackChangedListener(backStackChangedListener);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                if (shouldOpenUserSelectedMenuItem) {
                    openSelectedMenuItem();
                    shouldOpenUserSelectedMenuItem = false;
                }
            }
        };
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        toggle.setToolbarNavigationClickListener((view) -> {
            if (toggle != null && !toggle.isDrawerIndicatorEnabled()) {
                FragmentManager manager = getSupportFragmentManager();
                if (manager != null) {
                    manager.popBackStack();
                    setSelectedNavMenuItemForFragment(getCurrentFragment());
                }
            }
        });

        findViewById(R.id.global_now_playing_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopExoplayer();
                nowPlayingClaim = null;
                findViewById(R.id.global_now_playing_card).setVisibility(View.GONE);
            }
        });

        findViewById(R.id.global_now_playing_card).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (nowPlayingClaim != null) {
                    Intent intent = new Intent(MainActivity.this, FileViewActivity.class);
                    intent.putExtra("claimId", nowPlayingClaim.getClaimId());
                    intent.putExtra("url", nowPlayingClaim.getPermanentUrl());
                    startingFileViewActivity = true;
                    startActivity(intent);
                }
            }
        });

        // display custom navigation menu
        LinearLayoutManager llm = new LinearLayoutManager(this);
        RecyclerView navItemsView = findViewById(R.id.nav_view_items);
        navItemsView.setLayoutManager(llm);
        navMenuAdapter = new NavigationMenuAdapter(flattenNavMenu(buildNavMenu(this)), this);
        navMenuAdapter.setListener(new NavigationMenuAdapter.NavigationMenuItemClickListener() {
            @Override
            public void onNavigationMenuItemClicked(NavMenuItem menuItem) {
                if (navMenuAdapter.getCurrentItemId() == menuItem.getId() && menuItem.getId() != NavMenuItem.ID_ITEM_ALL_CONTENT) {
                    // already open
                    navMenuAdapter.setCurrentItem(menuItem);
                    closeDrawer();
                    return;
                }

                if (!supportedMenuItemIds.contains(menuItem.getId())) {
                    Snackbar.make(navItemsView, R.string.not_yet_implemented, Snackbar.LENGTH_LONG).show();
                } else {
                    navMenuAdapter.setCurrentItem(menuItem);
                    shouldOpenUserSelectedMenuItem = true;
                    selectedMenuItemId = menuItem.getId();
                }
                closeDrawer();
            }
        });
        navItemsView.setAdapter(navMenuAdapter);

        findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                walletSyncSignIn();
            }
        });
    }

    private boolean shouldOpenUserSelectedMenuItem;

    public void addSdkStatusListener(SdkStatusListener listener) {
        if (!sdkStatusListeners.contains(listener)) {
            sdkStatusListeners.add(listener);
        }
    }

    public void removeSdkStatusListener(SdkStatusListener listener) {
        sdkStatusListeners.remove(listener);
    }

    public void addWalletBalanceListener(WalletBalanceListener listener) {
        if (!walletBalanceListeners.contains(listener)) {
            walletBalanceListeners.add(listener);
        }
    }

    public void removeWalletBalanceListener(WalletBalanceListener listener) {
        walletBalanceListeners.remove(listener);
    }

    private void openSelectedMenuItem() {
        switch (selectedMenuItemId) {
            // TODO: reverse map lookup for class?
            case NavMenuItem.ID_ITEM_FOLLOWING:
                openFragment(FollowingFragment.class, true, NavMenuItem.ID_ITEM_FOLLOWING);
                break;
            case NavMenuItem.ID_ITEM_EDITORS_CHOICE:
                openFragment(EditorsChoiceFragment.class, true, NavMenuItem.ID_ITEM_EDITORS_CHOICE);
                break;
            case NavMenuItem.ID_ITEM_ALL_CONTENT:
                openFragment(AllContentFragment.class, true, NavMenuItem.ID_ITEM_ALL_CONTENT);
                break;
            case NavMenuItem.ID_ITEM_WALLET:
                openFragment(WalletFragment.class, true, NavMenuItem.ID_ITEM_WALLET);
                break;

            case NavMenuItem.ID_ITEM_SETTINGS:
                openFragment(SettingsFragment.class, true, NavMenuItem.ID_ITEM_SETTINGS);
                break;
        }
    }

    public void openChannelClaim(Claim claim) {
        Map<String, Object> params = new HashMap<>();
        params.put("url", claim.getPermanentUrl());
        params.put("claim", getCachedClaimForUrl(claim.getPermanentUrl()));
        openFragment(ChannelFragment.class, true, NavMenuItem.ID_ITEM_FOLLOWING, params);
        setWunderbarValue(claim.getShortUrl());
    }

    public void openChannelUrl(String url) {
        Map<String, Object> params = new HashMap<>();
        params.put("url", url);
        params.put("claim", getCachedClaimForUrl(url));
        openFragment(ChannelFragment.class, true, NavMenuItem.ID_ITEM_FOLLOWING, params);
        setWunderbarValue(url); // TODO: Move this to fragment onResume
    }

    private Claim getCachedClaimForUrl(String url) {
        ClaimCacheKey key = new ClaimCacheKey();
        key.setCanonicalUrl(url);
        key.setPermanentUrl(url);
        key.setShortUrl(url);
        return Lbry.claimCache.containsKey(key) ? Lbry.claimCache.get(key) : null;
    }

    public void setWunderbarValue(String value) {
        EditText wunderbar = findViewById(R.id.wunderbar);
        wunderbar.setText(value);
        wunderbar.setSelection(0);
    }

    private void openAllContentFragmentWithTag(String tag) {
        Map<String, Object> params = new HashMap<>();
        params.put("singleTag", tag);
        openFragment(AllContentFragment.class, true, NavMenuItem.ID_ITEM_ALL_CONTENT, params);
    }

    public static void openFileUrl(String url, Context context) {
        Intent intent = new Intent(context, FileViewActivity.class);
        intent.putExtra("url", url);
        startingFileViewActivity = true;
        context.startActivity(intent);
    }

    public static void openFileClaim(Claim claim, Context context) {
        Intent intent = new Intent(context, FileViewActivity.class);
        intent.putExtra("claimId", claim.getClaimId());
        intent.putExtra("url", claim.getPermanentUrl());
        startingFileViewActivity = true;
        context.startActivity(intent);
    }

    private FragmentManager.OnBackStackChangedListener backStackChangedListener = new FragmentManager.OnBackStackChangedListener() {
        @Override
        public void onBackStackChanged() {
            FragmentManager manager = getSupportFragmentManager();
            if (manager != null) {
                Fragment currentFragment = getCurrentFragment();

            }
        }
    };

    public void setSelectedMenuItemForFragment(Fragment fragment) {
        if (fragment != null) {
            Class fragmentClass = fragment.getClass();
            if (fragmentClassNavIdMap.containsKey(fragmentClass)) {
                navMenuAdapter.setCurrentItem(fragmentClassNavIdMap.get(fragmentClass));
            }
        }
    }

    private void renderPictureInPictureMode() {
        findViewById(R.id.content_main).setVisibility(View.GONE);
        findViewById(R.id.global_now_playing_card).setVisibility(View.GONE);
        getSupportActionBar().hide();

        PlayerView pipPlayer = findViewById(R.id.pip_player);
        pipPlayer.setVisibility(View.VISIBLE);
        pipPlayer.setPlayer(appPlayer);
    }
    private void renderFullMode() {
        getSupportActionBar().show();
        findViewById(R.id.content_main).setVisibility(View.VISIBLE);
        findViewById(R.id.global_now_playing_card).setVisibility(View.VISIBLE);

        PlayerView pipPlayer = findViewById(R.id.pip_player);
        pipPlayer.setVisibility(View.INVISIBLE);
        pipPlayer.setPlayer(null);
    }

    @Override
    protected void onDestroy() {
        unregisterReceivers();
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (receivedStopService || !isServiceRunning(this, LbrynetService.class)) {
            notificationManager.cancelAll();
        }
        if (dbHelper != null) {
            dbHelper.close();
        }
        stopExoplayer();
        super.onDestroy();
    }

    private static void stopExoplayer() {
        if (appPlayer != null) {
            appPlayer.stop(true);
            appPlayer.release();
            appPlayer = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void updateWalletBalance() {
        WalletBalanceTask task = new WalletBalanceTask(new WalletBalanceTask.WalletBalanceHandler() {
            @Override
            public void onSuccess(WalletBalance walletBalance) {
                for (WalletBalanceListener listener : walletBalanceListeners) {
                    if (listener != null) {
                        listener.onWalletBalanceUpdated(walletBalance);
                    }
                }
                Lbry.walletBalance = walletBalance;
                updateFloatingWalletBalance();

                sendBroadcast(new Intent(ACTION_WALLET_BALANCE_UPDATED));
            }

            @Override
            public void onError(Exception error) {

            }
        });
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mainActive = true;

        checkFirstRun();
        checkNowPlaying();

        // check (and start) the LBRY SDK service
        serviceRunning = isServiceRunning(this, LbrynetService.class);
        if (!serviceRunning) {
            ServiceHelper.start(this, "", LbrynetService.class, "lbrynetservice");
        }
        checkSdkReady();
        showSignedInUser();

        if (!Helper.isNullOrEmpty(pendingAllContentTag)) {
            openAllContentFragmentWithTag(pendingAllContentTag);
            pendingAllContentTag = null;
        }
        if (pendingFollowingReload) {
            loadFollowingContent();
            pendingFollowingReload = false;
        }
    }

    @Override
    protected void onPause() {
        mainActive = false;
        super.onPause();
    }

    private void toggleUrlSuggestions(boolean visible) {
        View container = findViewById(R.id.url_suggestions_container);
        View closeIcon = findViewById(R.id.wunderbar_close);
        EditText wunderbar = findViewById(R.id.wunderbar);
        wunderbar.setPadding(0, 0, visible ? getScaledValue(36) : 0, 0);

        container.setVisibility(visible ? View.VISIBLE : View.GONE);
        closeIcon.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private int getScaledValue(int value) {
        float scale = getResources().getDisplayMetrics().density;
        return (int) (value * scale + 0.5f);
    }

    private void setupUriBar() {
        findViewById(R.id.wunderbar_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearWunderbarFocus(view);
            }
        });
        findViewById(R.id.wunderbar).setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                toggleUrlSuggestions(hasFocus);
                if (hasFocus && Helper.isNullOrEmpty(Helper.getValue(((EditText) view).getText()))) {
                    displayUrlSuggestionsForNoInput();
                }
            }
        });

        ((EditText) findViewById(R.id.wunderbar)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence != null) {
                    handleUriInputChanged(charSequence.toString().trim());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        urlSuggestionListAdapter = new UrlSuggestionListAdapter(this);
        urlSuggestionListAdapter.setListener(new UrlSuggestionListAdapter.UrlSuggestionClickListener() {
            @Override
            public void onUrlSuggestionClicked(UrlSuggestion urlSuggestion) {
                switch (urlSuggestion.getType()) {
                    case UrlSuggestion.TYPE_CHANNEL:
                        // open channel page
                        openChannelUrl(urlSuggestion.getUri().toString());
                        break;
                    case UrlSuggestion.TYPE_FILE:
                        Context context = MainActivity.this;
                        if (urlSuggestion.getClaim() != null) {
                            openFileClaim(urlSuggestion.getClaim(), context);
                        } else {
                            openFileUrl(urlSuggestion.getUri().toString(), context);
                        }
                        break;
                    case UrlSuggestion.TYPE_SEARCH:
                        launchSearch(urlSuggestion.getText());
                        break;
                    case UrlSuggestion.TYPE_TAG:
                        // open tag page
                        openAllContentFragmentWithTag(urlSuggestion.getText());
                        break;
                }
                clearWunderbarFocus(findViewById(R.id.wunderbar));
            }
        });

        RecyclerView urlSuggestionList = findViewById(R.id.url_suggestions);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        urlSuggestionList.setLayoutManager(llm);
        urlSuggestionList.setAdapter(urlSuggestionListAdapter);
    }

    private void clearWunderbarFocus(View view) {
        findViewById(R.id.wunderbar).clearFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void launchSearch(String text) {
        Fragment currentFragment = getCurrentFragment();
        if (currentFragment instanceof SearchFragment) {
            ((SearchFragment) currentFragment).search(text, 0);
        } else {
            try {
                SearchFragment fragment = SearchFragment.class.newInstance();
                fragment.setCurrentQuery(text);
                openFragment(fragment, true);
            } catch (Exception ex) {
                // pass
            }
        }
    }

    private void resolveUrlSuggestions(List<String> urls) {
        ResolveTask task = new ResolveTask(urls, Lbry.LBRY_TV_CONNECTION_STRING, null, new ResolveTask.ResolveResultHandler() {
            @Override
            public void onSuccess(List<Claim> claims) {
                if (findViewById(R.id.url_suggestions_container).getVisibility() == View.VISIBLE) {
                    for (int i = 0; i < claims.size(); i++) {
                        // build a simple url from the claim for matching
                        Claim claim = claims.get(i);
                        if (Helper.isNullOrEmpty(claim.getName())) {
                            continue;
                        }

                        LbryUri simpleUrl = new LbryUri();
                        if (claim.getName().startsWith("@")) {
                            // channel
                            simpleUrl.setChannelName(claim.getName());
                        } else {
                            simpleUrl.setStreamName(claim.getName());
                        }

                        urlSuggestionListAdapter.setClaimForUrl(simpleUrl, claim);
                    }
                    urlSuggestionListAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onError(Exception error) {

            }
        });
        task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }

    private void displayUrlSuggestionsForNoInput() {
        urlSuggestionListAdapter.clear();
        List<UrlSuggestion> blankSuggestions = buildDefaultSuggestionsForBlankUrl();
        urlSuggestionListAdapter.addUrlSuggestions(blankSuggestions);
        List<String> urls = urlSuggestionListAdapter.getItemUrls();
        resolveUrlSuggestions(urls);
    }

    private void handleUriInputChanged(String text) {
        // build the default suggestions
        urlSuggestionListAdapter.clear();
        if (Helper.isNullOrEmpty(text) || text.trim().equals("@")) {
            displayUrlSuggestionsForNoInput();
            return;
        }

        List<UrlSuggestion> defaultSuggestions = buildDefaultSuggestions(text);
        urlSuggestionListAdapter.addUrlSuggestions(defaultSuggestions);

        LighthouseAutoCompleteTask task = new LighthouseAutoCompleteTask(text, null, new LighthouseAutoCompleteTask.AutoCompleteResultHandler() {
            @Override
            public void onSuccess(List<UrlSuggestion> suggestions) {
                String wunderBarText = Helper.getValue(((EditText) findViewById(R.id.wunderbar)).getText());
                if (wunderBarText.equalsIgnoreCase(text)) {
                    urlSuggestionListAdapter.addUrlSuggestions(suggestions);
                    List<String> urls = urlSuggestionListAdapter.getItemUrls();
                    resolveUrlSuggestions(urls);
                }
            }

            @Override
            public void onError(Exception error) {

            }
        });
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private List<UrlSuggestion> buildDefaultSuggestionsForBlankUrl() {
        List<UrlSuggestion> suggestions = new ArrayList<>();
        if (recentHistory != null && recentHistory.size() > 0) {
            // show recent history if avaiable
            suggestions = new ArrayList<>(recentHistory);
        } else {
            try {
                suggestions.add(new UrlSuggestion(
                        UrlSuggestion.TYPE_FILE, "What is LBRY?", LbryUri.parse("lbry://what#19b9c243bea0c45175e6a6027911abbad53e983e")));
                suggestions.add(new UrlSuggestion(
                        UrlSuggestion.TYPE_CHANNEL, "LBRYCast", LbryUri.parse("lbry://@lbrycast#4c29f8b013adea4d5cca1861fb2161d5089613ea")));
                suggestions.add(new UrlSuggestion(
                        UrlSuggestion.TYPE_CHANNEL, "The LBRY Channel", LbryUri.parse("lbry://@lbry#3fda836a92faaceedfe398225fb9b2ee2ed1f01a")));
                for (UrlSuggestion suggestion : suggestions) {
                    suggestion.setUseTextAsDescription(true);
                }
            } catch (LbryUriException ex) {
                // pass
            }
        }
        return suggestions;
    }

    private List<UrlSuggestion> buildDefaultSuggestions(String text) {
        List<UrlSuggestion> suggestions = new ArrayList<UrlSuggestion>();

        // First item is always search
        if (!text.startsWith(LbryUri.PROTO_DEFAULT)) {
            UrlSuggestion searchSuggestion = new UrlSuggestion(UrlSuggestion.TYPE_SEARCH, text);
            suggestions.add(searchSuggestion);
        }

        if (!text.matches(LbryUri.REGEX_INVALID_URI)) {
            boolean isChannel = text.startsWith("@");
            if (!isChannel) {
                LbryUri uri = new LbryUri();
                uri.setStreamName(text);
                UrlSuggestion fileSuggestion = new UrlSuggestion(UrlSuggestion.TYPE_FILE, text);
                fileSuggestion.setUri(uri);
                suggestions.add(fileSuggestion);
            }

            if (text.indexOf(' ') == -1) {
                // channels and tags should not contain spaces
                if (isChannel) {
                    LbryUri uri = new LbryUri();
                    uri.setChannelName(text);
                    UrlSuggestion suggestion = new UrlSuggestion(UrlSuggestion.TYPE_CHANNEL, text);
                    suggestion.setUri(uri);
                    suggestions.add(suggestion);
                } else {
                    UrlSuggestion suggestion = new UrlSuggestion(UrlSuggestion.TYPE_TAG, text);
                    suggestions.add(suggestion);
                }
            }
        }

        return suggestions;
    }

    private void checkNowPlaying() {
        if (nowPlayingClaim != null) {
            findViewById(R.id.global_now_playing_card).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.global_now_playing_title)).setText(nowPlayingClaim.getTitle());
            ((TextView) findViewById(R.id.global_now_playing_channel_title)).setText(nowPlayingClaim.getPublisherTitle());
        }
        if (appPlayer != null) {
            PlayerView playerView = findViewById(R.id.global_now_playing_player_view);
            playerView.setPlayer(null);
            playerView.setPlayer(appPlayer);
            playerView.setUseController(false);
        }
    }

    private void initKeyStore() {
        try {
            Lbry.KEYSTORE = Utils.initKeyStore(this);
        } catch (Exception ex) {
            // This shouldn't happen, but in case it does.
            Toast.makeText(this, "The keystore could not be initialized. The app requires a secure keystore to run properly.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void checkFirstRun() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        boolean firstRunCompleted = sp.getBoolean(PREFERENCE_KEY_FIRST_RUN_COMPLETED, false);
        if (!firstRunCompleted) {
            startActivity(new Intent(this, FirstRunActivity.class));
        } else if (!appStarted) {
            // first run completed, startup
            startup();
        }
    }

    public static boolean isServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo serviceInfo : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(serviceInfo.service.getClassName())) {
                return true;
            }
        }

        return false;
    }

    private void loadAuthToken() {
        // Check if an auth token is present and then set it for Lbryio
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String encryptedAuthToken = sp.getString(PREFERENCE_KEY_AUTH_TOKEN, null);
        if (!Helper.isNullOrEmpty(encryptedAuthToken)) {
            try {
                Lbryio.AUTH_TOKEN = new String(Utils.decrypt(
                        Base64.decode(encryptedAuthToken, Base64.NO_WRAP), this, Lbry.KEYSTORE), "UTF8");
            } catch (Exception ex) {
                // pass. A new auth token would have to be generated if the old one cannot be decrypted
                android.util.Log.e(TAG, "Could not decrypt existing auth token.", ex);
            }
        }
    }

    private void checkSdkReady() {
        if (!Lbry.SDK_READY) {
            new Handler().postDelayed(() -> {
                if (checkSdkReadyTask != null && checkSdkReadyTask.getStatus() != AsyncTask.Status.FINISHED) {
                    // task already running
                    return;
                }
                checkSdkReadyTask = new CheckSdkReadyTask(MainActivity.this, sdkStatusListeners);
                checkSdkReadyTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }, CHECK_SDK_READY_INTERVAL);
        } else {
            scheduleWalletBalanceUpdate();
            scheduleWalletSyncTask();
            initFloatingWalletBalance();
        }
    }

    public void onSdkReady() {
        if (Lbryio.isSignedIn()) {
            checkSyncedWallet();
        }

        scheduleWalletBalanceUpdate();
        scheduleWalletSyncTask();
        initFloatingWalletBalance();
    }

    public void showFloatingWalletBalance() {
        findViewById(R.id.floating_balance_main_container).setVisibility(View.VISIBLE);
    }
    public void hideFloatingWalletBalance() {
        findViewById(R.id.floating_balance_main_container).setVisibility(View.GONE);
    }

    private void initFloatingWalletBalance() {

        findViewById(R.id.floating_balance_container).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFragment(WalletFragment.class, true, NavMenuItem.ID_ITEM_WALLET);
            }
        });
    }

    private void updateFloatingWalletBalance() {
        if (!hasLoadedFirstBalance) {
            findViewById(R.id.floating_balance_loading).setVisibility(View.GONE);
            findViewById(R.id.floating_balance_value).setVisibility(View.VISIBLE);
            hasLoadedFirstBalance = true;
        }

        ((TextView) findViewById(R.id.floating_balance_value)).setText(Helper.shortCurrencyFormat(
                Lbry.walletBalance == null ? 0 : Lbry.walletBalance.getAvailable().doubleValue()));
    }

    private void scheduleWalletBalanceUpdate() {
        if (scheduler != null && !walletBalanceUpdateScheduled) {
            scheduler.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    updateWalletBalance();
                }
            }, 0, 5, TimeUnit.SECONDS);
            walletBalanceUpdateScheduled = true;
        }
    }

    private void scheduleWalletSyncTask() {
        if (scheduler != null && !walletSyncScheduled) {
            scheduler.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    syncWalletAndLoadPreferences();
                }
            }, 0, 5, TimeUnit.MINUTES);
            walletSyncScheduled = true;
        }
    }

    public void saveSharedUserState() {
        if (!userSyncEnabled()) {
            return;
        }
        SaveSharedUserStateTask saveTask = new SaveSharedUserStateTask(new SaveSharedUserStateTask.SaveSharedUserStateHandler() {
            @Override
            public void onSuccess() {
                // push wallet sync changes
                pushCurrentWalletSync();
            }

            @Override
            public void onError(Exception error) {
                // pass
            }
        });
        saveTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void loadSharedUserState() {
        // load wallet preferences
        LoadSharedUserStateTask loadTask = new LoadSharedUserStateTask(MainActivity.this, new LoadSharedUserStateTask.LoadSharedUserStateHandler() {
            @Override
            public void onSuccess(List<Subscription> subscriptions, List<Tag> followedTags) {
                if (subscriptions != null && subscriptions.size() > 0) {
                    // reload subscriptions if wallet fragment is FollowingFragment
                    //openNavFragments.get
                    MergeSubscriptionsTask mergeTask = new MergeSubscriptionsTask(
                            subscriptions, MainActivity.this, new MergeSubscriptionsTask.MergeSubscriptionsHandler() {
                        @Override
                        public void onSuccess(List<Subscription> subscriptions, List<Subscription> diff) {
                            Lbryio.subscriptions = new ArrayList<>(subscriptions);
                            if (diff != null && diff.size() > 0) {
                                saveSharedUserState();
                            }
                            for (Fragment fragment : openNavFragments.values()) {
                                if (fragment instanceof FollowingFragment) {
                                    // reload local subscriptions
                                    ((FollowingFragment) fragment).fetchLoadedSubscriptions();
                                }
                            }
                        }

                        @Override
                        public void onError(Exception error) {
                            Log.e(TAG, String.format("merge subscriptions failed: %s", error.getMessage()), error);
                        }
                    });
                    mergeTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }

                if (followedTags != null && followedTags.size() > 0) {
                    List<Tag> previousTags = new ArrayList<>(Lbry.followedTags);
                    Lbry.followedTags = new ArrayList<>(followedTags);
                    for (Fragment fragment : openNavFragments.values()) {
                        if (fragment instanceof AllContentFragment) {
                            AllContentFragment acFragment = (AllContentFragment) fragment;
                            if (!acFragment.isSingleTagView() &&
                                    acFragment.getCurrentContentScope() == ContentScopeDialogFragment.ITEM_TAGS &&
                                    !previousTags.equals(followedTags)) {
                                acFragment.fetchClaimSearchContent(true);
                            }
                        }
                    }
                }
            }

            @Override
            public void onError(Exception error) {
                Log.e(TAG, String.format("load shared user state failed: %s", error != null ? error.getMessage() : "no error message"), error);
            }
        });
        loadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void pushCurrentWalletSync() {
        String password = Utils.getSecureValue(SECURE_VALUE_KEY_SAVED_PASSWORD, this, Lbry.KEYSTORE);
        SyncApplyTask fetchTask = new SyncApplyTask(true, password, new DefaultSyncTaskHandler() {
            @Override
            public void onSyncApplySuccess(String hash, String data) {
                SyncSetTask setTask = new SyncSetTask(Lbryio.lastRemoteHash, hash, data, null);
                setTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
            @Override
            public void onSyncApplyError(Exception error) { }
        });
        fetchTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private boolean userSyncEnabled() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        boolean walletSyncEnabled = sp.getBoolean(PREFERENCE_KEY_INTERNAL_WALLET_SYNC_ENABLED, false);
        return walletSyncEnabled && Lbryio.isSignedIn();
    }

    private void syncWalletAndLoadPreferences() {
        if (!userSyncEnabled()) {
            return;
        }

        String password = Utils.getSecureValue(SECURE_VALUE_KEY_SAVED_PASSWORD, this, Lbry.KEYSTORE);
        SyncGetTask task = new SyncGetTask(password, true, null, new DefaultSyncTaskHandler() {
            @Override
            public void onSyncGetSuccess(WalletSync walletSync) {
                Lbryio.lastWalletSync = walletSync;
                Lbryio.lastRemoteHash = walletSync.getHash();
                loadSharedUserState();
            }

            @Override
            public void onSyncGetWalletNotFound() {
                // pass. This actually shouldn't happen at this point.
            }

            @Override
            public void onSyncGetError(Exception error) {
                // pass
                Log.e(TAG, String.format("sync get failed: %s", error != null ? error.getMessage() : "no error message"), error);
            }

            @Override
            public void onSyncApplySuccess(String hash, String data) {
                if (!hash.equalsIgnoreCase(Lbryio.lastRemoteHash)) {
                    SyncSetTask setTask = new SyncSetTask(Lbryio.lastRemoteHash, hash, data, null);
                    setTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }

                loadSharedUserState();
            }

            @Override
            public void onSyncApplyError(Exception error) {
                // pass
                Log.e(TAG, String.format("sync apply failed: %s", error != null ? error.getMessage() : "no error message"), error);
            }
        });
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void registerRequestsReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_AUTH_TOKEN_GENERATED);
        intentFilter.addAction(ACTION_OPEN_ALL_CONTENT_TAG);
        intentFilter.addAction(ACTION_USER_SIGN_IN_SUCCESS);
        requestsReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (ACTION_AUTH_TOKEN_GENERATED.equalsIgnoreCase(action)) {
                    handleAuthTokenGenerated(intent);
                } else if (ACTION_OPEN_ALL_CONTENT_TAG.equalsIgnoreCase(action)) {
                    handleOpenContentTag(intent);
                } else if (ACTION_USER_SIGN_IN_SUCCESS.equalsIgnoreCase(action)) {
                    handleUserSignInSuccess(intent);
                }
            }

            private void handleAuthTokenGenerated(Intent intent) {
                // store the value
                String encryptedAuthToken = intent.getStringExtra("authToken");
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                sp.edit().putString(PREFERENCE_KEY_AUTH_TOKEN, encryptedAuthToken).apply();
            }

            private void handleOpenContentTag(Intent intent) {
                String tag = intent.getStringExtra("tag");
                if (!Helper.isNullOrEmpty(tag)) {
                    pendingAllContentTag = tag;
                }
            }
            private void handleUserSignInSuccess(Intent intent) {
                pendingFollowingReload = true;
            }
            private void handleOpenChannelUrl(String url) {
                pendingChannelUrl = url;
            }
        };
        registerReceiver(requestsReceiver, intentFilter);
    }

    private void loadFollowingContent() {
        for (Fragment fragment : openNavFragments.values()) {
            if (fragment instanceof FollowingFragment) {
                ((FollowingFragment) fragment).loadFollowing();
            }
        }
    }

    private void registerUserActionsReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_NOW_PLAYING_CLAIM_UPDATED);
        intentFilter.addAction(ACTION_NOW_PLAYING_CLAIM_CLEARED);
        userActionsReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (ACTION_NOW_PLAYING_CLAIM_UPDATED.equals(action)) {
                    handleNowPlayingClaimUpdated();
                } else if (ACTION_NOW_PLAYING_CLAIM_CLEARED.equals(action)) {
                    handleNowPlayingClaimCleared();
                }
            }

            private void handleNowPlayingClaimUpdated() {
                if (nowPlayingClaim != null) {
                    ((TextView) findViewById(R.id.global_now_playing_title)).setText(nowPlayingClaim.getTitle());
                    ((TextView) findViewById(R.id.global_now_playing_channel_title)).setText(nowPlayingClaim.getPublisherTitle());
                }
            }

            private void handleNowPlayingClaimCleared() {
                findViewById(R.id.global_now_playing_card).setVisibility(View.GONE);
                ((TextView) findViewById(R.id.global_now_playing_title)).setText(null);
                ((TextView) findViewById(R.id.global_now_playing_channel_title)).setText(null);
            }
        };
        registerReceiver(userActionsReceiver, intentFilter);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            // check fragment and nav history
            FragmentManager manager = getSupportFragmentManager();
            int backCount = getSupportFragmentManager().getBackStackEntryCount();
            if (backCount > 0) {
                // we can pop the stack
                manager.popBackStack();
                setSelectedNavMenuItemForFragment(getCurrentFragment());
            } else if (!enterPIPMode()) {
                // we're at the top of the stack
                moveTaskToBack(true);
                return;
            }
        }
    }

    public void simpleSignIn() {
        Intent intent = new Intent(this, VerificationActivity.class);
        intent.putExtra("flow", VerificationActivity.VERIFICATION_FLOW_SIGN_IN);
        startActivityForResult(intent, REQUEST_SIMPLE_SIGN_IN);
    }

    public void walletSyncSignIn() {
        Intent intent = new Intent(this, VerificationActivity.class);
        intent.putExtra("flow", VerificationActivity.VERIFICATION_FLOW_WALLET);
        startActivityForResult(intent, REQUEST_WALLET_SYNC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            // user signed in
            showSignedInUser();

            if (requestCode == REQUEST_WALLET_SYNC_SIGN_IN) {
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
                sp.edit().putBoolean(MainActivity.PREFERENCE_KEY_INTERNAL_WALLET_SYNC_ENABLED, true).apply();

                for (Fragment fragment : openNavFragments.values()) {
                    if (fragment instanceof WalletFragment) {
                        ((WalletFragment) fragment).onWalletSyncEnabled();
                    }
                }
                scheduleWalletSyncTask();
            }
        }
    }

    private void showSignedInUser() {
        if (Lbryio.isSignedIn()) {
            findViewById(R.id.sign_in_button).setVisibility(View.GONE);
            findViewById(R.id.signed_in_email_container).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.signed_in_email)).setText(Lbryio.getSignedInEmail());
            findViewById(R.id.sign_in_header_divider).setBackgroundColor(getResources().getColor(R.color.lightDivider));
        }
    }

    private Fragment getCurrentFragment() {
        int backCount = getSupportFragmentManager().getBackStackEntryCount();
        if (backCount > 0) {
            try {
                Fragment fragment = getSupportFragmentManager().getFragments().get(backCount - 1);
                return fragment;
            } catch (IndexOutOfBoundsException ex) {
                return null;
            }
        }
        return null;
    }

    public void hideActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
    }
    public void showActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.show();
        }
    }

    private void startup() {
        final Context context = this;
        // perform some tasks before launching
        (new AsyncTask<Void, Void, Boolean>() {
            protected void onPreExecute() {
                hideActionBar();
                lockDrawer();
                findViewById(R.id.splash_view).setVisibility(View.VISIBLE);
            }
            protected Boolean doInBackground(Void... params) {
                BufferedReader reader = null;
                try {
                    // Load the installation id from the file system
                    String lbrynetDir = String.format("%s/%s", Utils.getAppInternalStorageDir(context), "lbrynet");
                    String installIdPath = String.format("%s/install_id", lbrynetDir);
                    reader = new BufferedReader(new InputStreamReader(new FileInputStream(installIdPath)));
                    String installId = reader.readLine();
                    if (Helper.isNullOrEmpty(installId)) {
                        // no install_id found (first run didn't start the sdk successfully?)
                        return false;
                    }

                    SQLiteDatabase db = dbHelper.getReadableDatabase();
                    List<Tag> fetchedTags = DatabaseHelper.getTags(db);
                    Lbry.knownTags = Helper.mergeKnownTags(fetchedTags);
                    Collections.sort(Lbry.knownTags, new Tag());
                    Lbry.followedTags = Helper.filterFollowedTags(Lbry.knownTags);

                    // load the exchange rate
                    if (Lbryio.LBCUSDRate == 0) {
                        Lbryio.loadExchangeRate();
                    }

                    Lbry.INSTALLATION_ID = installId;
                    if (Lbryio.currentUser == null) {
                        Lbryio.authenticate(context);
                    }
                    Lbryio.newInstall(context);

                    // (light) fetch subscriptions
                    if (Lbryio.subscriptions.size() == 0) {
                        List<Subscription> subscriptions = new ArrayList<>();
                        List<String> subUrls = new ArrayList<>();
                        JSONArray array = (JSONArray) Lbryio.parseResponse(Lbryio.call("subscription", "list", context));
                        if (array != null) {
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject item = array.getJSONObject(i);
                                String claimId = item.getString("claim_id");
                                String channelName = item.getString("channel_name");

                                LbryUri url = new LbryUri();
                                url.setChannelName(channelName);
                                url.setClaimId(claimId);
                                subscriptions.add(new Subscription(channelName, url.toString()));
                                subUrls.add(url.toString());
                            }
                            Lbryio.subscriptions = subscriptions;

                            // resolve subscriptions
                            if (subUrls.size() > 0 && Lbryio.cacheResolvedSubscriptions.size() != Lbryio.subscriptions.size()) {
                                List<Claim> resolvedSubs = Lbry.resolve(subUrls, Lbry.LBRY_TV_CONNECTION_STRING);
                                Lbryio.cacheResolvedSubscriptions = resolvedSubs;
                            }
                        }
                    }
                } catch (Exception ex) {
                    // nope
                    android.util.Log.e(TAG, String.format("App startup failed: %s", ex.getMessage()), ex);
                    return false;
                } finally {
                    Helper.closeCloseable(reader);
                }

                return true;
            }
            protected void onPostExecute(Boolean startupSuccessful) {
                if (!startupSuccessful) {
                    Toast.makeText(context, R.string.startup_failed, Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }

                findViewById(R.id.splash_view).setVisibility(View.GONE);
                unlockDrawer();
                showActionBar();

                if (navMenuAdapter != null) {
                    navMenuAdapter.setCurrentItem(NavMenuItem.ID_ITEM_FOLLOWING);
                }

                loadLastFragment();
                showSignedInUser();

                appStarted = true;
            }
        }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void loadLastFragment() {
        Fragment fragment = getCurrentFragment();

        if (fragment != null) {
            openFragment(fragment, true);
        } else {
            openFragment(FollowingFragment.class, false, NavMenuItem.ID_ITEM_FOLLOWING);
        }
    }

    private void setSelectedNavMenuItemForFragment(Fragment fragment) {
        if (fragment == null) {
            // assume the first fragment is selected
            navMenuAdapter.setCurrentItem(NavMenuItem.ID_ITEM_FOLLOWING);
            return;
        }

        Class fragmentClass = fragment.getClass();
        if (fragmentClassNavIdMap.containsKey(fragmentClass)) {
            navMenuAdapter.setCurrentItem(fragmentClassNavIdMap.get(fragmentClass));
        }
    }

    @Override
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
        enterPIPMode();
    }

    protected boolean enterPIPMode() {
        if (enteringPIPMode) {
            return true;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                appPlayer != null &&
                FileViewActivity.instance == null &&
                !startingFileViewActivity) {
            enteringPIPMode = true;

            getSupportActionBar().hide();
            findViewById(R.id.global_now_playing_card).setVisibility(View.GONE);
            findViewById(R.id.pip_player).setVisibility(View.VISIBLE);

            PictureInPictureParams params = new PictureInPictureParams.Builder().build();
            enterPictureInPictureMode(params);
            return true;
        }

        return false;
    }

    private void registerServiceActionsReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LbrynetService.LBRY_SDK_SERVICE_STARTED);
        intentFilter.addAction(LbrynetService.ACTION_STOP_SERVICE);
        serviceActionsReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (LbrynetService.ACTION_STOP_SERVICE.equals(action)) {
                    MainActivity.this.receivedStopService = true;
                    MainActivity.this.finish();
                } else if (LbrynetService.LBRY_SDK_SERVICE_STARTED.equals(action)) {
                    // Rebuild the service notification
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Notification svcNotification = buildServiceNotification();
                            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                            notificationManager.notify(1, svcNotification);
                        }
                    }, 1000);
                }
            }
        };
        registerReceiver(serviceActionsReceiver, intentFilter);
    }

    private void unregisterReceivers() {
        Helper.unregisterReceiver(requestsReceiver, this);
        Helper.unregisterReceiver(serviceActionsReceiver, this);
        Helper.unregisterReceiver(userActionsReceiver, this);
    }

    private Notification buildServiceNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, LbrynetService.NOTIFICATION_CHANNEL_ID);
        Intent contextIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, contextIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent stopIntent = new Intent(LbrynetService.ACTION_STOP_SERVICE);
        PendingIntent stopPendingIntent = PendingIntent.getBroadcast(this, 0, stopIntent, 0);

        String serviceDescription = "The LBRY service is running in the background.";
        Notification notification = builder.setColor(ContextCompat.getColor(this, R.color.lbryGreen))
                .setContentIntent(pendingIntent)
                .setContentText(serviceDescription)
                .setGroup(LbrynetService.GROUP_SERVICE)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_lbry)
                .setOngoing(true)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", stopPendingIntent)
                .build();

        return notification;
    }

    private static List<NavMenuItem> buildNavMenu(Context context) {
        NavMenuItem findContentGroup = new NavMenuItem(NavMenuItem.ID_GROUP_FIND_CONTENT, R.string.find_content, true, context);
        NavMenuItem yourContentGroup = new NavMenuItem(NavMenuItem.ID_GROUP_YOUR_CONTENT, R.string.your_content, true, context);
        NavMenuItem walletGroup = new NavMenuItem(NavMenuItem.ID_GROUP_WALLET, R.string.wallet, true, context);
        NavMenuItem otherGroup = new NavMenuItem(NavMenuItem.ID_GROUP_OTHER, 0, true, context);

        findContentGroup.setItems(Arrays.asList(
                new NavMenuItem(NavMenuItem.ID_ITEM_FOLLOWING, R.string.fa_heart, R.string.following, "Following", context),
                new NavMenuItem(NavMenuItem.ID_ITEM_EDITORS_CHOICE, R.string.fa_star, R.string.editors_choice, "EditorsChoice", context),
                new NavMenuItem(NavMenuItem.ID_ITEM_ALL_CONTENT, R.string.fa_globe_americas, R.string.all_content, "AllContent", context)
        ));

        yourContentGroup.setItems(Arrays.asList(
                new NavMenuItem(NavMenuItem.ID_ITEM_NEW_PUBLISH, R.string.fa_upload, R.string.new_publish, "NewPublish", context),
                new NavMenuItem(NavMenuItem.ID_ITEM_CHANNELS, R.string.fa_at, R.string.channels, "Channels", context),
                new NavMenuItem(NavMenuItem.ID_ITEM_LIBRARY, R.string.fa_download, R.string.library, "Library", context),
                new NavMenuItem(NavMenuItem.ID_ITEM_PUBLISHES, R.string.fa_cloud_upload, R.string.publishes, "Publishes", context)
        ));

        walletGroup.setItems(Arrays.asList(
                new NavMenuItem(NavMenuItem.ID_ITEM_WALLET, R.string.fa_wallet, R.string.wallet, "Wallet", context),
                new NavMenuItem(NavMenuItem.ID_ITEM_REWARDS, R.string.fa_award, R.string.rewards, "Rewards", context),
                new NavMenuItem(NavMenuItem.ID_ITEM_INVITES, R.string.fa_user_friends, R.string.invites, "Invites", context)
        ));

        otherGroup.setItems(Arrays.asList(
                new NavMenuItem(NavMenuItem.ID_ITEM_SETTINGS, R.string.fa_cog, R.string.settings, "Settings", context),
                new NavMenuItem(NavMenuItem.ID_ITEM_ABOUT, R.string.fa_mobile_alt, R.string.about, "About", context)
        ));

        return Arrays.asList(findContentGroup, yourContentGroup, walletGroup, otherGroup);
    }

    // Flatten the structure into a single list for the RecyclerView
    private static List<NavMenuItem> flattenNavMenu(List<NavMenuItem> navMenuItems) {
        List<NavMenuItem> flatMenu = new ArrayList<>();
        for (NavMenuItem item : navMenuItems) {
            flatMenu.add(item);
            if (item.getItems() != null) {
                for (NavMenuItem subItem : item.getItems()) {
                    flatMenu.add(subItem);
                }
            }
        }

        return flatMenu;
    }

    public static void setNowPlayingClaim(Claim claim, Context context) {
        nowPlayingClaim = claim;
        context.sendBroadcast(new Intent(ACTION_NOW_PLAYING_CLAIM_UPDATED));
    }

    public static void clearNowPlayingClaim(Context context) {
        nowPlayingClaim = null;
        context.sendBroadcast(new Intent(ACTION_NOW_PLAYING_CLAIM_CLEARED));
    }

    private static class CheckSdkReadyTask extends AsyncTask<Void, Void, Boolean> {
        private Context context;
        private List<SdkStatusListener> listeners;

        public CheckSdkReadyTask(Context context, List<SdkStatusListener> listeners) {
            this.context = context;
            this.listeners = new ArrayList<>(listeners);
        }

        public Boolean doInBackground(Void... params) {
            boolean sdkReady = false;
            try {
                String response = Utils.sdkCall("status");
                if (response != null) {
                    JSONObject result = new JSONObject(response);
                    JSONObject status = result.getJSONObject("result");
                    if (!Lbry.IS_STATUS_PARSED) {
                        Lbry.parseStatus(status.toString());
                    }

                    // TODO: Broadcast startup status changes
                    JSONObject startupStatus = status.getJSONObject("startup_status");
                    sdkReady = startupStatus.getBoolean("stream_manager") && startupStatus.getBoolean("wallet");
                }
            } catch (ConnectException ex) {
                // pass
            } catch (JSONException ex) {
                // pass
            }

            return sdkReady;
        }
        protected void onPostExecute(Boolean sdkReady) {
            Lbry.SDK_READY = sdkReady;
            if (context != null) {
                if (sdkReady) {
                    context.sendBroadcast(new Intent(ACTION_SDK_READY));

                    // update listeners
                    for (SdkStatusListener listener : listeners) {
                        if (listener != null) {
                            listener.onSdkReady();
                        }
                    }
                } else if (context instanceof MainActivity) {
                    ((MainActivity) context).checkSdkReady();
                }
            }
        }
    }

    public void showNavigationBackIcon() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        if (toggle != null) {
            TypedArray a = getTheme().obtainStyledAttributes(R.style.AppTheme, new int[] {R.attr.homeAsUpIndicator});
            int attributeResourceId = a.getResourceId(0, 0);
            Drawable drawable = ResourcesCompat.getDrawable(getResources(), attributeResourceId, null);
            DrawableCompat.setTint(drawable, ContextCompat.getColor(this, R.color.actionBarForeground));

            toggle.setDrawerIndicatorEnabled(false);
            toggle.setHomeAsUpIndicator(drawable);
        }
    }

    private void closeDrawer() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }

    public void lockDrawer() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    public void unlockDrawer() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }

    public void restoreToggle() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setTitle(null);
        }
        if (toggle != null) {
            toggle.setDrawerIndicatorEnabled(true);
        }
        unlockDrawer();
        showSearchBar();
    }

    public void hideSearchBar() {
        findViewById(R.id.wunderbar_container).setVisibility(View.GONE);
    }

    public void showSearchBar() {
        findViewById(R.id.wunderbar_container).setVisibility(View.VISIBLE);
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, Configuration newConfig) {
        enteringPIPMode = false;
        if (isInPictureInPictureMode) {
            // Hide the full-screen UI (controls, etc.) while in picture-in-picture mode.
            renderPictureInPictureMode();
        } else {
            // Restore the full-screen UI.
            renderFullMode();
        }
    }

    protected void onStop() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (!MainActivity.startingFileViewActivity && appPlayer != null && isInPictureInPictureMode()) {
                appPlayer.setPlayWhenReady(false);
            }
        }
        super.onStop();
    }

    public void openFragment(Fragment fragment, boolean allowNavigateBack) {
        Fragment currentFragment = getCurrentFragment();
        if (currentFragment != null && currentFragment.equals(fragment)) {
            return;
        }

        try {
            FragmentManager manager = getSupportFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction().replace(R.id.content_main, fragment);
            if (allowNavigateBack) {
                transaction.addToBackStack(null);
            }
            transaction.commit();
        } catch (Exception ex) {
            // pass
        }
    }

    public void openFragment(Class fragmentClass, boolean allowNavigateBack, int navItemId) {
        openFragment(fragmentClass, allowNavigateBack, navItemId, null);
    }

    private static String buildNavFragmentKey(Class fragmentClass, int navItemId) {
        return String.format("%s-%d", fragmentClass.getName(), navItemId);
    }

    public void openFragment(Class fragmentClass, boolean allowNavigateBack, int navItemId, Map<String, Object> params) {
        try {
            String key = buildNavFragmentKey(fragmentClass, navItemId);
            Fragment fragment = openNavFragments.containsKey(key) ? openNavFragments.get(key) : (Fragment) fragmentClass.newInstance();
            if (fragment instanceof BaseFragment) {
                ((BaseFragment) fragment).setParams(params);
                }
            Fragment currentFragment = getCurrentFragment();
            if (currentFragment != null && currentFragment.equals(fragment)) {
                return;
            }

            fragment.setRetainInstance(true);
            FragmentManager manager = getSupportFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction().replace(R.id.content_main, fragment);
            if (allowNavigateBack) {
                transaction.addToBackStack(null);
            }
            transaction.commit();

            if (navItemId > -1) {
                openNavFragments.put(key, fragment);
            }
        } catch (Exception ex) {
            // pass
        }
    }

    private void checkSyncedWallet() {
        String password = Utils.getSecureValue(SECURE_VALUE_KEY_SAVED_PASSWORD, this, Lbry.KEYSTORE);
        // Just check if the current user has a synced wallet, no need to do anything else here
        SyncGetTask task = new SyncGetTask(password, false, null, new DefaultSyncTaskHandler() {
            @Override
            public void onSyncGetSuccess(WalletSync walletSync) {
                Lbryio.userHasSyncedWallet = true;
                Lbryio.setLastWalletSync(walletSync);
                Lbryio.setLastRemoteHash(walletSync.getHash());
            }

            @Override
            public void onSyncGetWalletNotFound() { }
            @Override
            public void onSyncGetError(Exception error) { }
        });
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}
