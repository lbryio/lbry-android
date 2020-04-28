package io.lbry.browser.utils;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.lbry.browser.exceptions.ApiCallException;
import io.lbry.browser.exceptions.LbryRequestException;
import io.lbry.browser.exceptions.LbryResponseException;
import io.lbry.browser.model.Claim;
import io.lbry.browser.model.ClaimCacheKey;
import io.lbry.browser.model.File;
import io.lbry.browser.model.Transaction;
import io.lbry.browser.model.WalletBalance;
import io.lbry.browser.model.lbryinc.User;
import io.lbry.lbrysdk.Utils;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public final class Lbry {
    public static final String TAG = "Lbry";
    public static LinkedHashMap<ClaimCacheKey, Claim> claimCache = new LinkedHashMap<>();
    public static LinkedHashMap<Map<String, Object>, List<Claim>> claimSearchCache = new LinkedHashMap<>();
    public static WalletBalance walletBalance = new WalletBalance();

    public static final String SDK_CONNECTION_STRING = "http://127.0.0.1:5279";
    public static final String LBRY_TV_CONNECTION_STRING = "https://api.lbry.tv/api/v1/proxy";

    // Values to obtain from LBRY SDK status
    public static boolean IS_STATUS_PARSED = false; // Check if the status has been parsed at least once
    public static final String PLATFORM = String.format("Android %s (API %d)", Utils.getAndroidRelease(), Utils.getAndroidSdk());
    public static final String OS = "android";
    public static String INSTALLATION_ID = null;
    public static String NODE_ID = null;
    public static String DAEMON_VERSION = null;

    // JSON RPC API Call methods
    public static final String METHOD_RESOLVE = "resolve";
    public static final String METHOD_CLAIM_SEARCH = "claim_search";
    public static final String METHOD_FILE_LIST = "file_list";
    public static final String METHOD_GET = "get";

    public static final String METHOD_WALLET_BALANCE = "wallet_balance";
    public static final String METHOD_WALLET_ENCRYPT = "wallet_encrypt";
    public static final String METHOD_WALLET_DECRYPT = "wallet_decrypt";

    public static final String METHOD_WALLET_LIST = "wallet_list";
    public static final String METHOD_WALLET_SEND = "wallet_send";
    public static final String METHOD_WALLET_STATUS = "wallet_status";
    public static final String METHOD_WALLET_UNLOCK = "wallet_unlock";
    public static final String METHOD_ADDRESS_IS_MINE = "address_is_mine";
    public static final String METHOD_ADDRESS_UNUSED = "address_unused";
    public static final String METHOD_ADDRESS_LIST = "address_list";
    public static final String METHOD_TRANSACTION_LIST = "transaction_list";
    public static final String METHOD_UTXO_RELEASE = "utxo_release";
    public static final String METHOD_SUPPORT_ABANDON = "support_abandon";
    public static final String METHOD_SYNC_HASH = "sync_hash";
    public static final String METHOD_SYNC_APPLY = "sync_apply";
    public static final String METHOD_PREFERENCE_GET = "preference_get";
    public static final String METHOD_PREFERENCE_SET = "preference_set";


    public static KeyStore KEYSTORE;
    public static boolean SDK_READY = false;

    public static void parseStatus(String response) {
        try {
            JSONObject json = parseSdkResponse(response);
            INSTALLATION_ID = json.getString("installation_id");
            if (json.has("lbry_id")) {
                // if DHT is not enabled, lbry_id won't be set
                NODE_ID = json.getString("lbry_id");
            }
            IS_STATUS_PARSED = true;
        } catch (JSONException | LbryResponseException ex) {
            // pass
            android.util.Log.e(TAG, "Could not parse status response.", ex);
        }
    }

    public static Response apiCall(String method) throws LbryRequestException {
        return apiCall(method, null);
    }

    public static Response apiCall(String method, Map<String, Object> params) throws LbryRequestException {
        return apiCall(method, params, SDK_CONNECTION_STRING);
    }

    public static Response apiCall(String method, Map<String, Object> params, String connectionString) throws LbryRequestException {
        long counter = new Double(System.currentTimeMillis() / 1000.0).longValue();
        JSONObject requestParams = buildJsonParams(params);
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("jsonrpc", "2.0");
            requestBody.put("method", method);
            requestBody.put("params", requestParams);
            requestBody.put("counter", counter);
        } catch (JSONException ex) {
            throw new LbryRequestException("Could not build the JSON request body.", ex);
        }

        RequestBody body = RequestBody.create(requestBody.toString(), Helper.JSON_MEDIA_TYPE);
        Request request =  new Request.Builder().url(connectionString).post(body).build();
        OkHttpClient client = new OkHttpClient.Builder().
                writeTimeout(300, TimeUnit.SECONDS).
                readTimeout(300, TimeUnit.SECONDS).
                build();

        try {
            return client.newCall(request).execute();
        } catch (IOException ex) {
            throw new LbryRequestException(String.format("\"%s\" method to %s failed", method, connectionString), ex);
        }
    }

    public static JSONObject buildJsonParams(Map<String, Object> params) {
        JSONObject jsonParams = new JSONObject();
        if (params != null) {
            try {
                for (Map.Entry<String, Object> param : params.entrySet()) {
                    Object value = param.getValue();
                    if (value instanceof List) {
                        value = Helper.jsonArrayFromList((List) value);
                    }
                    jsonParams.put(param.getKey(), value);
                }
            } catch (JSONException ex) {
                // pass
            }
        }

        return jsonParams;
    }

    public static Object parseResponse(Response response) throws LbryResponseException {
        String responseString = null;
        try {
            responseString = response.body().string();
            JSONObject json = new JSONObject(responseString);
            if (response.code() >= 200 && response.code() < 300) {
                if (json.has("result")) {
                    Object result = json.get("result");
                    return result;
                } else {
                    processErrorJson(json);
                }
            }

            processErrorJson(json);
        } catch (JSONException | IOException ex) {
            throw new LbryResponseException(String.format("Could not parse response: %s", responseString), ex);
        }

        return null;
    }

    private static void processErrorJson(JSONObject json) throws JSONException, LbryResponseException {
        if (json.has("error")) {
            String errorMessage = null;
            Object jsonError = json.get("error");
            if (jsonError instanceof String) {
                errorMessage = jsonError.toString();
            } else {
                errorMessage = ((JSONObject) jsonError).getString("message");
            }
            throw new LbryResponseException(json.getString("error"));
        } else {
            throw new LbryResponseException("Protocol error with unknown response signature.");
        }
    }

    public static JSONObject parseSdkResponse(String responseString) throws LbryResponseException {
        try {
            JSONObject json = new JSONObject(responseString);
            if (json.has("error")) {
                String errorMessage = null;
                Object jsonError = json.get("error");
                if (jsonError instanceof String) {
                    errorMessage = jsonError.toString();
                } else {
                    errorMessage = ((JSONObject) jsonError).getString("message");
                }
                throw new LbryResponseException(json.getString("error"));
            }

            return json;
        } catch (JSONException ex) {
            throw new LbryResponseException(String.format("Could not parse response: %s", responseString), ex);
        }
    }

    /**
     * API Calls
     */
    public static Claim resolve(String url, String connectionString) throws ApiCallException {
        List<Claim> results = resolve(Arrays.asList(url), connectionString);
        return results.size() > 0 ? results.get(0) : null;
    }
    public static List<Claim> resolve(List<String> urls, String connectionString) throws ApiCallException {
        List<Claim> claims = new ArrayList<>();
        Map<String, Object> params = new HashMap<>();
        params.put("urls", urls);
        try {
            JSONObject result = (JSONObject) parseResponse(apiCall(METHOD_RESOLVE, params, connectionString));
            Iterator<String> keys = result.keys();
            if (keys != null) {
                while (keys.hasNext()) {
                    Claim claim = Claim.fromJSONObject(result.getJSONObject(keys.next()));
                    claims.add(claim);

                    // TODO: Add/remove/prune entries claim cache management
                    ClaimCacheKey key = ClaimCacheKey.fromClaim(claim);
                    claimCache.put(key, claim);
                }
            }
        } catch (LbryRequestException | LbryResponseException | JSONException ex) {
            throw new ApiCallException("Could not execute resolve call", ex);
        }

        return claims;
    }
    public static List<Transaction> transactionList(int page, int pageSize) throws ApiCallException {
        List<Transaction> transactions = new ArrayList<>();
        Map<String, Object> params = new HashMap<>();
        if (page > 0) {
            params.put("page", page);
        }
        if (pageSize > 0) {
            params.put("page_size", pageSize);
        }
        try {
            JSONObject result = (JSONObject) parseResponse(apiCall(METHOD_TRANSACTION_LIST, params, SDK_CONNECTION_STRING));
            JSONArray items = result.getJSONArray("items");
            for (int i = 0; i < items.length(); i++) {
                Transaction tx = Transaction.fromJSONObject(items.getJSONObject(i));
                transactions.add(tx);
            }
        } catch (LbryRequestException | LbryResponseException | JSONException ex) {
            throw new ApiCallException("Could not execute transaction_list call", ex);
        }

        return transactions;
    }

    public static File get(boolean saveFile) throws ApiCallException {
        File file = null;
        Map<String, Object> params = new HashMap<>();
        params.put("save_file", saveFile);
        try {
            JSONObject result = (JSONObject) parseResponse(apiCall(METHOD_GET, params));
            file = File.fromJSONObject(result);

            if (file != null) {
                String fileClaimId = file.getClaimId();
                if (!Helper.isNullOrEmpty(fileClaimId)) {
                    ClaimCacheKey key = new ClaimCacheKey();
                    key.setClaimId(fileClaimId);
                    if (claimCache.containsKey(key)) {
                        claimCache.get(key).setFile(file);
                    }
                }
            }
        } catch (LbryRequestException | LbryResponseException ex) {
            throw new ApiCallException("Could not execute resolve call", ex);
        }

        return file;
    }

    public static List<File> fileList(String claimId) throws ApiCallException {
        List<File> files = new ArrayList<>();
        Map<String, Object> params = new HashMap<>();
        if (!Helper.isNullOrEmpty(claimId)) {
            params.put("claim_id", claimId);
        }
        try {
            JSONObject result = (JSONObject) parseResponse(apiCall(METHOD_FILE_LIST, params));
            JSONArray items = result.getJSONArray("items");
            for (int i = 0; i < items.length(); i++) {
                JSONObject fileObject = items.getJSONObject(i);
                File file = File.fromJSONObject(fileObject);
                files.add(file);

                String fileClaimId = file.getClaimId();
                if (!Helper.isNullOrEmpty(fileClaimId)) {
                    ClaimCacheKey key = new ClaimCacheKey();
                    key.setClaimId(fileClaimId);
                    if (claimCache.containsKey(key)) {
                        claimCache.get(key).setFile(file);
                    }
                }
            }
        } catch (LbryRequestException | LbryResponseException | JSONException ex) {
            throw new ApiCallException("Could not execute resolve call", ex);
        }

        return files;
    }

    private static final String[] listParamTypes = new String[] {
            "any_tags", "channel_ids", "order_by", "not_tags", "not_channel_ids", "urls"
    };

    public static Map<String, Object> buildClaimSearchOptions(
            String claimType,
            List<String> anyTags,
            List<String> notTags,
            List<String> channelIds,
            List<String> notChannelIds,
            List<String> orderBy,
            String releaseTime,
            int page,
            int pageSize) {
        Map<String, Object> options = new HashMap<>();
        if (!Helper.isNullOrEmpty(claimType)) {
            options.put("claim_type", claimType);
        }
        options.put("no_totals", true);
        options.put("page", page);
        options.put("page_size", pageSize);
        if (!Helper.isNullOrEmpty(releaseTime)) {
            options.put("release_time", releaseTime);
        }

        addClaimSearchListOption("any_tags", anyTags, options);
        addClaimSearchListOption("not_tags", notTags, options);
        addClaimSearchListOption("channel_ids", channelIds, options);
        addClaimSearchListOption("not_channel_ids", notChannelIds, options);
        addClaimSearchListOption("order_by", orderBy, options);

        return options;
    }

    private static void addClaimSearchListOption(String key, List<String> list, Map<String, Object> options) {
        if (list != null && list.size() > 0) {
            options.put(key, list);
        }
    }

    public static List<Claim> claimSearch(Map<String, Object> options, String connectionString) throws ApiCallException {
        if (claimSearchCache.containsKey(options)) {
            return claimSearchCache.get(options);
        }

        List<Claim> claims = new ArrayList<>();
        try {
            JSONObject result = (JSONObject) parseResponse(apiCall(METHOD_CLAIM_SEARCH, options, connectionString));
            JSONArray items = result.getJSONArray("items");
            if (items != null) {
                for (int i = 0; i < items.length(); i++) {
                    Claim claim = Claim.fromJSONObject(items.getJSONObject(i));
                    claims.add(claim);

                    ClaimCacheKey key = ClaimCacheKey.fromClaim(claim);
                    claimCache.put(key, claim);
                }
            }

            claimSearchCache.put(options, claims);
        } catch (LbryRequestException | LbryResponseException | JSONException ex) {
            throw new ApiCallException("Could not execute resolve call", ex);
        }

        return claims;
    }

    public static Map<String, Object> buildSingleParam(String key, Object value) {
        Map<String, Object> params = new HashMap<>();
        params.put(key, value);
        return params;
    }

    /**
     * Call to return a generic JSONObject which can be further parsed as required
     * @param method
     * @param params
     * @return
     */
    public static Object genericApiCall(String method, Map<String, Object> params) throws ApiCallException {
        Object response = null;
        try {
            response = parseResponse(apiCall(method, params));
        } catch (LbryRequestException | LbryResponseException ex) {
            throw new ApiCallException(String.format("Could not execute %s call", method), ex);
        }
        return response;
    }
    public static Object genericApiCall(String method) throws ApiCallException {
        return genericApiCall(method, null);
    }
}
