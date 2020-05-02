package io.lbry.browser.utils;

import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.lbry.browser.exceptions.LbryRequestException;
import io.lbry.browser.exceptions.LbryResponseException;
import io.lbry.browser.model.Claim;
import io.lbry.browser.model.UrlSuggestion;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Lighthouse {
    public static final String CONNECTION_STRING = "https://lighthouse.lbry.com";
    public static Map<String, List<UrlSuggestion>> autocompleteCache = new HashMap<>();
    public static Map<Map<String, Object>, List<Claim>> searchCache = new HashMap<>();

    private static Map<String, Object> buildSearchOptionsKey(String rawQuery, int size, int from, boolean nsfw, String relatedTo) {
        Map<String, Object> options = new HashMap<>();
        options.put("s", rawQuery);
        options.put("size", size);
        options.put("from", from);
        options.put("nsfw", nsfw);
        if (!Helper.isNullOrEmpty(relatedTo)) {
            options.put("related_to", relatedTo);
        }
        return options;
    }

    public static List<Claim> search(String rawQuery, int size, int from, boolean nsfw, String relatedTo) throws LbryRequestException, LbryResponseException {
        Uri.Builder uriBuilder = Uri.parse(String.format("%s/search", CONNECTION_STRING)).buildUpon().
                appendQueryParameter("s", rawQuery).
                appendQueryParameter("resolve", "true").
                appendQueryParameter("nsfw", String.valueOf(nsfw).toLowerCase()).
                appendQueryParameter("size", String.valueOf(size)).
                appendQueryParameter("from", String.valueOf(from));
        if (!Helper.isNullOrEmpty(relatedTo)) {
            uriBuilder.appendQueryParameter("related_to", relatedTo);
        }

        Map<String, Object> cacheKey = buildSearchOptionsKey(rawQuery, size, from, nsfw, relatedTo);
        if (searchCache.containsKey(cacheKey)) {
            return searchCache.get(cacheKey);
        }

        List<Claim> results = new ArrayList<>();
        Request request = new Request.Builder().url(uriBuilder.toString()).build();
        OkHttpClient client = new OkHttpClient();
        try {
            Response response = client.newCall(request).execute();
            if (response.code() == 200) {
                JSONArray array = new JSONArray(response.body().string());
                for (int i = 0; i < array.length(); i++) {
                    Claim claim = Claim.fromSearchJSONObject(array.getJSONObject(i));
                    results.add(claim);
                }
                searchCache.put(cacheKey, results);
            } else {
                throw new LbryResponseException(response.message());
            }
        } catch (IOException ex) {
            throw new LbryRequestException(String.format("search request for '%s' failed", rawQuery), ex);
        } catch (JSONException ex) {
            throw new LbryResponseException(String.format("the search response for '%s' could not be parsed", rawQuery), ex);
        }

        return results;
    }

    public static List<UrlSuggestion> autocomplete(String text) throws LbryRequestException, LbryResponseException {
        if (autocompleteCache.containsKey(text)) {
            return autocompleteCache.get(text);
        }

        List<UrlSuggestion> suggestions = new ArrayList<>();
        Uri.Builder uriBuilder = Uri.parse(String.format("%s/autocomplete", CONNECTION_STRING)).buildUpon().
                appendQueryParameter("s", text);
        Request request = new Request.Builder().url(uriBuilder.toString()).build();
        OkHttpClient client = new OkHttpClient();
        try {
            Response response = client.newCall(request).execute();
            if (response.code() == 200) {
                JSONArray array = new JSONArray(response.body().string());
                for (int i = 0; i < array.length(); i++) {
                    String item = array.getString(i);
                    boolean isChannel = item.startsWith("@");
                    LbryUri uri = new LbryUri();
                    if (isChannel) {
                        uri.setChannelName(item);
                    } else {
                        uri.setStreamName(item);
                    }
                    UrlSuggestion suggestion = new UrlSuggestion(isChannel ? UrlSuggestion.TYPE_CHANNEL : UrlSuggestion.TYPE_FILE, item);
                    suggestion.setUri(uri);
                    suggestions.add(suggestion);
                }

                autocompleteCache.put(text, suggestions);
            } else {
                throw new LbryResponseException(response.message());
            }
        } catch (IOException ex) {
            throw new LbryRequestException(String.format("autocomplete request for '%s' failed", text), ex);
        } catch (JSONException ex) {
            throw new LbryResponseException(String.format("the autocomplete response for '%s' could not be parsed", text), ex);
        }

        return suggestions;
    }
}
