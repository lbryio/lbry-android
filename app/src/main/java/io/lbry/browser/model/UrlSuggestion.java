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

    public UrlSuggestion(int type, String text) {
        this.type = type;
        this.text = text;
    }

    public String getTitle() {
        switch (type) {
            case TYPE_CHANNEL:
                return String.format("%s - %s", text.startsWith("@") ? text.substring(1) : text, uri.toString());
            case TYPE_FILE:
                return String.format("%s - %s", text, uri.toString());
            case TYPE_TAG:
                return String.format("%s - #%s", text, text);
        }

        return text;
    }
}
