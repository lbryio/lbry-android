package io.lbry.browser.model;

import java.util.Comparator;

import io.lbry.browser.utils.Predefined;
import lombok.Getter;
import lombok.Setter;

public class Tag implements Comparator<Tag> {
    @Getter
    @Setter
    private String name;
    @Getter
    @Setter
    private boolean followed;

    public Tag() {

    }
    public Tag(String name) {
        this.name = name;
    }

    public String getLowercaseName() {
        return name.toLowerCase();
    }

    public boolean isMature() {
        return Predefined.MATURE_TAGS.contains(name.toLowerCase());
    }

    public String toString() {
        return getLowercaseName();
    }
    public boolean equals(Object o) {
        return (o instanceof Tag) && ((Tag) o).getName().equalsIgnoreCase(name);
    }
    public int hashCode() {
        return name.toLowerCase().hashCode();
    }
    public int compare(Tag a, Tag b) {
        return a.getLowercaseName().compareToIgnoreCase(b.getLowercaseName());
    }
}
