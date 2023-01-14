// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/config/links

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/config/links:link interface.
 */
public interface Link extends DomElement, Positioned {

    /**
     * Returns the value of the label child.
     *
     * @return the value of the label child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("label")
    @Required
    GenericAttributeValue<String> getLabel();


    /**
     * Returns the value of the icon child.
     *
     * @return the value of the icon child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("icon")
    GenericAttributeValue<String> getIcon();


    /**
     * Returns the value of the url child.
     *
     * @return the value of the url child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("url")
    GenericAttributeValue<String> getUrl();


    /**
     * Returns the value of the target child.
     *
     * @return the value of the target child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("target")
    GenericAttributeValue<Target> getTarget();


    /**
     * Returns the value of the merge-mode child.
     *
     * @return the value of the merge-mode child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("merge-mode")
    GenericAttributeValue<MergeMode> getMergeMode();


}
