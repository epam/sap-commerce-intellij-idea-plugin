// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    null

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

/**
 * null:SettingType enumeration.
 */
public enum Setting implements com.intellij.util.xml.NamedEnum {
    BOOLEAN("Boolean"),
    DOUBLE("Double"),
    INTEGER("Integer"),
    STRING("String"),
    defaultValue("defaultValue"),
    key("key"),
    type("type");

    private final String value;

    Setting(final String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }

}
