// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/config/dashboard

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

/**
 * http://www.hybris.com/cockpitng/config/dashboard:unassignedBehavior enumeration.
 */
public enum UnassignedBehavior implements com.intellij.util.xml.NamedEnum {
    APPEND("append"),
    EXCLUDE("exclude");

    private final String value;

    UnassignedBehavior(final String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }

}
