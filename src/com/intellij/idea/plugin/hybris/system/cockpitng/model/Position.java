// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/config/wizard-config

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

/**
 * http://www.hybris.com/cockpitng/config/wizard-config:positionAttrType enumeration.
 */
public enum Position implements com.intellij.util.xml.NamedEnum {
    BOTTOM("bottom"),
    LEFT("left"),
    RIGHT("right"),
    TOP("top");

    private final String value;

    Position(final String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }

}
