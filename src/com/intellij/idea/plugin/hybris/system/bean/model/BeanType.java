// Generated on Fri Nov 17 20:45:54 CET 2017
// DTD/Schema  :    null

package com.intellij.idea.plugin.hybris.system.bean.model;

public enum BeanType implements com.intellij.util.xml.NamedEnum {
    BEAN("bean"),
    EVENT("event");

    private final String value;

    private BeanType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
