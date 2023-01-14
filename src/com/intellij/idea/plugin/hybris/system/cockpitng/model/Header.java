// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/component/compareview

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/component/compareview:header interface.
 */
public interface Header extends DomElement, ImagePreview {

    /**
     * Returns the list of additional-renderer children.
     *
     * @return the list of additional-renderer children.
     */
    @NotNull
    @SubTagList("additional-renderer")
    java.util.List<Renderer> getAdditionalRenderers();

    /**
     * Adds new child to the list of additional-renderer children.
     *
     * @return created child
     */
    @SubTagList("additional-renderer")
    Renderer addAdditionalRenderer();


}
