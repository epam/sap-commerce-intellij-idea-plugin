// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    null

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.SubTag;
import org.jetbrains.annotations.NotNull;

/**
 * null:CloseSettings interface.
 * <pre>
 * <h3>Type null:CloseSettings documentation</h3>
 * Template instances closing settings.
 * </pre>
 */
public interface CloseSettings extends DomElement {

    /**
     * Returns the value of the incoming-events child.
     * <pre>
     * <h3>Element null:incoming-events documentation</h3>
     * Lists incoming socket events for which template instance should be closed.
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
     * Determines if template instance should be closed in case of any kind off
     * 							incoming socket
     * 							event.
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


    /**
     * Returns the value of the outgoing-events child.
     * <pre>
     * <h3>Element null:outgoing-events documentation</h3>
     * Lists outgoing socket events for which template instance should be closed.
     * </pre>
     *
     * @return the value of the outgoing-events child.
     */
    @NotNull
    @SubTag("outgoing-events")
    SocketEvents getOutgoingEvents();


    /**
     * Returns the value of the all-outgoing-events child.
     * <pre>
     * <h3>Element null:all-outgoing-events documentation</h3>
     * Determines if template instance should be closed in case of any kind off
     * 							outgoing socket
     * 							event.
     * </pre>
     * <pre>
     * <h3>Type null:AllSocketEvents documentation</h3>
     * Marker tag. If present, all socket events defined for the given widget template will be
     * 				used in the
     * 				context when the tag is used.
     * </pre>
     *
     * @return the value of the all-outgoing-events child.
     */
    @NotNull
    @SubTag("all-outgoing-events")
    GenericDomValue<String> getAllOutgoingEvents();


}
