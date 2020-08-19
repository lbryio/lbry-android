package io.lbry.browser.tasks.verification;

import android.os.AsyncTask;

import com.google.api.client.auth.oauth.OAuthHmacSigner;
import com.google.api.client.auth.oauth.OAuthParameters;
import com.google.api.client.http.GenericUrl;

import io.lbry.browser.VerificationActivity;
import io.lbry.browser.model.TwitterOauth;
import io.lbry.browser.tasks.TwitterOauthHandler;
import io.lbry.browser.utils.Helper;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class TwitterAccessTokenTask extends AsyncTask<Void, Void, String> {
    private static final String ENDPOINT = "https://api.twitter.com/oauth/access_token";

    private Exception error;
    private String oauthParams;
    private TwitterOauthHandler handler;

    public TwitterAccessTokenTask(String oauthParams, TwitterOauthHandler handler) {
        this.oauthParams = oauthParams;
        this.handler = handler;
    }

    public String doInBackground(Void... params) {
        try {
            String url = String.format("%s?%s", ENDPOINT, oauthParams);
            RequestBody body = RequestBody.create(new byte[0]);
            Request request = new Request.Builder().url(url).post(body).build();

            OkHttpClient client = new OkHttpClient.Builder().build();
            Response response = client.newCall(request).execute();
            return response.body().string();
        } catch (Exception ex) {
            error = ex;
            return null;
        }
    }

    protected void onPostExecute(String response) {
        if (!Helper.isNullOrEmpty(response)) {
            String[] pairs = response.split("&");
            TwitterOauth twitterOauth = new TwitterOauth();
            for (String pair : pairs) {
                String[] parts = pair.split("=");
                if (parts.length != 2) {
                    continue;
                }
                String key = parts[0];
                String value = parts[1];
                if ("oauth_token".equalsIgnoreCase(key)) {
                    twitterOauth.setOauthToken(value);
                } else if ("oauth_token_secret".equalsIgnoreCase(key)) {
                    twitterOauth.setOauthTokenSecret(value);
                }
            }
            handler.onSuccess(twitterOauth);
        } else {
            handler.onError(error);
        }
    }
}
