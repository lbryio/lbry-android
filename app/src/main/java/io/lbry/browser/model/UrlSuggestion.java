package io.lbry.browser.model;

import io.lbry.browser.utils.LbryUri;
import lombok.Data;

@Data
public class UrlSuggestion {
    public static final int TYPE_CHANNEL = 1;
    public static final int TYPE_FILE = 2;
    public static final int TYPE_SEARCH = 3;
    public static final int TYPE_TAG = 4;

    private int type;
    private String text;
    private LbryUri uri;
    private Claim claim; // associated claim if resolved
    private boolean titleTextOnly;
    private boolean titleUrlOnly;
    private boolean useTextAsDescription;

    public UrlSuggestion() {

    }
    public UrlSuggestion(int type, String text) {
        this.type = type;
        this.text = text;
    }
    public UrlSuggestion(int type, String text, LbryUri uri) {
        this(type, text);
        this.uri = uri;
    }
    public UrlSuggestion(int type, String text, LbryUri uri, boolean titleTextOnly) {
        this(type, text, uri);
        this.titleTextOnly = titleTextOnly;
    }

    public String getTitle() {
        if (titleUrlOnly && (type == TYPE_CHANNEL || type == TYPE_FILE)) {
            return uri.toString();
        }

        if (!titleTextOnly) {
            switch (type) {
                case TYPE_CHANNEL:
                    return String.format("%s - %s", text.startsWith("@") ? text.substring(1) : text, uri.toVanityString());
                case TYPE_FILE:
                    return String.format("%s - %s", text, uri.toVanityString());
                case TYPE_TAG:
                    return String.format("%s - #%s", text, text);
            }
        }

        return text;
    }
}
