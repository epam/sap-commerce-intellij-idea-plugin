// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/component/listView

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/component/listView:list-viewElemType interface.
 */
public interface ListView extends DomElement {

    /**
     * Returns the value of the refresh-after-object-creation child.
     *
     * @return the value of the refresh-after-object-creation child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("refresh-after-object-creation")
    GenericAttributeValue<Boolean> getRefreshAfterObjectCreation();


    /**
     * Returns the value of the show-header child.
     *
     * @return the value of the show-header child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("show-header")
    GenericAttributeValue<Boolean> getShowHeader();


    /**
     * Returns the list of column children.
     *
     * @return the list of column children.
     */
    @NotNull
    @SubTagList("column")
    java.util.List<ListColumn> getColumns();

    /**
     * Adds new child to the list of column children.
     *
     * @return created child
     */
    @SubTagList("column")
    ListColumn addColumn();


}
