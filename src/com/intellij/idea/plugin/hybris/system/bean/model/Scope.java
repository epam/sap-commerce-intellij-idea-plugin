// Generated on Fri Nov 17 20:45:54 CET 2017
// DTD/Schema  :    null

package com.intellij.idea.plugin.hybris.system.bean.model;

/**
 * null:scopeAttrType enumeration.
 */
public enum Scope implements com.intellij.util.xml.NamedEnum {
    ALL("all"),
    GETTER("getter"),
    MEMBER("member"),
    SETTER("setter");

    private final String value;

    private Scope(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
