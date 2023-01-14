// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/config/notifications

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.SubTag;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/config/notifications:NotificationDefaults interface.
 */
public interface NotificationDefaults extends DomElement {

    /**
     * Returns the value of the linksEnabled child.
     *
     * @return the value of the linksEnabled child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("linksEnabled")
    GenericAttributeValue<Boolean> getLinksEnabled();


    /**
     * Returns the value of the fallback child.
     *
     * @return the value of the fallback child.
     */
    @NotNull
    @SubTag("fallback")
    NotificationRenderingInfo getFallback();


    /**
     * Returns the value of the timeouts child.
     *
     * @return the value of the timeouts child.
     */
    @NotNull
    @SubTag("timeouts")
    NotificationTimeouts getTimeouts();


    /**
     * Returns the list of destinations children.
     *
     * @return the list of destinations children.
     */
    @NotNull
    @SubTagList("destinations")
    java.util.List<NotificationDestination> getDestinationses();

    /**
     * Adds new child to the list of destinations children.
     *
     * @return created child
     */
    @SubTagList("destinations")
    NotificationDestination addDestinations();


}
