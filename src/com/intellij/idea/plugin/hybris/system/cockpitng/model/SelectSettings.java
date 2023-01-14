// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    null

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.SubTag;
import org.jetbrains.annotations.NotNull;

/**
 * null:SelectSettings interface.
 * <pre>
 * <h3>Type null:SelectSettings documentation</h3>
 * Template instances selection settings.
 * </pre>
 */
public interface SelectSettings extends DomElement {

    /**
     * Returns the value of the onInit child.
     * <pre>
     * <h3>Attribute null:onInit documentation</h3>
     * Determines ifthe template instance should be selected (shown) right after
     * 					initialization.
     * </pre>
     *
     * @return the value of the onInit child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("onInit")
    GenericAttributeValue<Boolean> getOnInit();


    /**
     * Returns the value of the incoming-events child.
     * <pre>
     * <h3>Element null:incoming-events documentation</h3>
     * Lists incoming socket events for which template instance should be selected
     * 						(shown).
     * </pre>
     *
     * @return the value of the incoming-events child.
     */
    @NotNull
    @SubTag("incoming-events")
    SocketEvents getIncomingEvents();


    /**
     * Returns the value of the all-incoming-events child.
     * <pre>
     * <h3>Element null:all-incoming-events documentation</h3>
     * Determines if template instance should be selected in case of any kind off
     * 						incomming socket
     * 						event.
     * </pre>
     * <pre>
     * <h3>Type null:AllSocketEvents documentation</h3>
     * Marker tag. If present, all socket events defined for the given widget template will be
     * 				used in the
     * 				context when the tag is used.
     * </pre>
     *
     * @return the value of the all-incoming-events child.
     */
    @NotNull
    @SubTag("all-incoming-events")
    GenericDomValue<String> getAllIncomingEvents();


}
