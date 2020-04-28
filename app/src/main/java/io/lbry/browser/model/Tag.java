package io.lbry.browser.model;

import io.lbry.browser.utils.Helper;
import lombok.Getter;
import lombok.Setter;

public class Tag {
    @Getter
    @Setter
    private String name;

    public Tag(String name) {
        this.name = name;
    }

    public String getLowercaseName() {
        return name.toLowerCase();
    }

    public boolean isMature() {
        return Helper.MATURE_TAG_NAMES.contains(name.toLowerCase());
    }

    public boolean equals(Object o) {
        return (o instanceof Tag) && ((Tag) o).getName().equalsIgnoreCase(name);
    }
    public int hashCode() {
        return name.hashCode();
    }
}
