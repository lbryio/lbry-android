package io.lbry.browser.listener;

import java.util.List;

import io.lbry.browser.model.Claim;

public interface FetchChannelsListener {
    void onChannelsFetched(List<Claim> channels);
}
