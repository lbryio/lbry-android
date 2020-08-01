package io.lbry.browser.tasks;

import io.lbry.browser.model.TwitterOauth;

public interface TwitterOauthHandler {
    void onSuccess(TwitterOauth twitterOauth);
    void onError(Exception error);
}
