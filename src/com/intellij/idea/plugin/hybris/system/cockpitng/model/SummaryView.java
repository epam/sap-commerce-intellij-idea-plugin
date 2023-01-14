// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/component/summaryview

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/component/summaryview:summary-viewElemType interface.
 */
public interface SummaryView extends DomElement, ImagePreview {

    /**
     * Returns the value of the display-title child.
     *
     * @return the value of the display-title child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("display-title")
    GenericAttributeValue<Boolean> getDisplayTitle();


    /**
     * Returns the list of custom-section children.
     *
     * @return the list of custom-section children.
     */
    @NotNull
    @SubTagList("custom-section")
    java.util.List<CustomSection> getCustomSections();

    /**
     * Adds new child to the list of custom-section children.
     *
     * @return created child
     */
    @SubTagList("custom-section")
    CustomSection addCustomSection();


    /**
     * Returns the list of section children.
     *
     * @return the list of section children.
     */
    @NotNull
    @SubTagList("section")
    java.util.List<Section> getSections();

    /**
     * Adds new child to the list of section children.
     *
     * @return created child
     */
    @SubTagList("section")
    Section addSection();


}
