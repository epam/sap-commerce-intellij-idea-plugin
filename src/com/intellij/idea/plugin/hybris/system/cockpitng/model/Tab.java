// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/component/editorArea

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/component/editorArea:tab interface.
 */
public interface Tab extends DomElement, AbstractTab {

    /**
     * Returns the list of customSection children.
     *
     * @return the list of customSection children.
     */
    @NotNull
    @SubTagList("customSection")
    java.util.List<CustomSection> getCustomSections();

    /**
     * Adds new child to the list of customSection children.
     *
     * @return created child
     */
    @SubTagList("customSection")
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
