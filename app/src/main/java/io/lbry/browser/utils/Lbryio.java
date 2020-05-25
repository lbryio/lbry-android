package io.lbry.browser.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.lbry.browser.MainActivity;
import io.lbry.browser.exceptions.LbryioRequestException;
import io.lbry.browser.exceptions.LbryioResponseException;
import io.lbry.browser.model.Claim;
import io.lbry.browser.model.WalletSync;
import io.lbry.browser.model.lbryinc.Reward;
import io.lbry.browser.model.lbryinc.Subscription;
import io.lbry.browser.model.lbryinc.User;
import io.lbry.lbrysdk.Utils;
import lombok.Data;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Data
public final class Lbryio {

    // TODO: Get this from the bundled aar
    public static String SDK_VERSION = "0.74.0";

    public static User currentUser;
    public static boolean userHasSyncedWallet = false;
    public static String lastRemoteHash;
    public static WalletSync lastWalletSync;
    public static final Object lock = new Object();

    public static final String TAG = "Lbryio";
    public static final String CONNECTION_STRING = "https://api.lbry.com";
    public static final String AUTH_TOKEN_PARAM = "auth_token";
    public static List<Subscription> subscriptions = new ArrayList<>();
    public static List<Claim> cacheResolvedSubscriptions = new ArrayList<>();
    public static double LBCUSDRate = 0;
    public static String AUTH_TOKEN;
    private static boolean generatingAuthToken = false;

    public static List<Reward> allRewards = new ArrayList<>();
    public static List<Reward> unclaimedRewards = new ArrayList<>();
    public static double totalUnclaimedRewardAmount = 0;

    public static Response call(String resource, String action, Context context) throws LbryioRequestException, LbryioResponseException {
        return call(resource, action, null, Helper.METHOD_GET, context);
    }

    public static Response call(String resource, String action, Map<String, String> options, Context context) throws LbryioRequestException, LbryioResponseException {
        return call(resource, action, options, Helper.METHOD_GET, context);
    }

    public static Response call(String resource, String action, Map<String, String> options, String method, Context context)
            throws LbryioRequestException, LbryioResponseException {
        String authToken = AUTH_TOKEN;
        if (Helper.isNullOrEmpty(authToken) && !generatingAuthToken) {
            // Only call getAuthToken if not calling /user/new
            authToken = getAuthToken(context);
        }

        String url = String.format("%s/%s/%s", CONNECTION_STRING, resource, action);
        if (Helper.METHOD_GET.equalsIgnoreCase(method)) {
            Uri.Builder uriBuilder = Uri.parse(url).buildUpon();
            if (!Helper.isNullOrEmpty(authToken)) {
                uriBuilder.appendQueryParameter(AUTH_TOKEN_PARAM, authToken);
            }
            if (options != null) {
                for (Map.Entry<String, String> option : options.entrySet()) {
                    uriBuilder.appendQueryParameter(option.getKey(), option.getValue());
                }
            }
            url = uriBuilder.build().toString();
        }

        Request.Builder builder = new Request.Builder().url(url);
        if (Helper.METHOD_POST.equalsIgnoreCase(method)) {
            RequestBody body = RequestBody.create(buildQueryString(authToken, options), Helper.FORM_MEDIA_TYPE);
            builder.post(body);
        }

        Request request = builder.build();
        OkHttpClient client = new OkHttpClient.Builder().
                writeTimeout(120, TimeUnit.SECONDS).
                readTimeout(120, TimeUnit.SECONDS).
                build();
        try {
            return client.newCall(request).execute();
        } catch (IOException ex) {
            throw new LbryioRequestException(String.format("%s request to %s/%s failed", method, resource, action), ex);
        }
    }

    private static String buildQueryString(String authToken, Map<String, String> options) {
        StringBuilder qs = new StringBuilder();
        try {
            String delim = "";
            if (!Helper.isNullOrEmpty(authToken)) {
                qs.append(AUTH_TOKEN_PARAM).append("=").append(URLEncoder.encode(authToken, "UTF8"));
                delim = "&";
            }

            if (options != null) {
                for (Map.Entry<String, String> option : options.entrySet()) {
                    qs.append(delim).append(option.getKey()).append("=").append(URLEncoder.encode(Helper.isNull(option.getValue()) ? "" : option.getValue(), "UTF8"));
                    delim = "&";
                }
            }
        } catch (UnsupportedEncodingException ex) {
            // pass
        }

        return qs.toString();
    }

    public static String getAuthToken(Context context) throws LbryioRequestException, LbryioResponseException {
        // fetch a new auth token
        if (Helper.isNullOrEmpty(Lbry.INSTALLATION_ID)) {
            throw new LbryioRequestException("The LBRY installation ID is not set.");
        }

        generatingAuthToken = true;

        Map<String, String> options = new HashMap<>();
        options.put("auth_token", "");
        options.put("language", "en");
        options.put("app_id", Lbry.INSTALLATION_ID);
        Response response = Lbryio.call("user", "new", options, "post", context);
        try {
            JSONObject json = (JSONObject) parseResponse(response);
            if (!json.has(AUTH_TOKEN_PARAM)) {
                throw new LbryioResponseException("auth_token was not set in the response");
            }

            AUTH_TOKEN = json.getString(AUTH_TOKEN_PARAM);
            broadcastAuthTokenGenerated(context);
        } catch (JSONException | ClassCastException ex) {
            LbryAnalytics.logError(String.format("/user/new failed: %s", ex.getMessage()), ex.getClass().getName());
            throw new LbryioResponseException("auth_token was not set in the response", ex);
        } finally {
            generatingAuthToken = false;
        }

        return AUTH_TOKEN;
    }

    public static Object parseResponse(Response response) throws LbryioResponseException {
        String responseString = null;
        try {
            responseString = response.body().string();
            JSONObject json = new JSONObject(responseString);
            if (response.code() >= 200 && response.code() < 300) {
                if (json.isNull("data")) {
                    return null;
                }
                return json.get("data");
            }

            if (json.has("error")) {
                throw new LbryioResponseException(json.getString("error"), response.code());
            } else {
                throw new LbryioResponseException("Unknown API error signature.", response.code());
            }
        } catch (JSONException | IOException ex) {
            throw new LbryioResponseException(String.format("Could not parse response: %s", responseString), ex);
        }
    }

    public static User fetchCurrentUser(Context context) {
        try {
            Response response = Lbryio.call("user", "me", context);
            JSONObject object = (JSONObject) parseResponse(response);
            Type type = new TypeToken<User>(){}.getType();
            Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
            User user = gson.fromJson(object.toString(), type);
            return user;
        } catch (LbryioRequestException | LbryioResponseException | ClassCastException | IllegalStateException ex) {
            LbryAnalytics.logError(String.format("/user/me failed: %s", ex.getMessage()), ex.getClass().getName());
            android.util.Log.e(TAG, "Could not retrieve the current user", ex);
            return null;
        }
    }

    public static void newInstall(Context context) {
        String appVersion = "";
        if (context != null) {
            try {
                PackageManager manager = context.getPackageManager();
                PackageInfo info = manager.getPackageInfo(context.getPackageName(), PackageManager.GET_ACTIVITIES);
                appVersion = info.versionName;
            } catch (PackageManager.NameNotFoundException ex) {

            }
        }

        Map<String, String> options = new HashMap<>();
        if (context instanceof MainActivity) {
            String firebaseToken = ((MainActivity) context).getFirebaseMessagingToken();
            if (!Helper.isNullOrEmpty(firebaseToken)) {
                options.put("firebase_token", firebaseToken);
            }
        }
        options.put("app_version", appVersion);
        options.put("app_id", Lbry.INSTALLATION_ID);
        options.put("node_id", "");
        options.put("daemon_version", SDK_VERSION);
        options.put("operating_system", "android");
        options.put("platform", String.format("Android %s (API %d)", Utils.getAndroidRelease(), Utils.getAndroidSdk()));
        try {
            JSONObject response = (JSONObject) parseResponse(call("install", "new", options, Helper.METHOD_POST, context));
        } catch (LbryioRequestException | LbryioResponseException | ClassCastException ex) {
            // pass
            Log.e(TAG, String.format("install/new failed: %s", ex.getMessage()), ex);
        }
    }

    public static String getSignedInEmail() {
        return currentUser != null ? currentUser.getPrimaryEmail() : "";
    }

    public static boolean isSignedIn() {
        return currentUser != null && currentUser.isHasVerifiedEmail();
    }

    public static void authenticate(Context context) {
        User user = fetchCurrentUser(context);
        if (user != null) {
            currentUser = user;
            if (context != null) {
                context.sendBroadcast(new Intent(MainActivity.ACTION_USER_AUTHENTICATION_SUCCESS));
            }
        } else {
            if (context != null) {
                context.sendBroadcast(new Intent(MainActivity.ACTION_USER_AUTHENTICATION_FAILED));
            }
        }
    }

    public static void loadExchangeRate() {
        try {
            JSONObject response = (JSONObject) parseResponse(Lbryio.call("lbc", "exchange_rate", null));
            LBCUSDRate = Helper.getJSONDouble("lbc_usd", 0, response);
        } catch (LbryioResponseException | LbryioRequestException | ClassCastException ex) {
            // pass
        }
    }

    private static void broadcastAuthTokenGenerated(Context context) {
        try {
            if (context != null) {
                String encryptedAuthToken = Utils.encrypt(AUTH_TOKEN.getBytes("UTF8"), context, Lbry.KEYSTORE);
                Intent intent = new Intent(MainActivity.ACTION_AUTH_TOKEN_GENERATED);
                intent.putExtra("authToken", encryptedAuthToken);
                context.sendBroadcast(intent);
            }
        } catch (Exception ex) {
            android.util.Log.e(TAG, "Error sending encrypted auth token action broadcast", ex);
            // pass
        }
    }

    public static Map<String, String> buildSingleParam(String key, String value) {
        Map<String, String> params = new HashMap<>();
        params.put(key, value);
        return params;
    }

    public static void setLastWalletSync(WalletSync walletSync) {
        synchronized (lock) {
            lastWalletSync = walletSync;
        }
    }

    public static void setLastRemoteHash(String hash) {
        synchronized (lock) {
            lastRemoteHash = hash;
        }
    }

    public static void addSubscription(Subscription subscription) {
        synchronized (lock) {
            if (!subscriptions.contains(subscription)) {
                subscriptions.add(subscription);
            }
        }
    }
    public static void removeSubscription(Subscription subscription) {
        synchronized (lock) {
            subscriptions.remove(subscription);
        }
    }
    public static void addCachedResolvedSubscription(Claim claim) {
        synchronized (lock) {
            if (!cacheResolvedSubscriptions.contains(claim)) {
                cacheResolvedSubscriptions.add(claim);
            }
        }
    }
    public static void removeCachedResolvedSubscription(Claim claim) {
        synchronized (lock) {
            cacheResolvedSubscriptions.remove(claim);
        }
    }

    public static boolean isFollowing(Subscription subscription) {
        return subscriptions.contains(subscription);
    }
    public static boolean isFollowing(Claim claim) {
        return subscriptions.contains(Subscription.fromClaim(claim));
    }

    public static void updateRewardsLists(List<Reward> rewards) {
        synchronized (lock) {
            allRewards.clear();
            unclaimedRewards.clear();
            totalUnclaimedRewardAmount = 0;
            for (int i = 0; i < rewards.size(); i++) {
                Reward reward = rewards.get(i);
                allRewards.add(reward);
                if (!reward.isClaimed()) {
                    unclaimedRewards.add(reward);
                    totalUnclaimedRewardAmount += reward.getRewardAmount();
                }
            }
        }
    }
}
