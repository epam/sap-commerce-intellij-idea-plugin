// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/component/compareview

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.SubTag;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/component/compareview:compare-viewElemType interface.
 */
public interface CompareView extends DomElement {

    /**
     * Returns the value of the editor-area-ctx child.
     *
     * @return the value of the editor-area-ctx child.
     */
    @NotNull
    @SubTag("editor-area-ctx")
    EditorAreaCtx getEditorAreaCtx();


    /**
     * Returns the value of the grid-view-ctx child.
     *
     * @return the value of the grid-view-ctx child.
     */
    @NotNull
    @SubTag("grid-view-ctx")
    GridViewCtx getGridViewCtx();


    /**
     * Returns the value of the header child.
     *
     * @return the value of the header child.
     */
    @NotNull
    @SubTag("header")
    Header getHeader();


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
