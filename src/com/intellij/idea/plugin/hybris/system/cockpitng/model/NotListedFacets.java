// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/config/refineBy

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

/**
 * http://www.hybris.com/cockpitng/config/refineBy:notListedFacets enumeration.
 */
public enum NotListedFacets implements com.intellij.util.xml.NamedEnum {
    SKIP("skip"),
    VISIBLE("visible");

    private final String value;

    NotListedFacets(final String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }

}
