// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/component/dynamicForms

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/component/dynamicForms:dynamicAttribute interface.
 */
public interface DynamicAttribute extends DomElement, AbstractDynamicElement {

    /**
     * Returns the value of the computedValue child.
     *
     * @return the value of the computedValue child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("computedValue")
    GenericAttributeValue<String> getComputedValue();


    /**
     * Returns the value of the lang child.
     * <pre>
     * <h3>Attribute null:lang documentation</h3>
     * Specify coma separated names of locales to update or "*" to update all available locales.
     * </pre>
     *
     * @return the value of the lang child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("lang")
    GenericAttributeValue<String> getLang();


    /**
     * Returns the value of the paramName child.
     *
     * @return the value of the paramName child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("paramName")
    GenericAttributeValue<String> getParamName();


}
