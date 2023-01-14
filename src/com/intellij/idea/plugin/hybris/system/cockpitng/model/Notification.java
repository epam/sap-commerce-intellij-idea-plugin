// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/config/notifications

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.Required;
import com.intellij.util.xml.SubTag;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/config/notifications:Notification interface.
 */
public interface Notification extends DomElement, NotificationRenderingInfo {

    /**
     * Returns the value of the eventType child.
     *
     * @return the value of the eventType child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("eventType")
    @Required
    GenericAttributeValue<String> getEventType();


    /**
     * Returns the value of the level child.
     *
     * @return the value of the level child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("level")
    @Required
    GenericAttributeValue<ImportanceLevel> getLevel();


    /**
     * Returns the value of the destination child.
     *
     * @return the value of the destination child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("destination")
    GenericAttributeValue<Destination> getDestination();


    /**
     * Returns the value of the referencesType child.
     *
     * @return the value of the referencesType child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("referencesType")
    GenericAttributeValue<String> getReferencesType();

    /**
     * Returns the value of the message child.
     *
     * @return the value of the message child.
     */
    @NotNull
    @SubTag("message")
    GenericDomValue<String> getMessage();


    /**
     * Returns the value of the timeout child.
     *
     * @return the value of the timeout child.
     */
    @NotNull
    @SubTag("timeout")
    GenericDomValue<Integer> getTimeout();


    /**
     * Returns the value of the references child.
     *
     * @return the value of the references child.
     */
    @NotNull
    @SubTag("references")
    NotificationReferences getReferences();


}
