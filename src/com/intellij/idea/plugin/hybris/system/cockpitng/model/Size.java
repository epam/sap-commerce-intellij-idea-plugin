// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/config/wizard-config

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

/**
 * http://www.hybris.com/cockpitng/config/wizard-config:Size enumeration.
 */
public enum Size implements com.intellij.util.xml.NamedEnum {
    LARGE("large"),
    MEDIUM("medium"),
    SMALL("small");

    private final String value;

    Size(final String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }

}
