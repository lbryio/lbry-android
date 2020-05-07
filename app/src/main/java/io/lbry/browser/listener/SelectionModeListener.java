package io.lbry.browser.listener;

public interface SelectionModeListener {
    void onEnterSelectionMode();
    void onExitSelectionMode();
    void onItemSelectionToggled();
}
