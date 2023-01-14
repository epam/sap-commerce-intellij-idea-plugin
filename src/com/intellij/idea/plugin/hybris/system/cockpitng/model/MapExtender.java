// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/spring

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/spring:map-extenderElemType interface.
 */
public interface MapExtender extends DomElement, AbstractExtender {

    /**
     * Returns the list of put children.
     *
     * @return the list of put children.
     */
    @NotNull
    @SubTagList("put")
    java.util.List<GenericDomValue<String>> getPuts();

    /**
     * Adds new child to the list of put children.
     *
     * @return created child
     */
    @SubTagList("put")
    GenericDomValue<String> addPut();


    /**
     * Returns the list of remove children.
     *
     * @return the list of remove children.
     */
    @NotNull
    @SubTagList("remove")
    java.util.List<GenericDomValue<String>> getRemoves();

    /**
     * Adds new child to the list of remove children.
     *
     * @return created child
     */
    @SubTagList("remove")
    GenericDomValue<String> addRemove();


}
