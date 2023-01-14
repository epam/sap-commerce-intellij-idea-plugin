// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/component/gridView

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/component/gridView:grid-viewElemType interface.
 */
public interface GridView extends DomElement, ImagePreview {

    /**
     * Returns the list of additionalRenderer children.
     *
     * @return the list of additionalRenderer children.
     */
    @NotNull
    @SubTagList("additionalRenderer")
    java.util.List<Renderer> getAdditionalRenderers();

    /**
     * Adds new child to the list of additionalRenderer children.
     *
     * @return created child
     */
    @SubTagList("additionalRenderer")
    Renderer addAdditionalRenderer();


}
