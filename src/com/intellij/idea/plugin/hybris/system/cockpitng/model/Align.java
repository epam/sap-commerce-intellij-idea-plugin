// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/config/wizard-config

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

/**
 * http://www.hybris.com/cockpitng/config/wizard-config:alignAttrType enumeration.
 */
public enum Align implements com.intellij.util.xml.NamedEnum {
    LEFT("left"),
    RIGHT("right");

    private final String value;

    Align(final String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }

}
