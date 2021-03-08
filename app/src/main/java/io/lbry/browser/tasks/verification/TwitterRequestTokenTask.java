package io.lbry.browser.tasks.verification;

import android.os.AsyncTask;
import android.util.Base64;

import com.google.api.client.auth.oauth.OAuthHmacSigner;
import com.google.api.client.auth.oauth.OAuthParameters;
import com.google.api.client.http.GenericUrl;

import java.nio.charset.StandardCharsets;

import io.lbry.browser.model.TwitterOauth;
import io.lbry.browser.tasks.TwitterOauthHandler;
import io.lbry.browser.utils.Helper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class TwitterRequestTokenTask extends AsyncTask<Void, Void, String> {
    private static final String ENDPOINT = "https://api.twitter.com/oauth/request_token";

    private final String consumerKey;
    private final String consumerSecret;
    private Exception error;
    private final TwitterOauthHandler handler;

    public TwitterRequestTokenTask(String consumerKey, String consumerSecret, TwitterOauthHandler handler) {
        this.consumerKey = consumerKey;
        this.consumerSecret = consumerSecret;
        this.handler = handler;
    }

    public String doInBackground(Void... params) {
        try {

            OAuthHmacSigner signer = new OAuthHmacSigner();
            signer.clientSharedSecret = new String(
                    Base64.decode(consumerSecret, Base64.NO_WRAP), StandardCharsets.UTF_8.name());

            OAuthParameters oauthParams = new OAuthParameters();
            oauthParams.callback = "https://lbry.tv";
            oauthParams.consumerKey = new String(
                    Base64.decode(consumerKey, Base64.NO_WRAP), StandardCharsets.UTF_8.name());
            oauthParams.signatureMethod = "HMAC-SHA-1";
            oauthParams.signer = signer;
            oauthParams.computeNonce();
            oauthParams.computeTimestamp();
            oauthParams.computeSignature("POST", new GenericUrl(ENDPOINT));

            RequestBody body = RequestBody.create(new byte[0]);
            Request request = new Request.Builder().url(ENDPOINT).addHeader(
                    "Authorization", oauthParams.getAuthorizationHeader()).post(body).build();

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
