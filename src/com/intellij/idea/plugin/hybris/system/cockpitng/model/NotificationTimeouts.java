// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/config/notifications

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/config/notifications:NotificationTimeouts interface.
 */
public interface NotificationTimeouts extends DomElement {

    /**
     * Returns the value of the default child.
     *
     * @return the value of the default child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("default")
    GenericAttributeValue<Integer> getDefault();


    /**
     * Returns the list of timeout children.
     *
     * @return the list of timeout children.
     */
    @NotNull
    @SubTagList("timeout")
    java.util.List<NotificationTimeout> getTimeouts();

    /**
     * Adds new child to the list of timeout children.
     *
     * @return created child
     */
    @SubTagList("timeout")
    NotificationTimeout addTimeout();


}
