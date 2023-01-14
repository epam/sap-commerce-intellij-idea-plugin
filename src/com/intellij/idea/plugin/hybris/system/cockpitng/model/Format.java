// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/config/perspectiveChooser

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/config/perspectiveChooser:format interface.
 */
public interface Format extends DomElement {

    /**
     * Returns the value of the id child.
     *
     * @return the value of the id child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("id")
    @Required
    GenericAttributeValue<String> getId();


    /**
     * Returns the value of the title-key child.
     *
     * @return the value of the title-key child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("title-key")
    GenericAttributeValue<String> getTitleKey();


    /**
     * Returns the value of the title child.
     *
     * @return the value of the title child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("title")
    GenericAttributeValue<String> getTitle();


    /**
     * Returns the value of the description-key child.
     *
     * @return the value of the description-key child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("description-key")
    GenericAttributeValue<String> getDescriptionKey();


    /**
     * Returns the value of the description child.
     *
     * @return the value of the description child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("description")
    GenericAttributeValue<String> getDescription();


    /**
     * Returns the value of the icon-key child.
     *
     * @return the value of the icon-key child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("icon-key")
    GenericAttributeValue<String> getIconKey();


    /**
     * Returns the value of the icon child.
     *
     * @return the value of the icon child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("icon")
    GenericAttributeValue<String> getIcon();


}
