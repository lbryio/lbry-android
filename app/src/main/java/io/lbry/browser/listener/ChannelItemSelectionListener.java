package io.lbry.browser.listener;

import io.lbry.browser.model.Claim;

public interface ChannelItemSelectionListener {
    void onChannelItemSelected(Claim claim);
    void onChannelItemDeselected(Claim claim);
    void onChannelSelectionCleared();
}
