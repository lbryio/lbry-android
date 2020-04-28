package io.lbry.browser.listener;

public interface SignInListener {
    void onEmailAdded(String email);
    void onEmailEdit();
    void onEmailVerified();
}
