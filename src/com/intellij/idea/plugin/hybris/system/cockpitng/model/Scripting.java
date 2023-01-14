// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/component/dynamicForms

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

/**
 * http://www.hybris.com/cockpitng/component/dynamicForms:scriptingType enumeration.
 * <pre>
 * <h3>Enumeration http://www.hybris.com/cockpitng/component/dynamicForms:scriptingType documentation</h3>
 * Script can be treated as "inline" script and evaluated immediately or it can be retrieved from remote
 * 				"uri" and
 * 				then evaluated.
 * 				Inline is set by default.
 * </pre>
 */
public enum Scripting implements com.intellij.util.xml.NamedEnum {
    INLINE("inline"),
    URI("uri");

    private final String value;

    Scripting(final String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }

}
