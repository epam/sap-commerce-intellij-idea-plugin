// Generated on Sat Jan 14 21:57:32 CET 2023
// DTD/Schema  :    null

package com.intellij.idea.plugin.hybris.system.cockpitng.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.SubTag;
import org.jetbrains.annotations.NotNull;

/**
 * null:InstanceSettings interface.
 * <pre>
 * <h3>Type null:InstanceSettings documentation</h3>
 * Settings describing how template instances should work.
 * </pre>
 */
public interface InstanceSettings extends DomElement {

    /**
     * Returns the value of the socketEventRoutingMode child.
     * <pre>
     * <h3>Attribute null:socketEventRoutingMode documentation</h3>
     * Defines how socket events are forwarded to the template instances.
     * </pre>
     *
     * @return the value of the socketEventRoutingMode child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("socketEventRoutingMode")
    GenericAttributeValue<SocketEventRoutingMode> getSocketEventRoutingMode();


    /**
     * Returns the value of the create child.
     * <pre>
     * <h3>Element null:create documentation</h3>
     * Defines when new template instance should be created.
     * </pre>
     *
     * @return the value of the create child.
     */
    @NotNull
    @SubTag("create")
    CreateSettings getCreate();


    /**
     * Returns the value of the close child.
     * <pre>
     * <h3>Element null:close documentation</h3>
     * Defines when new template instance should be closed.
     * </pre>
     *
     * @return the value of the close child.
     */
    @NotNull
    @SubTag("close")
    CloseSettings getClose();


    /**
     * Returns the value of the select child.
     * <pre>
     * <h3>Element null:select documentation</h3>
     * Defines when new template instance should be selected (shown).
     * </pre>
     *
     * @return the value of the select child.
     */
    @NotNull
    @SubTag("select")
    SelectSettings getSelect();


}
