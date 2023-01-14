// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/config/notifications

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/config/notifications:NotificationReferences interface.
 */
public interface NotificationReferences extends DomElement {

    /**
     * Returns the value of the linksEnabled child.
     *
     * @return the value of the linksEnabled child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("linksEnabled")
    GenericAttributeValue<Boolean> getLinksEnabled();


    /**
     * Returns the list of reference children.
     *
     * @return the list of reference children.
     */
    @NotNull
    @SubTagList("reference")
    java.util.List<NotificationReference> getReferences();

    /**
     * Adds new child to the list of reference children.
     *
     * @return created child
     */
    @SubTagList("reference")
    NotificationReference addReference();


}
