package io.lbry.browser.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.lbry.browser.exceptions.LbryUriException;
import lombok.Data;

@Data
public class LbryUri {
    public static final String LBRY_TV_BASE_URL = "https://lbry.tv";
    public static final String PROTO_DEFAULT = "lbry://";
    public static final String REGEX_INVALID_URI = "[ =&#:$@%?;/\\\\\"<>%\\{\\}|^~\\[\\]`\u0000-\u0008\u000b-\u000c\u000e-\u001F\uD800-\uDFFF\uFFFE-\uFFFF]";
    public static final String REGEX_ADDRESS = "^(b|r)(?=[^0OIl]{32,33})[0-9A-Za-z]{32,33}$";
    public static final int CHANNEL_NAME_MIN_LENGTH = 1;
    public static final int CLAIM_ID_MAX_LENGTH = 40;

    private static final String REGEX_PART_PROTOCOL = "^((?:lbry://)?)";
    private static final String REGEX_PART_STREAM_OR_CHANNEL_NAME = "([^:$#/]*)";
    private static final String REGEX_PART_MODIFIER_SEPARATOR = "([:$#]?)([^/]*)";
    private static final String QUERY_STRING_BREAKER = "^([\\S]+)([?][\\S]*)";
    private static final Pattern PATTERN_SEPARATE_QUERY_STRING = Pattern.compile(QUERY_STRING_BREAKER);

    private String path;
    private boolean isChannel;
    private String streamName;
    private String streamClaimId;
    private String channelName;
    private String channelClaimId;
    private int primaryClaimSequence;
    private int secondaryClaimSequence;
    private int primaryBidPosition;
    private int secondaryBidPosition;

    private String claimName;
    private String claimId;
    private String contentName;
    private String queryString;

    private boolean isChannelUrl() {
        return (!Helper.isNullOrEmpty(channelName) && Helper.isNullOrEmpty(streamName)) || (!Helper.isNullOrEmpty(claimName) && claimName.startsWith("@"));
    }

    public static boolean isNameValid(String name) {
        return !Pattern.compile(REGEX_INVALID_URI).matcher(name).find();
    }

    public static LbryUri tryParse(String url) {
        try {
            return parse(url, false);
        } catch (LbryUriException ex) {
            return null;
        }
    }
    public static LbryUri parse(String url) throws LbryUriException {
        return parse(url, false);
    }
    public static LbryUri parse(String url, boolean requireProto) throws LbryUriException {
        Pattern componentsPattern = Pattern.compile(String.format("%s%s%s(/?)%s%s",
                REGEX_PART_PROTOCOL,
                REGEX_PART_STREAM_OR_CHANNEL_NAME,
                REGEX_PART_MODIFIER_SEPARATOR,
                REGEX_PART_STREAM_OR_CHANNEL_NAME,
                REGEX_PART_MODIFIER_SEPARATOR));

        String cleanUrl = url, queryString = null;
        Matcher qsMatcher = PATTERN_SEPARATE_QUERY_STRING.matcher(url);
        if (qsMatcher.matches()) {
            queryString = qsMatcher.group(2);
            cleanUrl = !Helper.isNullOrEmpty(queryString) ? url.substring(0, url.indexOf(queryString)) : url;
            if (queryString != null && queryString.length() > 0) {
                queryString = queryString.substring(1);
            }
        }

        List<String> components = new ArrayList<>();
        Matcher matcher = componentsPattern.matcher(cleanUrl);
        if (matcher.matches()) {
            // Note: For Java regex, group index 0 is always the full match
            for (int i = 1; i <= matcher.groupCount(); i++) {
                components.add(matcher.group(i));
            }
        }

        if (components.size() == 0) {
            throw new LbryUriException("Regular expression error occurred while trying to parse the value");
        }

        // components[0] = proto
        // components[1] = streamNameOrChannelName
        // components[2] = primaryModSeparator
        // components[3] = primaryModValue
        // components[4] = pathSep
        // components[5] = possibleStreamName
        // components[6] = secondaryModSeparator
        // components[7] = secondaryModValue
        if (requireProto && Helper.isNullOrEmpty(components.get(0))) {
            throw new LbryUriException("LBRY URLs must include a protocol prefix (lbry://).");
        }

        if (Helper.isNullOrEmpty(components.get(1))) {
            throw new LbryUriException("URL does not include name.");
        }

        for (String component : components.subList(1, components.size())) {
            if (component.indexOf(' ') > -1) {
                throw new LbryUriException("URL cannot include a space.");
            }
        }

        String streamOrChannelName = components.get(1);
        String primaryModSeparator = components.get(2);
        String primaryModValue = components.get(3);
        String possibleStreamName = components.get(5);
        String secondaryModSeparator = components.get(6);
        String secondaryModValue = components.get(7);

        boolean includesChannel = streamOrChannelName.startsWith("@");
        boolean isChannel = includesChannel && Helper.isNullOrEmpty(possibleStreamName);
        String channelName = includesChannel && streamOrChannelName.length() > 1 ? streamOrChannelName.substring(1) : null;
        if (includesChannel) {
            if (Helper.isNullOrEmpty(channelName)) {
                throw new LbryUriException("No channel name after @.");
            }
            if (channelName.length() < CHANNEL_NAME_MIN_LENGTH) {
                throw new LbryUriException(String.format("Channel names must be at least %d character long.", CHANNEL_NAME_MIN_LENGTH));
            }
        }

        UriModifier primaryMod = null, secondaryMod = null;
        if (!Helper.isNullOrEmpty(primaryModSeparator) && !Helper.isNullOrEmpty(primaryModValue)) {
            primaryMod = UriModifier.parse(primaryModSeparator, primaryModValue);
        }
        if (!Helper.isNullOrEmpty(secondaryModSeparator) && !Helper.isNullOrEmpty(secondaryModValue)) {
            secondaryMod = UriModifier.parse(secondaryModSeparator, secondaryModValue);
        }
        String streamName = includesChannel ? possibleStreamName : streamOrChannelName;
        String streamClaimId = (includesChannel && secondaryMod != null) ?
                secondaryMod.getClaimId() : primaryMod != null ? primaryMod.getClaimId() : null;
        String channelClaimId = (includesChannel && primaryMod != null) ? primaryMod.getClaimId() : null;

        LbryUri uri = new LbryUri();
        uri.setChannel(isChannel);
        uri.setPath(Helper.join(components.subList(1, components.size()), ""));
        uri.setStreamName(streamName);
        uri.setStreamClaimId(streamClaimId);
        uri.setChannelName(channelName);
        uri.setChannelClaimId(channelClaimId);
        uri.setPrimaryClaimSequence(primaryMod != null ? primaryMod.getClaimSequence() : -1);
        uri.setSecondaryClaimSequence(secondaryMod != null ? secondaryMod.getClaimSequence() : -1);
        uri.setPrimaryBidPosition(primaryMod != null ? primaryMod.getBidPosition() : -1);
        uri.setSecondaryBidPosition(secondaryMod != null ? secondaryMod.getBidPosition() : -1);

        // Values that will not work properly with canonical urls
        uri.setClaimName(streamOrChannelName);
        uri.setClaimId(primaryMod != null ? primaryMod.getClaimId() : null);
        uri.setContentName(streamName);
        uri.setQueryString(queryString);
        return uri;
    }

    public String build(boolean includeProto, String protoDefault, boolean vanity) {
        String formattedChannelName = null;
        if (channelName != null) {
            formattedChannelName = channelName.startsWith("@") ? channelName : String.format("@%s", channelName);
        }
        String primaryClaimName = claimName;
        if (Helper.isNullOrEmpty(primaryClaimName)) {
            primaryClaimName = contentName;
        }
        if (Helper.isNullOrEmpty(primaryClaimName)) {
            primaryClaimName = formattedChannelName;
        }
        if (Helper.isNullOrEmpty(primaryClaimName)) {
            primaryClaimName = streamName;
        }

        String primaryClaimId = claimId;
        if (Helper.isNullOrEmpty(primaryClaimId)) {
            primaryClaimId = !Helper.isNullOrEmpty(formattedChannelName) ? channelClaimId : streamClaimId;
        }

        StringBuilder sb = new StringBuilder();
        if (includeProto) {
            sb.append(protoDefault);
        }
        sb.append(primaryClaimName);
        if (vanity) {
            return sb.toString();
        }

        String secondaryClaimName = null;
        if (Helper.isNullOrEmpty(claimName) && !Helper.isNullOrEmpty(contentName)) {
            secondaryClaimName = contentName;
        }
        if (Helper.isNullOrEmpty(secondaryClaimName)) {
            secondaryClaimName = !Helper.isNullOrEmpty(formattedChannelName) ? streamName : null;
        }
        String secondaryClaimId = !Helper.isNullOrEmpty(secondaryClaimName) ? streamClaimId : null;

        if (!Helper.isNullOrEmpty(primaryClaimId)) {
            sb.append('#').append(primaryClaimId);
        }
        if (primaryClaimSequence > 0) {
            sb.append(':').append(primaryClaimSequence);
        }
        if (primaryBidPosition > 0) {
            sb.append('$').append(primaryBidPosition);
        }
        if (!Helper.isNullOrEmpty(secondaryClaimName)) {
            sb.append('/').append(secondaryClaimName);
        }
        if (!Helper.isNullOrEmpty(secondaryClaimId)) {
            sb.append('#').append(secondaryClaimId);
        }
        if (secondaryClaimSequence > 0) {
            sb.append(':').append(secondaryClaimSequence);
        }
        if (secondaryBidPosition > 0) {
            sb.append('$').append(secondaryBidPosition);
        }

        return sb.toString();
    }

    public String toTvString() {
        String formattedChannelName = null;
        if (channelName != null) {
            formattedChannelName = channelName.startsWith("@") ? channelName : String.format("@%s", channelName);
        }
        String primaryClaimName = claimName;
        if (Helper.isNullOrEmpty(primaryClaimName)) {
            primaryClaimName = contentName;
        }
        if (Helper.isNullOrEmpty(primaryClaimName)) {
            primaryClaimName = formattedChannelName;
        }
        if (Helper.isNullOrEmpty(primaryClaimName)) {
            primaryClaimName = streamName;
        }

        String primaryClaimId = claimId;
        if (Helper.isNullOrEmpty(primaryClaimId)) {
            primaryClaimId = !Helper.isNullOrEmpty(formattedChannelName) ? channelClaimId : streamClaimId;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(LBRY_TV_BASE_URL).append('/');
        sb.append(primaryClaimName);

        String secondaryClaimName = null;
        if (Helper.isNullOrEmpty(claimName) && !Helper.isNullOrEmpty(contentName)) {
            secondaryClaimName = contentName;
        }
        if (Helper.isNullOrEmpty(secondaryClaimName)) {
            secondaryClaimName = !Helper.isNullOrEmpty(formattedChannelName) ? streamName : null;
        }
        String secondaryClaimId = !Helper.isNullOrEmpty(secondaryClaimName) ? streamClaimId : null;

        if (!Helper.isNullOrEmpty(primaryClaimId)) {
            sb.append(':').append(primaryClaimId);
        }
        if (!Helper.isNullOrEmpty(secondaryClaimName)) {
            sb.append('/').append(secondaryClaimName);
        }
        if (!Helper.isNullOrEmpty(secondaryClaimId)) {
            sb.append(':').append(secondaryClaimId);
        }
        return sb.toString();
    }

    public static String normalize(String url) throws LbryUriException {
        return parse(url).toString();
    }

    public String toVanityString() {
        return build(true, PROTO_DEFAULT, true);
    }
    public String toString() {
        return build(true, PROTO_DEFAULT, false);
    }
    public int hashCode() {
        return toString().hashCode();
    }
    public boolean equals(Object o) {
        if (o == null || !(o instanceof LbryUri)) {
            return false;
        }
        return toString().equalsIgnoreCase(o.toString());
    }

    @Data
    public static class UriModifier {
        private String claimId;
        private int claimSequence;
        private int bidPosition;

        public UriModifier(String claimId, int claimSequence, int bidPosition) {
            this.claimId = claimId;
            this.claimSequence = claimSequence;
            this.bidPosition = bidPosition;
        }

        public static UriModifier parse(String modSeparator, String modValue) throws LbryUriException {
            String claimId = null;
            int claimSequence = 0, bidPosition = 0;
            if (!Helper.isNullOrEmpty(modSeparator)) {
                if (Helper.isNullOrEmpty(modValue)) {
                    throw new LbryUriException(String.format("No modifier provided after separator %s", modSeparator));
                }

                if ("#".equals(modSeparator)) {
                    claimId = modValue;
                } else if (":".equals(modSeparator)) {
                    claimSequence = Helper.parseInt(modValue, -1);
                } else if ("$".equals(modSeparator)) {
                    bidPosition = Helper.parseInt(modValue, -1);
                }
            }

            if (!Helper.isNullOrEmpty(claimId) && (claimId.length() > CLAIM_ID_MAX_LENGTH || !claimId.matches("^[0-9a-f]+$"))) {
                throw new LbryUriException(String.format("Invalid claim ID %s", claimId));
            }
            if (claimSequence == -1) {
                throw new LbryUriException("Claim sequence must be a number");
            }
            if (bidPosition == -1) {
                throw new LbryUriException("Bid position must be a number");
            }

            return new UriModifier(claimId, claimSequence, bidPosition);
        }
    }
}
