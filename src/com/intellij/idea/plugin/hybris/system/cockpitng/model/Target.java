// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/config/links

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

/**
 * http://www.hybris.com/cockpitng/config/links:target enumeration.
 */
public enum Target implements com.intellij.util.xml.NamedEnum {
    _BLANK("_blank"),
    _SELF("_self");

    private final String value;

    Target(final String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }

}
