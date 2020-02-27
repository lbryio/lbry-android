package io.lbry.browser.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import io.lbry.browser.DownloadManager;

public class NotificationDeletedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int notificationId = intent.getExtras().getInt(DownloadManager.NOTIFICATION_ID_KEY);
        if (DownloadManager.DOWNLOAD_NOTIFICATION_GROUP_ID == notificationId) {
            DownloadManager.groupCreated = false;
        }
    }
}
