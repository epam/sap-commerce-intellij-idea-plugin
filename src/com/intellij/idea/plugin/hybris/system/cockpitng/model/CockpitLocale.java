// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/config/availableLocales

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/config/availableLocales:cockpit-locale interface.
 */
public interface CockpitLocale extends DomElement {

    /**
     * Returns the value of the name child.
     *
     * @return the value of the name child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("name")
    @Required
    GenericAttributeValue<String> getName();


    /**
     * Returns the value of the locale child.
     *
     * @return the value of the locale child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("locale")
    @Required
    GenericAttributeValue<String> getLocale();


    /**
     * Returns the value of the enabled child.
     *
     * @return the value of the enabled child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("enabled")
    @Required
    GenericAttributeValue<Boolean> getEnabled();


    /**
     * Returns the value of the default child.
     *
     * @return the value of the default child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("default")
    GenericAttributeValue<Boolean> getDefault();


}
