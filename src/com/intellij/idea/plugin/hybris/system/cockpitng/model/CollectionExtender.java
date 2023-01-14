// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/spring

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/spring:collection-extender interface.
 */
public interface CollectionExtender extends DomElement, AbstractExtender {

    /**
     * Returns the value of the unique child.
     *
     * @return the value of the unique child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("unique")
    GenericAttributeValue<Boolean> getUnique();

    /**
     * Returns the list of add children.
     *
     * @return the list of add children.
     */
    @NotNull
    @SubTagList("add")
    java.util.List<GenericDomValue<String>> getAdds();

    /**
     * Adds new child to the list of add children.
     *
     * @return created child
     */
    @SubTagList("add")
    GenericDomValue<String> addAdd();


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
