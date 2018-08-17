package io.lbry.browser.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import io.lbry.browser.reactmodules.DownloadManagerModule;

public class NotificationDeletedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int notificationId = intent.getExtras().getInt(DownloadManagerModule.NOTIFICATION_ID_KEY);
        if (DownloadManagerModule.GROUP_ID == notificationId) {
            DownloadManagerModule.groupCreated = false;
        }
    }
}
