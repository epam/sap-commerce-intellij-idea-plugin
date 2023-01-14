// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    null

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

/**
 * null:SocketVisibility enumeration.
 */
public enum SocketVisibility implements com.intellij.util.xml.NamedEnum {
    DEFAULT("default"),
    EXTERNAL("external"),
    INTERNAL("internal"),
    INVISIBLE("invisible");

    private final String value;

    SocketVisibility(final String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }

}
