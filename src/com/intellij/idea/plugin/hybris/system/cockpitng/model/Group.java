// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpit/test

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.SubTag;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpit/test:group interface.
 */
public interface Group extends DomElement {

    /**
     * Returns the list of property children.
     *
     * @return the list of property children.
     */
    @NotNull
    @SubTagList("property")
    java.util.List<Property> getProperties();

    /**
     * Adds new child to the list of property children.
     *
     * @return created child
     */
    @SubTagList("property")
    Property addProperty();


    /**
     * Returns the value of the title child.
     *
     * @return the value of the title child.
     */
    @NotNull
    @SubTag("title")
    GenericDomValue<String> getTitle();


}
