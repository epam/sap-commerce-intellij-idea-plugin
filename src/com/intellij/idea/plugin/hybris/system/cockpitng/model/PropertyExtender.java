// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/spring

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/spring:property-extenderElemType interface.
 */
public interface PropertyExtender extends DomElement, AbstractExtender {

    /**
     * Returns the value of the value child.
     *
     * @return the value of the value child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("value")
    GenericAttributeValue<String> getValue();


    /**
     * Returns the value of the value-type child.
     *
     * @return the value of the value-type child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("value-type")
    GenericAttributeValue<String> getValueType();


}
