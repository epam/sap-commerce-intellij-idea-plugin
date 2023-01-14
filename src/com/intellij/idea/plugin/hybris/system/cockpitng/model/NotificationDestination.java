// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    http://www.hybris.com/cockpitng/config/notifications

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

/**
 * http://www.hybris.com/cockpitng/config/notifications:NotificationDestination interface.
 */
public interface NotificationDestination extends DomElement {

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


}
