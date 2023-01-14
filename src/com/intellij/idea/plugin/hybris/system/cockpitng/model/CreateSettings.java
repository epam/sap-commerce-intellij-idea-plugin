// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    null

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.SubTag;
import org.jetbrains.annotations.NotNull;

/**
 * null:CreateSettings interface.
 * <pre>
 * <h3>Type null:CreateSettings documentation</h3>
 * Template instances creation settings.
 * </pre>
 */
public interface CreateSettings extends DomElement {

    /**
     * Returns the value of the onInit child.
     * <pre>
     * <h3>Attribute null:onInit documentation</h3>
     * Determines if the template instance should be created automatically on template
     * 					initialization.
     * </pre>
     *
     * @return the value of the onInit child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("onInit")
    GenericAttributeValue<Boolean> getOnInit();


    /**
     * Returns the value of the reuseExisting child.
     * <pre>
     * <h3>Attribute null:reuseExisting documentation</h3>
     * Determines if new template instance should be created or rather reuse existing
     * 					instance.
     * </pre>
     *
     * @return the value of the reuseExisting child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("reuseExisting")
    GenericAttributeValue<Boolean> getReuseExisting();


    /**
     * Returns the value of the incoming-events child.
     * <pre>
     * <h3>Element null:incoming-events documentation</h3>
     * Lists incoming socket events for which new instance should be created
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
     * Determines if new instance should be created in case of any incoming socket event.
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
