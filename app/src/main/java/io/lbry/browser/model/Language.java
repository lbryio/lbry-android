package io.lbry.browser.model;

import lombok.Data;

@Data
public class Language {
    private final String code;
    private final String name;
    private final int stringResourceId;

    public Language(String code, String name, int stringResourceId) {
        this.code = code;
        this.name = name;
        this.stringResourceId = stringResourceId;
    }
}
