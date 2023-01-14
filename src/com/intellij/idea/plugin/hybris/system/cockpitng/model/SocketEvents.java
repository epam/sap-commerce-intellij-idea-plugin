// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    null

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

/**
 * null:SocketEvents interface.
 * <pre>
 * <h3>Type null:SocketEvents documentation</h3>
 * Groups socket events
 * </pre>
 */
public interface SocketEvents extends DomElement {

    /**
     * Returns the list of socket-event children.
     *
     * @return the list of socket-event children.
     */
    @NotNull
    @SubTagList("socket-event")
    java.util.List<SocketEvent> getSocketEvents();

    /**
     * Adds new child to the list of socket-event children.
     *
     * @return created child
     */
    @SubTagList("socket-event")
    SocketEvent addSocketEvent();


}
