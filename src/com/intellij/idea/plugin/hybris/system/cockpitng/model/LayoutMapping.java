// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/config/extendedsplitlayout

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/config/extendedsplitlayout:LayoutMapping interface.
 */
public interface LayoutMapping extends DomElement {

    /**
     * Returns the value of the parentLayout child.
     *
     * @return the value of the parentLayout child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("parentLayout")
    @Required
    GenericAttributeValue<String> getParentLayout();


    /**
     * Returns the value of the selfLayout child.
     *
     * @return the value of the selfLayout child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("selfLayout")
    @Required
    GenericAttributeValue<String> getSelfLayout();


    /**
     * Returns the value of the merge-mode child.
     *
     * @return the value of the merge-mode child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("merge-mode")
    GenericAttributeValue<MergeMode> getMergeMode();


}
