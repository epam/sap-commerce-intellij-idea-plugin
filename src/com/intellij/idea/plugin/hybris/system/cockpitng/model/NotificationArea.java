// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/config/notifications

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.SubTag;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/config/notifications:notification-areaElemType interface.
 */
public interface NotificationArea extends DomElement {

    /**
     * Returns the value of the defaults child.
     *
     * @return the value of the defaults child.
     */
    @NotNull
    @SubTag("defaults")
    NotificationDefaults getDefaults();


    /**
     * Returns the list of notifications children.
     *
     * @return the list of notifications children.
     */
    @NotNull
    @SubTagList("notifications")
    java.util.List<Notification> getNotificationses();

    /**
     * Adds new child to the list of notifications children.
     *
     * @return created child
     */
    @SubTagList("notifications")
    Notification addNotifications();


}
