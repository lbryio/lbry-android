package io.lbry.browser.tasks.lbryinc;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import io.lbry.browser.model.TwitterOauth;
import io.lbry.browser.model.lbryinc.RewardVerified;
import io.lbry.browser.tasks.RewardVerifiedHandler;
import io.lbry.browser.utils.Helper;
import io.lbry.browser.utils.Lbryio;

public class TwitterVerifyTask extends AsyncTask<Void, Void, RewardVerified> {
    private Context context;
    private View progressView;
    private TwitterOauth twitterOauth;
    private RewardVerifiedHandler handler;
    private Exception error;

    public TwitterVerifyTask(TwitterOauth twitterOauth, View progressView, Context context, RewardVerifiedHandler handler) {
        this.twitterOauth = twitterOauth;
        this.progressView = progressView;
        this.context = context;
        this.handler = handler;
    }

    protected void onPreExecute() {
        Helper.setViewVisibility(progressView, View.VISIBLE);
    }

    protected RewardVerified doInBackground(Void... params) {
        try {
            Map<String, String> options = new HashMap<>();
            options.put("oauth_token", twitterOauth.getOauthToken());
            options.put("oauth_token_secret", twitterOauth.getOauthTokenSecret());

            JSONObject object = (JSONObject) Lbryio.parseResponse(Lbryio.call("verification", "twitter_verify", options, context));
            Type type = new TypeToken<RewardVerified>(){}.getType();
            Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
            return gson.fromJson(object.toString(), type);
        } catch (Exception ex) {
            error = ex;
            return null;
        }
    }

    protected void onPostExecute(RewardVerified result) {
        Helper.setViewVisibility(progressView, View.GONE);
        if (handler != null) {
            if (result != null) {
                handler.onSuccess(result);
            } else {
                handler.onError(error);
            }
        }
    }
}
