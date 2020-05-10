package io.lbry.browser.model.lbryinc;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.lang.reflect.Type;

import io.lbry.browser.model.Claim;
import io.lbry.browser.utils.Helper;
import lombok.Data;

@Data
public class Reward {
    public static final String TYPE_NEW_DEVELOPER = "new_developer";
    public static final String TYPE_NEW_USER = "new_user";
    public static final String TYPE_CONFIRM_EMAIL = "email_provided";
    public static final String TYPE_FIRST_CHANNEL = "new_channel";
    public static final String TYPE_FIRST_STREAM = "first_stream";
    public static final String TYPE_MANY_DOWNLOADS = "many_downloads";
    public static final String TYPE_FIRST_PUBLISH = "first_publish";
    public static final String TYPE_REFERRAL = "referrer";
    public static final String TYPE_REFEREE = "referee";
    public static final String TYPE_REWARD_CODE = "reward_code";
    public static final String TYPE_SUBSCRIPTION = "subscription";
    public static final String YOUTUBE_CREATOR = "youtube_creator";
    public static final String TYPE_DAILY_VIEW = "daily_view";
    public static final String TYPE_NEW_ANDROID = "new_android";

    private boolean custom;
    private long id;
    private String rewardType;
    private double rewardAmount;
    private String transactionId;
    private String createdAt;
    private String updatedAt;
    private String rewardTitle;
    private String rewardDescription;
    private String rewardNotification;
    private String rewardRange;

    public String getDisplayAmount() {
        if (shouldDisplayRange()) {
            return rewardRange.split("-")[1];
        }
        if (rewardAmount > 0) {
            return String.valueOf(rewardAmount);
        }
        return "?";
    }

    public boolean isClaimed() {
        return !Helper.isNullOrEmpty(transactionId);
    }

    public boolean shouldDisplayRange() {
        return (!isClaimed() && !Helper.isNullOrEmpty(rewardRange) && rewardRange.indexOf('-') > -1);
    }

    public static Reward fromJSONObject(JSONObject rewardObject) {
        String rewardJson = rewardObject.toString();
        Type type = new TypeToken<Reward>(){}.getType();
        Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
        return gson.fromJson(rewardJson, type);
    }
}
