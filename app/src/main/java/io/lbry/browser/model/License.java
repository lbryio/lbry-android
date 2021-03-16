package io.lbry.browser.model;

import lombok.Data;

@Data
public class License {
    private final String name;
    private String url;
    private final int stringResourceId;

    public License(String name, int stringResourceId) {
        this.name = name;
        this.stringResourceId = stringResourceId;
    }
    public License(String name, String url, int stringResourceId) {
        this.name = name;
        this.url = url;
        this.stringResourceId  = stringResourceId;
    }
}
