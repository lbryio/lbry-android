package io.lbry.browser.listener;

public interface StoragePermissionListener {
    void onStoragePermissionGranted();
    void onStoragePermissionRefused();
}
