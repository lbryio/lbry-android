package io.lbry.browser.tasks.lbryinc;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.lbry.browser.MainActivity;
import io.lbry.browser.data.DatabaseHelper;
import io.lbry.browser.exceptions.LbryioRequestException;
import io.lbry.browser.exceptions.LbryioResponseException;
import io.lbry.browser.model.lbryinc.LbryNotification;
import io.lbry.browser.utils.Helper;
import io.lbry.browser.utils.Lbryio;

public class NotificationListTask extends AsyncTask<Void, Void, List<LbryNotification>> {
    private Context context;
    private ListNotificationsHandler handler;
    private ProgressBar progressBar;
    private Exception error;

    public NotificationListTask(Context context, ProgressBar progressBar, ListNotificationsHandler handler) {
        this.context = context;
        this.progressBar = progressBar;
        this.handler = handler;
    }
    protected void onPreExecute() {
        Helper.setViewVisibility(progressBar, View.VISIBLE);
    }
    protected List<LbryNotification> doInBackground(Void... params) {
        List<LbryNotification> notifications = new ArrayList<>();
        SQLiteDatabase db = null;
        try {
            JSONArray array = (JSONArray) Lbryio.parseResponse(Lbryio.call("notification", "list", context));
            if (array != null) {
                for (int i = 0; i < array.length(); i++) {
                    JSONObject item = array.getJSONObject(i);
                    if (item.has("notification_parameters")) {
                        LbryNotification notification = new LbryNotification();

                        JSONObject notificationParams = item.getJSONObject("notification_parameters");
                        if (notificationParams.has("device")) {
                            JSONObject device = notificationParams.getJSONObject("device");
                            notification.setTitle(Helper.getJSONString("title", null, device));
                            notification.setDescription(Helper.getJSONString("text", null, device));
                            notification.setTargetUrl(Helper.getJSONString("target", null, device));
                        }
                        if (notificationParams.has("dynamic") && !notificationParams.isNull("dynamic")) {
                            JSONObject dynamic = notificationParams.getJSONObject("dynamic");
                            if (dynamic.has("channelURI")) {
                                String channelUrl = Helper.getJSONString("channelURI", null, dynamic);
                                if (!Helper.isNullOrEmpty(channelUrl)) {
                                    notification.setTargetUrl(channelUrl);
                                }
                            }
                            if (dynamic.has("hash") && "comment".equalsIgnoreCase(Helper.getJSONString("notification_rule", null, item))) {
                                notification.setTargetUrl(String.format("%s?comment_hash=%s", notification.getTargetUrl(), dynamic.getString("hash")));
                            }
                        }

                        notification.setRule(Helper.getJSONString("notification_rule", null, item));
                        notification.setRemoteId(Helper.getJSONLong("id", 0, item));
                        notification.setRead(Helper.getJSONBoolean("is_read", false, item));
                        notification.setSeen(Helper.getJSONBoolean("is_seen", false, item));

                        try {
                            SimpleDateFormat dateFormat = new SimpleDateFormat(Helper.ISO_DATE_FORMAT_JSON, Locale.US);
                            notification.setTimestamp(dateFormat.parse(Helper.getJSONString("created_at", dateFormat.format(new Date()), item)));
                        } catch (ParseException ex) {
                            notification.setTimestamp(new Date());
                        }

                        if (notification.getRemoteId() > 0 && !Helper.isNullOrEmpty(notification.getDescription())) {
                            notifications.add(notification);
                        }
                    }
                }

                if (context instanceof MainActivity) {
                    db = ((MainActivity) context).getDbHelper().getWritableDatabase();
                    for (LbryNotification notification : notifications) {
                        DatabaseHelper.createOrUpdateNotification(notification, db);
                    }
                }
            }
        } catch (ClassCastException | LbryioRequestException | LbryioResponseException | JSONException | IllegalStateException ex) {
            error = ex;
            return null;
        }

        return notifications;
    }
    protected void onPostExecute(List<LbryNotification> notifications) {
        Helper.setViewVisibility(progressBar, View.GONE);
        if (handler != null) {
            if (notifications != null) {
                handler.onSuccess(notifications);
            } else {
                handler.onError(error);
            }
        }
    }

    public interface ListNotificationsHandler {
        void onSuccess(List<LbryNotification> notifications);
        void onError(Exception exception);
    }
}
