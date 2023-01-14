// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/spring

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/spring:list-extenderElemType interface.
 */
public interface ListExtender extends DomElement, CollectionExtender {

    /**
     * Returns the value of the sort child.
     *
     * @return the value of the sort child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("sort")
    GenericAttributeValue<Boolean> getSort();


    /**
     * Returns the value of the comparator child.
     *
     * @return the value of the comparator child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("comparator")
    GenericAttributeValue<String> getComparator();

    /**
     * Returns the list of comparator children.
     *
     * @return the list of comparator children.
     */
    @NotNull
    @SubTagList("comparator")
    java.util.List<Comparator> getComparators();

    /**
     * Adds new child to the list of comparator children.
     *
     * @return created child
     */
    @SubTagList("comparator")
    Comparator addComparator();


}
