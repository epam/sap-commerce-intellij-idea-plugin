// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/config/dashboard

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/config/dashboard:placement interface.
 */
public interface Placement extends DomElement {

    /**
     * Returns the value of the widgetId child.
     *
     * @return the value of the widgetId child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("widgetId")
    @Required
    GenericAttributeValue<String> getWidgetId();


    /**
     * Returns the value of the width child.
     *
     * @return the value of the width child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("width")
    GenericAttributeValue<Integer> getWidth();


    /**
     * Returns the value of the height child.
     *
     * @return the value of the height child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("height")
    GenericAttributeValue<Integer> getHeight();


    /**
     * Returns the value of the x child.
     *
     * @return the value of the x child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("x")
    GenericAttributeValue<Integer> getX();


    /**
     * Returns the value of the y child.
     *
     * @return the value of the y child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("y")
    GenericAttributeValue<Integer> getY();


    /**
     * Returns the value of the merge-mode child.
     *
     * @return the value of the merge-mode child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("merge-mode")
    GenericAttributeValue<String> getMergeMode();


}
