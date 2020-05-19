package io.lbry.browser.model;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import io.lbry.browser.utils.Helper;
import io.lbry.browser.utils.LbryUri;
import io.lbry.browser.utils.Predefined;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Claim {
    public static final String CLAIM_TYPE_CLAIM = "claim";
    public static final String CLAIM_TYPE_UPDATE = "update";
    public static final String CLAIM_TYPE_SUPPORT = "support";

    public static final String TYPE_STREAM = "stream";
    public static final String TYPE_CHANNEL = "channel";
    public static final String TYPE_REPOST = "repost";

    public static final String STREAM_TYPE_AUDIO = "audio";
    public static final String STREAM_TYPE_IMAGE = "image";
    public static final String STREAM_TYPE_VIDEO = "video";
    public static final String STREAM_TYPE_SOFTWARE = "software";

    public static final String ORDER_BY_EFFECTIVE_AMOUNT = "effective_amount";
    public static final String ORDER_BY_RELEASE_TIME = "release_time";
    public static final String ORDER_BY_TRENDING_GROUP = "trending_group";
    public static final String ORDER_BY_TRENDING_MIXED = "trending_mixed";

    public static final List<String> CLAIM_TYPES = Arrays.asList(TYPE_CHANNEL, TYPE_STREAM);
    public static final List<String> STREAM_TYPES = Arrays.asList(
            STREAM_TYPE_AUDIO, STREAM_TYPE_IMAGE, STREAM_TYPE_SOFTWARE, STREAM_TYPE_VIDEO
    );

    public static final String RELEASE_TIME_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    @EqualsAndHashCode.Include
    private boolean placeholder;
    private boolean placeholderAnonymous;
    private boolean featured;
    private boolean unresolved; // used for featured
    private String address;
    private String amount;
    private String canonicalUrl;
    @EqualsAndHashCode.Include
    private String claimId;
    private int claimSequence;
    private String claimOp;
    private long confirmations;
    private boolean decodedClaim;
    private long timestamp;
    private long height;
    private boolean isMine;
    private String name;
    private String normalizedName;
    private int nout;
    private String permanentUrl;
    private String shortUrl;
    private String txid;
    private String type; // claim | update | support
    private String valueType; // stream | channel | repost
    private Claim repostedClaim;
    private Claim signingChannel;
    private String repostChannelUrl;
    private boolean isChannelSignatureValid;
    private GenericMetadata value;
    private LbryFile file; // associated file if it exists

    // device it was viewed on (for view history)
    private String device;

    public static Claim claimFromOutput(JSONObject item) {
        // we only need name, permanent_url, txid and nout
        Claim claim = new Claim();
        claim.setClaimId(Helper.getJSONString("claim_id", null, item));
        claim.setName(Helper.getJSONString("name", null, item));
        claim.setPermanentUrl(Helper.getJSONString("permanent_url", null, item));
        claim.setTxid(Helper.getJSONString("txid", null, item));
        claim.setNout(Helper.getJSONInt("nout", -1, item));
        return claim;
    }

    public String getOutpoint() {
        return String.format("%s:%d", txid, nout);
    }

    public boolean isFree() {
        if (!(value instanceof StreamMetadata)) {
            return true;
        }

        Fee fee = ((StreamMetadata) value).getFee();
        return fee == null || Helper.parseDouble(fee.getAmount(), 0) == 0;
    }

    public BigDecimal getActualCost(double usdRate) {
        if (!(value instanceof StreamMetadata)) {
            return new BigDecimal(0);
        }

        Fee fee = ((StreamMetadata) value).getFee();
        if (fee != null) {
            double amount = Helper.parseDouble(fee.getAmount(), 0);
            if ("usd".equalsIgnoreCase(fee.getCurrency())) {
                return new BigDecimal(String.valueOf(amount / usdRate));
            }

            return new BigDecimal(String.valueOf(amount)); // deweys
        }

        return new BigDecimal(0);
    }

    public String getMediaType() {
        if (value instanceof StreamMetadata) {
            StreamMetadata metadata = (StreamMetadata) value;
            String mediaType = metadata.getSource() != null ? metadata.getSource().getMediaType() : null;
            return mediaType;
        }
        return null;
    }

    public boolean isPlayable() {
        if (value instanceof StreamMetadata) {
            StreamMetadata metadata = (StreamMetadata) value;
            String mediaType = metadata.getSource() != null ? metadata.getSource().getMediaType() : null;
            if (mediaType != null) {
                return mediaType.startsWith("video") || mediaType.startsWith("audio");
            }
        }
        return false;
    }
    public boolean isViewable() {
        if (value instanceof StreamMetadata) {
            StreamMetadata metadata = (StreamMetadata) value;
            String mediaType = metadata.getSource() != null ? metadata.getSource().getMediaType() : null;
            if (mediaType != null) {
                return mediaType.startsWith("image") || mediaType.startsWith("text");
            }
        }
        return false;
    }
    public boolean isMature() {
        List<String> tags = getTags();
        if (tags != null && tags.size() > 0) {
            for (String tag : tags) {
                if (Predefined.MATURE_TAGS.contains(tag.toLowerCase())) {
                    return true;
                }
            }
        }
        return false;
    }

    public String getThumbnailUrl() {
        if (value != null && value.getThumbnail() != null) {
            return value.getThumbnail().getUrl();
        }
        return null;
    }

    public String getCoverUrl() {
        if (TYPE_CHANNEL.equals(valueType) && value != null && value instanceof ChannelMetadata && ((ChannelMetadata) value).getCover() != null) {
            return ((ChannelMetadata) value).getCover().getUrl();
        }
        return null;
    }

    public String getFirstCharacter() {
        if (name != null) {
            return name.startsWith("@") ? name.substring(1) : name;
        }
        return "";
    }

    public String getFirstTag() {
        if (value != null && value.tags != null && value.tags.size() > 0) {
            return value.tags.get(0);
        }
        return null;
    }

    public String getDescription() {
        return (value != null) ? value.getDescription() : null;
    }

    public String getWebsiteUrl() {
        return (value instanceof ChannelMetadata) ? ((ChannelMetadata) value).getWebsiteUrl() : null;
    }

    public String getEmail() {
        return (value instanceof ChannelMetadata) ? ((ChannelMetadata) value).getEmail() : null;
    }

    public String getPublisherName() {
        if (signingChannel != null) {
            return signingChannel.getName();
        }
        return "Anonymous";
    }

    public String getPublisherTitle() {
        if (signingChannel != null) {
            return Helper.isNullOrEmpty(signingChannel.getTitle()) ? signingChannel.getName() : signingChannel.getTitle();
        }
        return "Anonymous";
    }


    public List<String> getTags() {
        return (value != null && value.getTags() != null) ? new ArrayList<>(value.getTags()) : new ArrayList<>();
    }

    public List<Tag> getTagObjects() {
        List<Tag> tags = new ArrayList<>();
        if (value != null && value.getTags() != null) {
            for (String value : value.getTags()) {
                tags.add(new Tag(value));
            }
        }
        return tags;
    }

    public String getTitle() {
        return (value != null) ? value.getTitle() : null;
    }
    public String getTitleOrName() {
        return (value != null) ? value.getTitle() : getName();
    }

    public long getDuration() {
        if (value instanceof StreamMetadata) {
            StreamMetadata metadata = (StreamMetadata) value;
            if (STREAM_TYPE_VIDEO.equalsIgnoreCase(metadata.getStreamType()) && metadata.getVideo() != null) {
                return metadata.getVideo().getDuration();
            } else if (STREAM_TYPE_AUDIO.equalsIgnoreCase(metadata.getStreamType()) && metadata.getAudio() != null) {
                return metadata.getAudio().getDuration();
            }
        }

        return 0;
    }

    public static Claim fromViewHistory(ViewHistory viewHistory) {
        // only for stream claims
        Claim claim = new Claim();
        claim.setClaimId(viewHistory.getClaimId());
        claim.setName(viewHistory.getClaimName());
        claim.setValueType(TYPE_STREAM);
        claim.setPermanentUrl(viewHistory.getUri().toString());
        claim.setDevice(viewHistory.getDevice());
        claim.setConfirmations(1);

        StreamMetadata value = new StreamMetadata();
        value.setTitle(viewHistory.getTitle());
        value.setReleaseTime(viewHistory.getReleaseTime());
        if (!Helper.isNullOrEmpty(viewHistory.getThumbnailUrl())) {
            Resource thumbnail = new Resource();
            thumbnail.setUrl(viewHistory.getThumbnailUrl());
            value.setThumbnail(thumbnail);
        }
        if (viewHistory.getCost() != null && viewHistory.getCost().doubleValue() > 0) {
            Fee fee = new Fee();
            fee.setAmount(String.valueOf(viewHistory.getCost().doubleValue()));
            fee.setCurrency(viewHistory.getCurrency());
            value.setFee(fee);
        }

        claim.setValue(value);

        if (!Helper.isNullOrEmpty(viewHistory.getPublisherClaimId())) {
            Claim signingChannel = new Claim();
            signingChannel.setClaimId(viewHistory.getPublisherClaimId());
            signingChannel.setName(viewHistory.getPublisherName());
            if (!Helper.isNullOrEmpty(viewHistory.getPublisherTitle())) {
                GenericMetadata channelValue = new GenericMetadata();
                channelValue.setTitle(viewHistory.getPublisherTitle());
                signingChannel.setValue(channelValue);
            }
            claim.setSigningChannel(signingChannel);
        }

        return claim;
    }

    public static Claim fromJSONObject(JSONObject claimObject) {
        Claim claim = null;
        String claimJson = claimObject.toString();
        Type type = new TypeToken<Claim>(){}.getType();
        Type streamMetadataType = new TypeToken<StreamMetadata>(){}.getType();
        Type channelMetadataType = new TypeToken<ChannelMetadata>(){}.getType();

        Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
        claim = gson.fromJson(claimJson, type);

        try {
            String valueType = claim.getValueType();
            // Specific value type parsing
            if (TYPE_REPOST.equalsIgnoreCase(valueType)) {
                JSONObject repostedClaimObject = claimObject.getJSONObject("reposted_claim");
                claim.setRepostedClaim(Claim.fromJSONObject(repostedClaimObject));
            } else {
                JSONObject value = claimObject.getJSONObject("value");
                if (value != null) {
                    String valueJson = value.toString();
                    if (TYPE_STREAM.equalsIgnoreCase(valueType)) {
                        claim.setValue(gson.fromJson(valueJson, streamMetadataType));
                    } else if (TYPE_CHANNEL.equalsIgnoreCase(valueType)) {
                        claim.setValue(gson.fromJson(valueJson, channelMetadataType));
                    }
                }
            }
        } catch (JSONException ex) {
            // pass
        }

        return claim;
    }

    public static Claim fromSearchJSONObject(JSONObject searchResultObject) {
        Claim claim = new Claim();
        LbryUri claimUri = new LbryUri();
        try {
            claim.setClaimId(searchResultObject.getString("claimId"));
            claim.setName(searchResultObject.getString("name"));
            claim.setConfirmations(1);

            if (claim.getName().startsWith("@")) {
                claimUri.setChannelClaimId(claim.getClaimId());
                claimUri.setChannelName(claim.getName());
                claim.setValueType(TYPE_CHANNEL);
            } else {
                claimUri.setStreamClaimId(claim.getClaimId());
                claimUri.setStreamName(claim.getName());
                claim.setValueType(TYPE_STREAM);
            }

            int duration = searchResultObject.isNull("duration") ? 0 : searchResultObject.getInt("duration");
            long feeAmount = searchResultObject.isNull("fee") ? 0 : searchResultObject.getLong("fee");
            String releaseTimeString = !searchResultObject.isNull("release_time") ? searchResultObject.getString("release_time") : null;
            long releaseTime = 0;
            try {
                releaseTime = Double.valueOf(new SimpleDateFormat(RELEASE_TIME_DATE_FORMAT).parse(releaseTimeString).getTime() / 1000.0).longValue();
            } catch (ParseException ex) {
                // pass
            }

            GenericMetadata metadata = (duration > 0 || releaseTime > 0 || feeAmount > 0) ? new StreamMetadata() : new GenericMetadata();
            metadata.setTitle(searchResultObject.getString("title"));
            if (metadata instanceof StreamMetadata) {
                StreamInfo streamInfo = new StreamInfo();
                if (duration > 0) {
                    // assume stream type video
                    ((StreamMetadata) metadata).setStreamType(STREAM_TYPE_VIDEO);
                    streamInfo.setDuration(duration);
                }

                Fee fee = null;
                if (feeAmount > 0) {
                    fee = new Fee();
                    fee.setAmount(String.valueOf(new BigDecimal(String.valueOf(feeAmount)).divide(new BigDecimal(100000000))));
                    fee.setCurrency("LBC");
                }

                ((StreamMetadata) metadata).setFee(fee);
                ((StreamMetadata) metadata).setVideo(streamInfo);
                ((StreamMetadata) metadata).setReleaseTime(releaseTime);
            }
            claim.setValue(metadata);

            if (!searchResultObject.isNull("thumbnail_url")) {
                Resource thumbnail = new Resource();
                thumbnail.setUrl(searchResultObject.getString("thumbnail_url"));
                claim.getValue().setThumbnail(thumbnail);
            }

            if (!searchResultObject.isNull("channel_claim_id") && !searchResultObject.isNull("channel")) {
                Claim signingChannel = new Claim();
                signingChannel.setClaimId(searchResultObject.getString("channel_claim_id"));
                signingChannel.setName(searchResultObject.getString("channel"));
                LbryUri channelUri = new LbryUri();
                channelUri.setChannelClaimId(signingChannel.getClaimId());
                channelUri.setChannelName(signingChannel.getName());
                signingChannel.setPermanentUrl(channelUri.toString());

                claim.setSigningChannel(signingChannel);
            }
        } catch (JSONException ex) {
            // pass
        }

        claim.setPermanentUrl(claimUri.toString());

        return claim;
    }

    @Data
    public static class Meta {
        private long activationHeight;
        private int claimsInChannel;
        private int creationHeight;
        private int creationTimestamp;
        private String effectiveAmount;
        private long expirationHeight;
        private boolean isControlling;
        private String supportAmount;
        private int reposted;
        private double trendingGlobal;
        private double trendingGroup;
        private double trendingLocal;
        private double trendingMixed;
    }

    @Data
    public static class GenericMetadata {
        private String title;
        private String description;
        private Resource thumbnail;
        private List<String> languages;
        private List<String> tags;
        private List<Location> locations;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class ChannelMetadata extends GenericMetadata {
        private String publicKey;
        private String publicKeyId;
        private Resource cover;
        private String email;
        private String websiteUrl;
        private List<String> featured;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class StreamMetadata extends GenericMetadata {
        private String license;
        private String licenseUrl;
        private long releaseTime;
        private String author;
        private Fee fee;
        private String streamType; // video | audio | image | software
        private Source source;
        private StreamInfo video;
        private StreamInfo audio;
        private StreamInfo image;
        private StreamInfo software;

        @Data
        public static class Source {
            private String sdHash;
            private String mediaType;
            private String hash;
            private String name;
            private long size;
        }
    }

    // only support "url" for now
    @Data
    public static class Resource {
        private String url;
    }

    @Data
    public static class StreamInfo {
        private long duration; // video / audio
        private long height; // video / image
        private long width; // video / image
        private String os; // software
    }
}
