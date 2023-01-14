// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpit/config

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

/**
 * http://www.hybris.com/cockpit/config:mergeAttrTypeKnown enumeration.
 */
public enum MergeAttrTypeKnown implements com.intellij.util.xml.NamedEnum {
    AUTHORITY("authority"),
    PRINCIPAL("principal"),
    TYPE("type");

    private final String value;

    MergeAttrTypeKnown(final String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }

}
