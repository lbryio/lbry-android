package io.lbry.browser.tasks;

public interface GenericTaskHandler {
    void beforeStart();
    void onSuccess();
    void onError(Exception error);
}
