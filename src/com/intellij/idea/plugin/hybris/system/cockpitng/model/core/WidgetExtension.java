/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019 EPAM Systems <hybrisideaplugin@epam.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

// Generated on Wed Jan 18 00:34:54 CET 2023
// DTD/Schema  :    null

package com.intellij.idea.plugin.hybris.system.cockpitng.model.core;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import com.intellij.util.xml.SubTag;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

/**
 * null:WidgetExtension interface.
 * <pre>
 * <h3>Type null:WidgetExtension documentation</h3>
 * Allows to change existing widget configuration (in example add/remove child widgets).
 * </pre>
 */
public interface WidgetExtension extends DomElement {

    /**
     * Returns the value of the widgetId child.
     * <pre>
     * <h3>Attribute null:widgetId documentation</h3>
     * ID of existing widget that is the target for the extension.
     * </pre>
     *
     * @return the value of the widgetId child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("widgetId")
    @Required
    GenericAttributeValue<String> getWidgetId();


    /**
     * Returns the value of the contextId child.
     *
     * @return the value of the contextId child.
     */
    @NotNull
    @com.intellij.util.xml.Attribute("contextId")
    GenericAttributeValue<String> getContextId();


    /**
     * Returns the list of move children.
     * <pre>
     * <h3>Element null:move documentation</h3>
     * Specifies widgets to be moved to the target widget.
     * </pre>
     *
     * @return the list of move children.
     */
    @NotNull
    @SubTagList("move")
    java.util.List<WidgetMove> getMoves();

    /**
     * Adds new child to the list of move children.
     *
     * @return created child
     */
    @SubTagList("move")
    WidgetMove addMove();


    /**
     * Returns the list of remove children.
     * <pre>
     * <h3>Element null:remove documentation</h3>
     * Specifies widget to be removed from the target widgets children.
     * </pre>
     *
     * @return the list of remove children.
     */
    @NotNull
    @SubTagList("remove")
    java.util.List<WidgetRemoveEntry> getRemoves();

    /**
     * Adds new child to the list of remove children.
     *
     * @return created child
     */
    @SubTagList("remove")
    WidgetRemoveEntry addRemove();


    /**
     * Returns the list of remove-all children.
     * <pre>
     * <h3>Element null:remove-all documentation</h3>
     * Allows to remove all children widgets from the target widgets children.
     * </pre>
     *
     * @return the list of remove-all children.
     */
    @NotNull
    @SubTagList("remove-all")
    java.util.List<WidgetRemoveAllEntry> getRemoveAlls();

    /**
     * Adds new child to the list of remove-all children.
     *
     * @return created child
     */
    @SubTagList("remove-all")
    WidgetRemoveAllEntry addRemoveAll();


    /**
     * Returns the list of widget children.
     * <pre>
     * <h3>Element null:widget documentation</h3>
     * Allows to add new child widget to the target widget.
     * </pre>
     *
     * @return the list of widget children.
     */
    @NotNull
    @SubTagList("widget")
    java.util.List<Widget> getWidgets();

    /**
     * Adds new child to the list of widget children.
     *
     * @return created child
     */
    @SubTagList("widget")
    Widget addWidget();


    /**
     * Returns the value of the instance-settings child.
     * <pre>
     * <h3>Element null:instance-settings documentation</h3>
     * Allows to modify template instance behavior settings of the target widget.
     * </pre>
     *
     * @return the value of the instance-settings child.
     */
    @NotNull
    @SubTag("instance-settings")
    InstanceSettings getInstanceSettings();


    /**
     * Returns the list of setting children.
     * <pre>
     * <h3>Element null:setting documentation</h3>
     * Allows to modify settings of the target widget instance.
     * </pre>
     *
     * @return the list of setting children.
     */
    @NotNull
    @SubTagList("setting")
    java.util.List<WidgetSetting> getSettings();

    /**
     * Adds new child to the list of setting children.
     *
     * @return created child
     */
    @SubTagList("setting")
    WidgetSetting addSetting();


    /**
     * Returns the list of access children.
     * <pre>
     * <h3>Element null:access documentation</h3>
     * Allows to modify access of the target widget instance.
     * </pre>
     *
     * @return the list of access children.
     */
    @NotNull
    @SubTagList("access")
    java.util.List<AccessSettings> getAccesses();

    /**
     * Adds new child to the list of access children.
     *
     * @return created child
     */
    @SubTagList("access")
    AccessSettings addAccess();


    /**
     * Returns the value of the virtual-sockets child.
     * <pre>
     * <h3>Element null:virtual-sockets documentation</h3>
     * A set of dynamic sockets. Even though virtual socket is not specified by widget
     * 				definition, application
     * 				designer can add them via widgets.xml or application orchestrator.
     * </pre>
     *
     * @return the value of the virtual-sockets child.
     */
    @NotNull
    @SubTag("virtual-sockets")
    VirtualSockets getVirtualSockets();


}
