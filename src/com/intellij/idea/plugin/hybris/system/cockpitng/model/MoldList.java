// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/config/collectionbrowser

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/config/collectionbrowser:mold-list interface.
 */
public interface MoldList extends DomElement {

    /**
     * Returns the value of the default-mold child.
     *
     * @return the value of the default-mold child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("default-mold")
    GenericAttributeValue<String> getDefaultMold();


    /**
     * Returns the list of mold children.
     *
     * @return the list of mold children.
     */
    @NotNull
    @SubTagList("mold")
    @Required
    java.util.List<Mold> getMolds();

    /**
     * Adds new child to the list of mold children.
     *
     * @return created child
     */
    @SubTagList("mold")
    Mold addMold();


}
