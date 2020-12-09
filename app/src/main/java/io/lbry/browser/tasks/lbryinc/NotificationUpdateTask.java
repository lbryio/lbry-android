package io.lbry.browser.tasks.lbryinc;

import android.os.AsyncTask;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.lbry.browser.exceptions.LbryioRequestException;
import io.lbry.browser.exceptions.LbryioResponseException;
import io.lbry.browser.utils.Helper;
import io.lbry.browser.utils.Lbryio;

public class NotificationUpdateTask extends AsyncTask<Void, Void, Boolean> {
    private List<Long> ids;
    private boolean seen;
    private boolean read;
    private boolean updateRead;

    public NotificationUpdateTask(List<Long> ids, boolean seen) {
        this(ids, false, true, false);
    }

    public NotificationUpdateTask(List<Long> ids, boolean read, boolean seen, boolean updateRead) {
        this.ids = ids;
        this.read = read;
        this.seen = seen;
        this.updateRead = updateRead;
    }

    protected Boolean doInBackground(Void... params) {
        Map<String, String> options = new HashMap<>();
        options.put("notification_ids", Helper.joinL(ids, ","));
        options.put("is_seen", String.valueOf(seen));
        if (updateRead) {
            options.put("is_read", String.valueOf(read));
        }

        try {
            Object result = Lbryio.parseResponse(Lbryio.call("notification", "edit", options, null));
            return "ok".equalsIgnoreCase(result.toString());
        } catch (LbryioResponseException | LbryioRequestException ex) {

        }
        return false;
    }
}
