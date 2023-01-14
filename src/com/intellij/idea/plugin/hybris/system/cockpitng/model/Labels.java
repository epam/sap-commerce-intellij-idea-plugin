// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpit/config/hybris

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.SubTag;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpit/config/hybris:labels interface.
 */
public interface Labels extends DomElement {

    /**
     * Returns the value of the beanId child.
     *
     * @return the value of the beanId child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("beanId")
    GenericAttributeValue<String> getBeanId();


    /**
     * Returns the value of the label child.
     *
     * @return the value of the label child.
     */
    @NotNull
    @SubTag("label")
    GenericDomValue<String> getLabel();


    /**
     * Returns the value of the shortLabel child.
     *
     * @return the value of the shortLabel child.
     */
    @NotNull
    @SubTag("shortLabel")
    GenericDomValue<String> getShortLabel();


    /**
     * Returns the value of the description child.
     *
     * @return the value of the description child.
     */
    @NotNull
    @SubTag("description")
    GenericDomValue<String> getDescription();


    /**
     * Returns the value of the iconPath child.
     *
     * @return the value of the iconPath child.
     */
    @NotNull
    @SubTag("iconPath")
    GenericDomValue<String> getIconPath();


}
