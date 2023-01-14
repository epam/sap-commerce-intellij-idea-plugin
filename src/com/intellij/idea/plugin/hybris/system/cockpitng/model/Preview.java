// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpit/config/hybris

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpit/config/hybris:preview interface.
 */
public interface Preview extends DomElement {

    /**
     * Returns the value of the fallbackToIcon child.
     *
     * @return the value of the fallbackToIcon child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("fallbackToIcon")
    GenericAttributeValue<Boolean> getFallbackToIcon();


    /**
     * Returns the value of the urlQualifier child.
     *
     * @return the value of the urlQualifier child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("urlQualifier")
    GenericAttributeValue<String> getUrlQualifier();


}
