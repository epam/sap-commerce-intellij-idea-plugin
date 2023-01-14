// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/config/simplesearch

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.SubTag;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/config/simplesearch:simple-searchElemType interface.
 */
public interface SimpleSearch extends DomElement {

    /**
     * Returns the value of the includeSubtypes child.
     *
     * @return the value of the includeSubtypes child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("includeSubtypes")
    GenericAttributeValue<Boolean> getIncludeSubtypes();


    /**
     * Returns the list of field children.
     *
     * @return the list of field children.
     */
    @NotNull
    @SubTagList("field")
    java.util.List<Field> getFields();

    /**
     * Adds new child to the list of field children.
     *
     * @return created child
     */
    @SubTagList("field")
    Field addField();


    /**
     * Returns the value of the sort-field child.
     *
     * @return the value of the sort-field child.
     */
    @NotNull
    @SubTag("sort-field")
    SortField getSortField();


}
