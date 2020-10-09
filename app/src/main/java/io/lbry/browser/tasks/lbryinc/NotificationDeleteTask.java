package io.lbry.browser.tasks.lbryinc;

import android.os.AsyncTask;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.lbry.browser.exceptions.LbryioRequestException;
import io.lbry.browser.exceptions.LbryioResponseException;
import io.lbry.browser.utils.Helper;
import io.lbry.browser.utils.Lbryio;

public class NotificationDeleteTask extends AsyncTask<Void, Void, Boolean> {
    private List<Long> ids;

    public NotificationDeleteTask(List<Long> ids) {
        this.ids = ids;
    }

    protected Boolean doInBackground(Void... params) {
        Map<String, String> options = new HashMap<>();
        options.put("notification_ids", Helper.joinL(ids, ","));

        try {
            Object result = Lbryio.parseResponse(Lbryio.call("notification", "delete", options, null));
            return "ok".equalsIgnoreCase(result.toString());
        } catch (LbryioResponseException | LbryioRequestException ex) {
            // pass
        }
        return false;
    }
}
