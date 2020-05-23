package io.lbry.browser.tasks.file;

import android.os.AsyncTask;
import android.view.View;

import java.util.List;

import io.lbry.browser.exceptions.ApiCallException;
import io.lbry.browser.model.LbryFile;
import io.lbry.browser.utils.Helper;
import io.lbry.browser.utils.Lbry;

public class FileListTask extends AsyncTask<Void, Void, List<LbryFile>> {
    private String claimId;
    private boolean downloads;
    private int page;
    private int pageSize;
    private FileListResultHandler handler;
    private View progressView;
    private ApiCallException error;

    public FileListTask(int page, int pageSize, boolean downloads, View progressView, FileListResultHandler handler) {
        this(null, progressView, handler);
        this.page = page;
        this.pageSize = pageSize;
        this.downloads = downloads;
    }

    public FileListTask(String claimId, View progressView, FileListResultHandler handler) {
        this.claimId = claimId;
        this.progressView = progressView;
        this.handler = handler;
    }
    protected void onPreExecute() {
        Helper.setViewVisibility(progressView, View.VISIBLE);
    }
    protected List<LbryFile> doInBackground(Void... params) {
        try {
            return Lbry.fileList(claimId, downloads, page, pageSize);
        } catch (ApiCallException ex) {
            error = ex;
            return null;
        }
    }
    protected void onPostExecute(List<LbryFile> files) {
        Helper.setViewVisibility(progressView, View.GONE);
        if (handler != null) {
            if (files != null) {
                handler.onSuccess(files, files.size() < pageSize);
            } else {
                handler.onError(error);
            }
        }
    }

    public interface FileListResultHandler {
        void onSuccess(List<LbryFile> files, boolean hasReachedEnd);
        void onError(Exception error);
    }
}
