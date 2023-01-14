// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    null

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

/**
 * null:SocketMultiplicity enumeration.
 */
public enum SocketMultiplicity implements com.intellij.util.xml.NamedEnum {
    COLLECTION("Collection"),
    LIST("List"),
    SET("Set");

    private final String value;

    SocketMultiplicity(final String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }

}
