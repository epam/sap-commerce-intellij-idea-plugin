// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/config/wizard-config

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/config/wizard-config:flowElemType interface.
 */
public interface Flow extends DomElement, AbstractFlow {

    /**
     * Returns the value of the title child.
     *
     * @return the value of the title child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("title")
    @Required
    GenericAttributeValue<String> getTitle();


    /**
     * Returns the value of the size child.
     *
     * @return the value of the size child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("size")
    GenericAttributeValue<Size> getSize();


    /**
     * Returns the value of the show-breadcrumb child.
     *
     * @return the value of the show-breadcrumb child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("show-breadcrumb")
    GenericAttributeValue<Boolean> getShowBreadcrumb();


}
