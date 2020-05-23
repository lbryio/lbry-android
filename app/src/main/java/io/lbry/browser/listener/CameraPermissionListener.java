package io.lbry.browser.listener;

public interface CameraPermissionListener {
    void onCameraPermissionGranted();
    void onCameraPermissionRefused();
    void onRecordAudioPermissionGranted();
    void onRecordAudioPermissionRefused();
}
