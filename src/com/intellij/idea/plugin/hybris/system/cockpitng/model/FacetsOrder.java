// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/config/refineBy

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

/**
 * http://www.hybris.com/cockpitng/config/refineBy:facetsOrder enumeration.
 */
public enum FacetsOrder implements com.intellij.util.xml.NamedEnum {
    LISTED("listed"),
    RECEIVED("received");

    private final String value;

    FacetsOrder(final String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }

}
