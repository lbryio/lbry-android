package io.lbry.browser.model;

import lombok.Data;

@Data
public class Language {
    private String code;
    private String name;
    private int stringResourceId;

    public Language(String code, String name, int stringResourceId) {
        this.code = code;
        this.name = name;
        this.stringResourceId = stringResourceId;
    }
}
