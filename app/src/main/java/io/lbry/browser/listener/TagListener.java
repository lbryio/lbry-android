package io.lbry.browser.listener;

import io.lbry.browser.model.Tag;

public interface TagListener {
    void onTagAdded(Tag tag);
    void onTagRemoved(Tag tag);
}
