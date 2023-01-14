// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/component/editorArea

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/component/editorArea:abstractTab interface.
 */
public interface AbstractTab extends DomElement, AbstractPositioned {

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
     * Returns the value of the merge-mode child.
     *
     * @return the value of the merge-mode child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("merge-mode")
    GenericAttributeValue<String> getMergeMode();


    /**
     * Returns the value of the displayEssentialSectionIfPresent child.
     *
     * @return the value of the displayEssentialSectionIfPresent child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("displayEssentialSectionIfPresent")
    GenericAttributeValue<Boolean> getDisplayEssentialSectionIfPresent();


    /**
     * Returns the value of the tooltipText child.
     *
     * @return the value of the tooltipText child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("tooltipText")
    GenericAttributeValue<String> getTooltipText();


    /**
     * Returns the value of the initiallyOpened child.
     *
     * @return the value of the initiallyOpened child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("initiallyOpened")
    GenericAttributeValue<Boolean> getInitiallyOpened();

}
