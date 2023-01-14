// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/component/dynamicForms

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

/**
 * http://www.hybris.com/cockpitng/component/dynamicForms:scriptingLanguage enumeration.
 * <pre>
 * <h3>Enumeration http://www.hybris.com/cockpitng/component/dynamicForms:scriptingLanguage documentation</h3>
 * You can select between Groovy, Spring Expression Language (SpEL), BeanShell and JavaScript. SpEL is the
 * 				default.
 * </pre>
 */
public enum ScriptingLanguage implements com.intellij.util.xml.NamedEnum {
    BEAN_SHELL("BeanShell"),
    GROOVY("Groovy"),
    JAVA_SCRIPT("JavaScript"),
    SP_EL("SpEL");

    private final String value;

    ScriptingLanguage(final String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }

}
