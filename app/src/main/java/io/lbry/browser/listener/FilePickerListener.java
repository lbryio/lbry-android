package io.lbry.browser.listener;

public interface FilePickerListener {
    void onFilePicked(String filePath);
    void onFilePickerCancelled();
}
